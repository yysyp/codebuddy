package com.example.flink.rules;

import com.example.flink.model.Transaction;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe Drools rule engine wrapper for Flink UDF.
 * Uses session pooling for concurrent access.
 */
public class DroolsRuleEngine implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(DroolsRuleEngine.class);
    
    // Volatile for thread-safe lazy initialization
    private static volatile KieContainer kieContainer;
    private static final ReadWriteLock containerLock = new ReentrantReadWriteLock();
    
    // ThreadLocal for KieSession pooling - each thread gets its own session
    private static final ThreadLocal<KieSession> sessionPool = ThreadLocal.withInitial(() -> {
        if (kieContainer == null) {
            throw new IllegalStateException("KieContainer not initialized");
        }
        return kieContainer.newKieSession();
    });
    
    /**
     * Initializes the Drools rule engine with rules from classpath.
     * Thread-safe initialization using double-checked locking.
     */
    public static void initialize() {
        if (kieContainer == null) {
            containerLock.writeLock().lock();
            try {
                if (kieContainer == null) {
                    LOG.info("Initializing Drools rule engine...");
                    KieServices kieServices = KieServices.Factory.get();
                    KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
                    
                    // Load rules from classpath
                    String[] ruleFiles = {
                            "rules/transaction-rules.drl"
                    };
                    
                    for (String ruleFile : ruleFiles) {
                        var resource = DroolsRuleEngine.class.getClassLoader().getResource(ruleFile);
                        if (resource != null) {
                            LOG.info("Loading rule file: {}", ruleFile);
                            kieFileSystem.write(kieServices.getResources()
                                    .newClassPathResource(ruleFile));
                        } else {
                            LOG.warn("Rule file not found: {}", ruleFile);
                        }
                    }
                    
                    KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
                    kieBuilder.buildAll();
                    
                    // Check for compilation errors
                    if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
                        String errors = kieBuilder.getResults().getMessages().toString();
                        LOG.error("Drools compilation errors: {}", errors);
                        throw new RuntimeException("Drools compilation errors: " + errors);
                    }
                    
                    kieContainer = kieServices.newKieContainer(kieBuilder.getKieModule().getReleaseId());
                    LOG.info("Drools rule engine initialized successfully");
                }
            } finally {
                containerLock.writeLock().unlock();
            }
        }
    }
    
    /**
     * Applies rules to a transaction and returns the tagging result.
     * Thread-safe method using ThreadLocal session pooling.
     */
    public TaggingResult applyRules(Transaction transaction) {
        if (transaction == null) {
            LOG.warn("Null transaction provided to rule engine");
            return new TaggingResult();
        }
        
        ensureInitialized();
        
        KieSession session = null;
        try {
            session = sessionPool.get();
            
            TaggingResult result = new TaggingResult();
            
            // Insert facts into the session
            session.insert(transaction);
            session.insert(result);
            
            // Fire all rules
            int rulesFired = session.fireAllRules();
            LOG.debug("Fired {} rules for transaction {}", rulesFired, transaction.getTransactionId());
            
            // Clean up the session
            session.delete(session.getFactHandle(transaction));
            session.delete(session.getFactHandle(result));
            
            return result;
            
        } catch (Exception e) {
            LOG.error("Error applying rules to transaction {}: {}", 
                    transaction.getTransactionId(), e.getMessage(), e);
            // Return empty result on error to allow processing to continue
            TaggingResult errorResult = new TaggingResult();
            errorResult.addTag("RULE_ERROR");
            errorResult.updateRiskLevel("MEDIUM");
            errorResult.flagForReview("Rule processing error: " + e.getMessage());
            return errorResult;
        }
    }
    
    /**
     * Applies rules and returns a formatted string result suitable for SQL UDF.
     */
    public String applyRulesAsString(Transaction transaction) {
        TaggingResult result = applyRules(transaction);
        return formatResult(result);
    }
    
    private String formatResult(TaggingResult result) {
        if (result == null) {
            return "LOW||";
        }
        return String.format("%s|%s|%s",
                result.getRiskLevel(),
                result.getTagsAsString(),
                result.getAppliedRulesAsString()
        );
    }
    
    private void ensureInitialized() {
        if (kieContainer == null) {
            containerLock.readLock().lock();
            try {
                if (kieContainer == null) {
                    containerLock.readLock().unlock();
                    initialize();
                    containerLock.readLock().lock();
                }
            } finally {
                containerLock.readLock().unlock();
            }
        }
    }
    
    /**
     * Cleanup method to dispose ThreadLocal sessions.
     * Should be called when the UDF is closed.
     */
    public static void cleanup() {
        KieSession session = sessionPool.get();
        if (session != null) {
            try {
                session.dispose();
            } catch (Exception e) {
                LOG.warn("Error disposing KieSession: {}", e.getMessage());
            }
        }
        sessionPool.remove();
    }
    
    /**
     * Reloads rules from classpath.
     * Use with caution in production as it affects all sessions.
     */
    public static void reloadRules() {
        containerLock.writeLock().lock();
        try {
            LOG.info("Reloading Drools rules...");
            kieContainer = null;
            initialize();
        } finally {
            containerLock.writeLock().unlock();
        }
    }
}
