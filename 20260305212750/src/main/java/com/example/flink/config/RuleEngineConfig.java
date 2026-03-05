package com.example.flink.config;

import com.example.flink.model.Transaction;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Rule Engine Configuration.
 * Manages Drools KieContainer with dynamic rule reloading capability.
 */
public class RuleEngineConfig {
    
    private static final Logger LOG = LoggerFactory.getLogger(RuleEngineConfig.class);
    private static final String RULES_PATH = "src/main/resources/rules/";
    private static final String RULES_CLASSPATH = "rules/";
    
    private final AtomicReference<KieContainer> kieContainerRef;
    private final ExecutorService watcherExecutor;
    private final boolean enableDynamicReload;
    
    public RuleEngineConfig() {
        this(true);
    }
    
    public RuleEngineConfig(boolean enableDynamicReload) {
        this.enableDynamicReload = enableDynamicReload;
        this.kieContainerRef = new AtomicReference<>();
        this.watcherExecutor = enableDynamicReload ? Executors.newSingleThreadExecutor() : null;
        initializeRuleEngine();
    }
    
    /**
     * Initializes the rule engine and sets up dynamic reloading if enabled.
     */
    private void initializeRuleEngine() {
        LOG.info("Initializing Drools rule engine...");
        loadRules();
        
        if (enableDynamicReload) {
            startRuleWatcher();
        }
        
        LOG.info("Rule engine initialized successfully.");
    }
    
    /**
     * Loads rules from classpath and builds the KieContainer.
     */
    private synchronized void loadRules() {
        try {
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
            
            // Load rules from classpath resources
            loadRulesFromClasspath(kieServices, kieFileSystem);
            
            // Build the KieModule
            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();
            
            // Check for errors
            if (kieBuilder.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
                LOG.error("Rule compilation errors: {}", kieBuilder.getResults().getMessages());
                throw new RuntimeException("Rule compilation failed: " + kieBuilder.getResults().getMessages());
            }
            
            // Create KieContainer
            KieModule kieModule = kieBuilder.getKieModule();
            KieContainer newContainer = kieServices.newKieContainer(kieModule.getReleaseId());
            
            // Atomically update the container reference
            KieContainer oldContainer = kieContainerRef.getAndSet(newContainer);
            
            // Dispose old container if exists
            if (oldContainer != null) {
                oldContainer.dispose();
                LOG.info("Old KieContainer disposed and replaced with new one.");
            }
            
            LOG.info("Rules loaded successfully. ReleaseId: {}", kieModule.getReleaseId());
            
        } catch (Exception e) {
            LOG.error("Failed to load rules", e);
            throw new RuntimeException("Rule loading failed", e);
        }
    }
    
    /**
     * Loads rule files from classpath.
     */
    private void loadRulesFromClasspath(KieServices kieServices, KieFileSystem kieFileSystem) {
        // Load the main rules file
        String rulePath = RULES_CLASSPATH + "transaction-rules.drl";
        try {
            kieFileSystem.write(
                ResourceFactory.newClassPathResource(rulePath, "UTF-8")
            );
            LOG.info("Loaded rule file: {}", rulePath);
        } catch (Exception e) {
            LOG.warn("Could not load rule file from classpath: {}", rulePath);
            // Try loading from file system as fallback
            loadRulesFromFileSystem(kieFileSystem);
        }
    }
    
    /**
     * Loads rule files from file system as fallback.
     */
    private void loadRulesFromFileSystem(KieFileSystem kieFileSystem) {
        File rulesDir = new File(RULES_PATH);
        if (!rulesDir.exists() || !rulesDir.isDirectory()) {
            LOG.warn("Rules directory not found: {}", RULES_PATH);
            return;
        }
        
        File[] ruleFiles = rulesDir.listFiles((dir, name) -> name.endsWith(".drl"));
        if (ruleFiles == null || ruleFiles.length == 0) {
            LOG.warn("No rule files found in: {}", RULES_PATH);
            return;
        }
        
        for (File ruleFile : ruleFiles) {
            String path = "src/main/resources/rules/" + ruleFile.getName();
            kieFileSystem.write(
                ResourceFactory.newFileResource(ruleFile)
            );
            LOG.info("Loaded rule file from filesystem: {}", ruleFile.getName());
        }
    }
    
    /**
     * Starts a file watcher to dynamically reload rules when they change.
     */
    private void startRuleWatcher() {
        watcherExecutor.submit(() -> {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                Path path = Paths.get(RULES_PATH);
                
                if (!path.toFile().exists()) {
                    LOG.warn("Rules directory does not exist for watching: {}", RULES_PATH);
                    return;
                }
                
                path.register(watchService, 
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE
                );
                
                LOG.info("Started watching rules directory for changes: {}", RULES_PATH);
                
                while (!Thread.currentThread().isInterrupted()) {
                    WatchKey key = watchService.take();
                    
                    for (WatchEvent<?> event : key.pollEvents()) {
                        String fileName = event.context().toString();
                        if (fileName.endsWith(".drl")) {
                            LOG.info("Rule file changed: {}. Reloading rules...", fileName);
                            try {
                                loadRules();
                                LOG.info("Rules reloaded successfully.");
                            } catch (Exception e) {
                                LOG.error("Failed to reload rules", e);
                            }
                        }
                    }
                    
                    boolean valid = key.reset();
                    if (!valid) {
                        LOG.warn("Watch key no longer valid. Stopping rule watcher.");
                        break;
                    }
                }
            } catch (InterruptedException e) {
                LOG.info("Rule watcher interrupted.");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LOG.error("Error in rule watcher", e);
            }
        });
    }
    
    /**
     * Creates a new stateless KieSession for rule execution.
     * Note: Caller must dispose the session after use.
     *
     * @return StatelessKieSession
     */
    public StatelessKieSession createStatelessSession() {
        KieContainer container = kieContainerRef.get();
        if (container == null) {
            throw new IllegalStateException("KieContainer not initialized");
        }
        return container.newStatelessKieSession("transactionKsession");
    }
    
    /**
     * Executes rules on a transaction.
     *
     * @param transaction the transaction to evaluate
     */
    public void executeRules(Transaction transaction) {
        if (transaction == null) {
            LOG.warn("Cannot execute rules on null transaction");
            return;
        }
        
        StatelessKieSession session = createStatelessSession();
        // Set global logger
        session.setGlobal("logger", LOG);
        
        // Execute rules
        session.execute(transaction);
        
        // Note: StatelessKieSession doesn't require explicit disposal
    }
    
    /**
     * Executes rules on multiple transactions.
     *
     * @param transactions the transactions to evaluate
     */
    public void executeRules(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return;
        }
        
        StatelessKieSession session = createStatelessSession();
        session.setGlobal("logger", LOG);
        session.execute(transactions);
        // Note: StatelessKieSession doesn't require explicit disposal
    }
    
    /**
     * Shuts down the rule engine and releases resources.
     */
    public void shutdown() {
        LOG.info("Shutting down rule engine...");
        
        if (watcherExecutor != null) {
            watcherExecutor.shutdownNow();
        }
        
        KieContainer container = kieContainerRef.getAndSet(null);
        if (container != null) {
            container.dispose();
        }
        
        LOG.info("Rule engine shutdown complete.");
    }
}
