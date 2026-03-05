package com.example.flink.udf;

import com.example.flink.model.TaggedTransaction;
import com.example.flink.model.Transaction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RichMapFunction that uses Drools rule engine to tag transactions.
 * This function properly manages the rule engine lifecycle in a distributed Flink environment.
 */
public class DroolsTaggingFunction extends RichMapFunction<Transaction, TaggedTransaction> {
    
    private static final Logger LOG = LoggerFactory.getLogger(DroolsTaggingFunction.class);
    private static final long serialVersionUID = 1L;
    
    private transient KieContainer kieContainer;
    private transient AtomicLong processedCount;
    private transient AtomicLong errorCount;
    
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        LOG.info("Initializing Drools KieContainer in task {}...", getRuntimeContext().getIndexOfThisSubtask());
        
        initializeKieContainer();
        this.processedCount = new AtomicLong(0);
        this.errorCount = new AtomicLong(0);
        
        LOG.info("Drools KieContainer initialized successfully in task {}.", 
                getRuntimeContext().getIndexOfThisSubtask());
    }
    
    /**
     * Initializes the Drools KieContainer from classpath resources.
     * This is called once per task instance (subtask).
     */
    private void initializeKieContainer() {
        try {
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
            
            // Load rules from classpath
            String rulePath = "rules/transaction-rules.drl";
            try (InputStream ruleStream = getClass().getClassLoader().getResourceAsStream(rulePath)) {
                if (ruleStream == null) {
                    throw new RuntimeException("Rule file not found in classpath: " + rulePath);
                }
                kieFileSystem.write(
                    ResourceFactory.newClassPathResource(rulePath, "UTF-8")
                );
                LOG.info("Loaded rule file from classpath: {}", rulePath);
            }
            
            // Build the KieModule
            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();
            
            // Check for compilation errors
            if (kieBuilder.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
                String errors = kieBuilder.getResults().getMessages().toString();
                LOG.error("Rule compilation errors: {}", errors);
                throw new RuntimeException("Rule compilation failed: " + errors);
            }
            
            // Create KieContainer
            KieModule kieModule = kieBuilder.getKieModule();
            this.kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
            
            LOG.info("KieContainer created with ReleaseId: {}", kieModule.getReleaseId());
            
        } catch (Exception e) {
            LOG.error("Failed to initialize KieContainer", e);
            throw new RuntimeException("KieContainer initialization failed", e);
        }
    }
    
    @Override
    public TaggedTransaction map(Transaction transaction) throws Exception {
        if (transaction == null) {
            LOG.warn("Received null transaction, skipping");
            return null;
        }
        
        String traceId = generateTraceId();
        
        try {
            // Execute Drools rules
            executeRules(transaction);
            
            // Convert to output format
            TaggedTransaction result = TaggedTransaction.fromTransaction(transaction, traceId);
            
            // Update metrics
            long count = processedCount.incrementAndGet();
            if (count % 1000 == 0) {
                LOG.info("Task {} processed {} transactions", 
                        getRuntimeContext().getIndexOfThisSubtask(), count);
            }
            
            return result;
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
            LOG.error("Error processing transaction {}: {}", 
                    transaction.getTransactionId(), e.getMessage(), e);
            
            // Return transaction with error tag
            transaction.addTag("PROCESSING_ERROR");
            transaction.setRiskScore(100);
            return TaggedTransaction.fromTransaction(transaction, traceId);
        }
    }
    
    /**
     * Executes Drools rules on the transaction.
     *
     * @param transaction the transaction to evaluate
     */
    private void executeRules(Transaction transaction) {
        // Use default stateless session (no name specified)
        StatelessKieSession session = kieContainer.newStatelessKieSession();
        
        // Set global logger
        session.setGlobal("logger", LOG);
        
        // Execute rules
        session.execute(transaction);
        
        // Note: StatelessKieSession doesn't require explicit disposal
    }
    
    @Override
    public void close() throws Exception {
        LOG.info("Closing DroolsTaggingFunction. Processed: {}, Errors: {}", 
                processedCount != null ? processedCount.get() : 0,
                errorCount != null ? errorCount.get() : 0);
        
        // Dispose KieContainer
        if (kieContainer != null) {
            kieContainer.dispose();
            kieContainer = null;
        }
        
        super.close();
    }
    
    /**
     * Generates a unique trace ID for distributed tracing.
     *
     * @return trace ID string
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
