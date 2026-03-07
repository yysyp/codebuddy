package com.example.flink.service;

import com.example.flink.config.FlinkJobConfig;
import com.example.flink.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.drools.decisiontable.InputType;
import org.drools.decisiontable.SpreadsheetCompiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Drools Rule Engine Service for transaction tagging.
 * Thread-safe singleton pattern for rule engine management.
 */
@Slf4j
@Service
public class RuleEngineService {

    private static final String RULES_FILE = "rules/transaction-tagging.drl";
    private static final String RULES_DIR = "rules";
    private static final String DECISION_TABLE_FILE = "rules/decision-table.csv";

    private final AtomicReference<KieContainer> kieContainer = new AtomicReference<>();
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean destroyed = new AtomicBoolean(false);

    @Autowired(required = false)
    private FlinkJobConfig config;

    /**
     * Initialize the rule engine on startup
     */
    @PostConstruct
    public void initialize() {
        if (initialized.compareAndSet(false, true)) {
            try {
                log.info("Initializing Drools Rule Engine...");
                reloadRules();
                log.info("Drools Rule Engine initialized successfully");
            } catch (Exception e) {
                initialized.set(false);
                log.error("Failed to initialize Drools Rule Engine: {}", e.getMessage(), e);
                throw new RuntimeException("Rule engine initialization failed", e);
            }
        }
    }

    /**
     * Pre-destroy cleanup
     */
    @PreDestroy
    public void cleanup() {
        if (destroyed.compareAndSet(false, true)) {
            log.info("Cleaning up Drools Rule Engine...");
            kieContainer.set(null);
            initialized.set(false);
        }
    }

    /**
     * Reload rules from configuration
     */
    public synchronized void reloadRules() {
        try {
            // Mark as initialized when rules are successfully loaded
            initialized.set(true);
            
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

            // Load rules from classpath
            Resource resource = loadRulesResource();
            kieFileSystem.write(resource);

            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();

            if (kieBuilder.getResults().hasMessages(
                    org.kie.api.builder.Message.Level.ERROR)) {
                String errors = kieBuilder.getResults().getMessages().toString();
                log.error("Rule compilation errors: {}", errors);
                throw new RuntimeException("Rule compilation failed: " + errors);
            }

            KieModule kieModule = kieBuilder.getKieModule();
            KieContainer newContainer = kieServices.newKieContainer(kieModule.getReleaseId());

            KieContainer oldContainer = kieContainer.getAndSet(newContainer);
            if (oldContainer != null) {
                log.info("Old rule container replaced");
            }

            log.info("Rules reloaded successfully");
        } catch (Exception e) {
            log.error("Failed to reload rules: {}", e.getMessage(), e);
            throw new RuntimeException("Rule reload failed", e);
        }
    }

    /**
     * Reload rules from table-structured CSV definition
     */
    public synchronized void reloadRulesFromTable() {
        try {
            // Mark as initialized when rules are successfully loaded
            initialized.set(true);
            
            log.info("Loading rules from table-structured definition...");

            // Parse table rules
            TableRuleParserService tableRuleParser = new TableRuleParserService();
            String drlContent = tableRuleParser.parseRulesFromTable();

            log.debug("Generated DRL content:\n{}", drlContent);

            // Create KieBase from parsed DRL
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

            // Write the generated DRL content
            kieFileSystem.write("src/main/resources/rules/table-generated.drl", drlContent);

            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();

            if (kieBuilder.getResults().hasMessages(
                    org.kie.api.builder.Message.Level.ERROR)) {
                String errors = kieBuilder.getResults().getMessages().toString();
                log.error("Rule compilation errors from table: {}", errors);
                throw new RuntimeException("Rule compilation failed: " + errors);
            }

            KieModule kieModule = kieBuilder.getKieModule();
            KieContainer newContainer = kieServices.newKieContainer(kieModule.getReleaseId());

            KieContainer oldContainer = kieContainer.getAndSet(newContainer);
            if (oldContainer != null) {
                log.info("Old rule container replaced");
            }

            log.info("Rules loaded from table structure successfully");
        } catch (Exception e) {
            log.error("Failed to load rules from table: {}", e.getMessage(), e);
            throw new RuntimeException("Table rule loading failed", e);
        }
    }

    /**
     * Reload rules from Drools Decision Table (CSV format)
     */
    public synchronized void reloadRulesFromDecisionTable() {
        try {
            // Mark as initialized when rules are successfully loaded
            initialized.set(true);
            
            log.info("Loading rules from Drools Decision Table...");

            // Load decision table from file system or classpath
            String decisionTablePath = loadDecisionTablePath();
            String drlContent = compileDecisionTable(decisionTablePath);

            log.debug("Compiled DRL content from decision table:\n{}", drlContent);

            // Create KieBase from compiled DRL
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

            // Write the compiled DRL content
            kieFileSystem.write("src/main/resources/rules/decision-table-generated.drl", drlContent);

            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();

            if (kieBuilder.getResults().hasMessages(
                    org.kie.api.builder.Message.Level.ERROR)) {
                String errors = kieBuilder.getResults().getMessages().toString();
                log.error("Rule compilation errors from decision table: {}", errors);
                throw new RuntimeException("Decision table compilation failed: " + errors);
            }

            KieModule kieModule = kieBuilder.getKieModule();
            KieContainer newContainer = kieServices.newKieContainer(kieModule.getReleaseId());

            KieContainer oldContainer = kieContainer.getAndSet(newContainer);
            if (oldContainer != null) {
                log.info("Old rule container replaced");
            }

            log.info("Rules loaded from decision table successfully");
        } catch (Exception e) {
            log.error("Failed to load rules from decision table: {}", e.getMessage(), e);
            throw new RuntimeException("Decision table loading failed", e);
        }
    }

    /**
     * Load rules resource from classpath or file system
     */
    private String loadDecisionTablePath() throws IOException {
        // Get decision table path from config or use default
        String configuredPath = (config != null) ? config.getDecisionTablePath() : DECISION_TABLE_FILE;

        // Try file system first (works in both dev and test environments)
        Path path = Path.of(configuredPath);
        if (Files.exists(path)) {
            log.debug("Loading decision table from file system: {}", path);
            return path.toString();
        }

        // Try with src/main/resources prefix
        Path fullPath = Path.of("src/main/resources/" + configuredPath);
        if (Files.exists(fullPath)) {
            log.debug("Loading decision table from full path: {}", fullPath);
            return fullPath.toString();
        }

        // Try classpath
        InputStream is = getClass().getClassLoader().getResourceAsStream(configuredPath);
        if (is != null) {
            log.debug("Loading decision table from classpath: {}", configuredPath);
            is.close();
            return configuredPath;
        }

        // Try alternative file system paths
        Path altPath = Path.of(RULES_DIR, "decision-table.csv");
        if (Files.exists(altPath)) {
            log.debug("Loading decision table from alternative path: {}", altPath);
            return altPath.toString();
        }

        throw new IOException("Decision table file not found: " + configuredPath);
    }

    /**
     * Compile Drools Decision Table to DRL
     * Automatically detects file type (CSV or Excel) based on extension
     */
    private String compileDecisionTable(String decisionTablePath) throws IOException {
        SpreadsheetCompiler compiler = new SpreadsheetCompiler();

        InputStream decisionTableStream;
        Path path = Path.of(decisionTablePath);

        // Determine input type based on file extension
        InputType inputType;
        String lowerPath = decisionTablePath.toLowerCase();
        
        if (lowerPath.endsWith(".xls") || lowerPath.endsWith(".xlsx")) {
            inputType = InputType.XLS;
            log.debug("Detected Excel decision table format");
        } else if (lowerPath.endsWith(".csv")) {
            inputType = InputType.CSV;
            log.debug("Detected CSV decision table format");
        } else {
            // Default to CSV for backward compatibility
            inputType = InputType.CSV;
            log.warn("Unknown decision table format, defaulting to CSV");
        }

        if (Files.exists(path)) {
            decisionTableStream = Files.newInputStream(path);
            log.debug("Loading decision table from file system: {}", path);
        } else {
            decisionTableStream = getClass().getClassLoader().getResourceAsStream(decisionTablePath);
            if (decisionTableStream == null) {
                throw new IOException("Cannot read decision table from: " + decisionTablePath);
            }
            log.debug("Loading decision table from classpath: {}", decisionTablePath);
        }

        try {
            // Compile decision table to DRL
            String drlContent = compiler.compile(decisionTableStream, inputType);
            log.info("Successfully compiled decision table to DRL (format: {})", inputType);
            return drlContent;
        } finally {
            decisionTableStream.close();
        }
    }

    /**
     * Load rules resource from classpath or file system
     */
    private Resource loadRulesResource() throws IOException {
        // Try file system first (works in both dev and test environments)
        Path rulesPath = Path.of("src/main/resources/" + RULES_FILE);
        if (Files.exists(rulesPath)) {
            log.debug("Loading rules from file system: {}", rulesPath);
            return ResourceFactory.newFileResource(rulesPath.toFile())
                    .setResourceType(ResourceType.DRL)
                    .setSourcePath("rules/transaction-tagging.drl");
        }

        // Try classpath
        InputStream is = getClass().getClassLoader().getResourceAsStream(RULES_FILE);
        if (is != null) {
            log.debug("Loading rules from classpath: {}", RULES_FILE);
            return ResourceFactory.newInputStreamResource(is)
                    .setResourceType(ResourceType.DRL)
                    .setSourcePath("rules/transaction-tagging.drl");
        }

        // Try alternative file system paths
        Path altPath = Path.of(RULES_DIR, "transaction-tagging.drl");
        if (Files.exists(altPath)) {
            log.debug("Loading rules from alternative path: {}", altPath);
            return ResourceFactory.newFileResource(altPath.toFile())
                    .setResourceType(ResourceType.DRL)
                    .setSourcePath("rules/transaction-tagging.drl");
        }

        throw new IOException("Rules file not found: " + RULES_FILE);
    }

    /**
     * Execute rules on a transaction
     *
     * @param transaction the transaction to tag
     * @return the tagged transaction
     */
    public Transaction executeRules(Transaction transaction) {
        Objects.requireNonNull(transaction, "Transaction cannot be null");

        String traceId = transaction.getTraceId();
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
            transaction.setTraceId(traceId);
        }

        if (!initialized.get() || destroyed.get()) {
            log.warn("[traceId={}] Rule engine not initialized, returning transaction without tags", traceId);
            return transaction;
        }

        KieContainer container = kieContainer.get();
        if (container == null) {
            log.warn("[traceId={}] Rule container not available, returning transaction without tags", traceId);
            return transaction;
        }

        KieSession kieSession = null;
        try {
            long startTime = System.currentTimeMillis();

            KieBase kieBase = container.getKieBase();
            kieSession = kieBase.newKieSession();

            // Set global variables
            kieSession.setGlobal("log", log);
            kieSession.setGlobal("traceId", traceId);

            // Insert transaction into working memory
            kieSession.insert(transaction);

            // Fire all rules
            int rulesFired = kieSession.fireAllRules();

            long processingTime = System.currentTimeMillis() - startTime;

            log.info("[traceId={}] Rules executed: {} rules fired, processing time: {}ms, tags: {}",
                    traceId, rulesFired, processingTime, transaction.getTags());

            return transaction;

        } catch (Exception e) {
            log.error("[traceId={}] Error executing rules: {}", traceId, e.getMessage(), e);
            // Return transaction without tags - graceful degradation
            return transaction;
        } finally {
            if (kieSession != null) {
                try {
                    kieSession.dispose();
                } catch (Exception e) {
                    log.warn("[traceId={}] Error disposing KieSession: {}", traceId, e.getMessage());
                }
            }
        }
    }

    /**
     * Check if the rule engine is initialized and ready
     *
     * @return true if ready
     */
    public boolean isReady() {
        return initialized.get() && !destroyed.get() && kieContainer.get() != null;
    }

    /**
     * Get rule engine status information
     *
     * @return status string
     */
    public String getStatus() {
        return String.format("RuleEngineService{initialized=%s, destroyed=%s, container=%s}",
                initialized.get(), destroyed.get(), kieContainer.get() != null ? "available" : "null");
    }
}
