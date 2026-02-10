package com.example.flink.transaction.rules;

import com.example.flink.transaction.model.Transaction;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Rule Engine using Drools for dynamic transaction tagging
 * Supports hot-reloading of rules at runtime
 * Thread-safe implementation for concurrent processing
 */
public class RuleEngine {

    private static final Logger LOG = LoggerFactory.getLogger(RuleEngine.class);

    private volatile KieBase knowledgeBase;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final String ruleFilePath;
    private volatile long lastRuleLoadTime = 0;

    /**
     * Constructor
     *
     * @param ruleFilePath path to the Drools rule file
     */
    public RuleEngine(String ruleFilePath) {
        this.ruleFilePath = ruleFilePath;
        loadRules();
    }

    /**
     * Constructor with default rule file path
     */
    public RuleEngine() {
        this("rules/TransactionRule.drl");
    }

    /**
     * Load rules from the rule file
     * Thread-safe with write lock
     */
    public void loadRules() {
        lock.writeLock().lock();
        try {
            LOG.info("Loading rules from: {}", ruleFilePath);

            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

            Resource resource;

            // Try to load from file system first
            File ruleFile = new File(ruleFilePath);
            if (ruleFile.exists() && ruleFile.isFile()) {
                resource = kieServices.getResources().newFileSystemResource(ruleFile);
                LOG.info("Loading rules from file system: {}", ruleFile.getAbsolutePath());
            } else {
                // Fallback to classpath
                resource = kieServices.getResources().newClassPathResource(ruleFilePath);
                LOG.info("Loading rules from classpath: {}", ruleFilePath);
            }

            kieFileSystem.write(resource);

            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();

            if (kieBuilder.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
                StringBuilder errorMsg = new StringBuilder("Rule compilation errors:\n");
                kieBuilder.getResults().getMessages().forEach(msg ->
                        errorMsg.append(msg.toString()).append("\n"));
                LOG.error(errorMsg.toString());
                throw new RuntimeException("Failed to compile rules: " + errorMsg);
            }

            // Create new knowledge base
            KieBase newKBase = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId()).getKieBase();

            // Atomic swap of knowledge base
            this.knowledgeBase = newKBase;
            this.lastRuleLoadTime = System.currentTimeMillis();

            LOG.info("Rules loaded successfully at {}", Instant.now());
        } catch (Exception e) {
            LOG.error("Error loading rules file: " + ruleFilePath, e);
            throw new RuntimeException("Failed to load rules", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Check if rules need to be reloaded
     *
     * @param reloadIntervalMs minimum interval between rule reloads in milliseconds
     * @return true if rules should be reloaded
     */
    public boolean shouldReloadRules(long reloadIntervalMs) {
        long timeSinceLastLoad = System.currentTimeMillis() - lastRuleLoadTime;
        return timeSinceLastLoad >= reloadIntervalMs;
    }

    /**
     * Apply rules to a transaction and return tagged transaction
     * Thread-safe with read lock
     *
     * @param transaction the transaction to process
     * @return the same transaction with tags applied
     */
    public Transaction applyRules(Transaction transaction) {
        if (transaction == null) {
            LOG.warn("Cannot apply rules to null transaction");
            return null;
        }

        lock.readLock().lock();
        try {
            KieSession ksession = null;
            try {
                if (knowledgeBase == null) {
                    LOG.error("Knowledge base is not initialized");
                    return transaction;
                }

                // Create new session for each transaction to ensure thread safety
                ksession = knowledgeBase.newKieSession();

                // Insert transaction into working memory
                ksession.insert(transaction);

                // Fire all rules
                int rulesFired = ksession.fireAllRules();
                LOG.debug("Fired {} rules for transaction {}", rulesFired, transaction.getTransactionId());

                return transaction;

            } catch (Exception e) {
                LOG.error("Error applying rules to transaction " + transaction.getTransactionId(), e);
                return transaction;
            } finally {
                if (ksession != null) {
                    try {
                        ksession.dispose();
                    } catch (Exception e) {
                        LOG.warn("Error disposing KIE session", e);
                    }
                }
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Apply rules to a transaction with automatic rule reload check
     *
     * @param transaction the transaction to process
     * @param reloadIntervalMs minimum interval between rule reloads
     * @return the transaction with tags applied
     */
    public Transaction applyRulesWithReloadCheck(Transaction transaction, long reloadIntervalMs) {
        // Check if we need to reload rules
        if (shouldReloadRules(reloadIntervalMs)) {
            try {
                loadRules();
                LOG.info("Rules reloaded successfully");
            } catch (Exception e) {
                LOG.warn("Failed to reload rules, using existing rules", e);
            }
        }

        return applyRules(transaction);
    }

    /**
     * Get the last rule load time
     *
     * @return timestamp of last rule load
     */
    public long getLastRuleLoadTime() {
        return lastRuleLoadTime;
    }

    /**
     * Close the rule engine and release resources
     */
    public void close() {
        lock.writeLock().lock();
        try {
            knowledgeBase = null;
            LOG.info("Rule engine closed");
        } finally {
            lock.writeLock().unlock();
        }
    }
}
