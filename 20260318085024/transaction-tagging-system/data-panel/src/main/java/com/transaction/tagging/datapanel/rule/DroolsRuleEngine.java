package com.transaction.tagging.datapanel.rule;

import com.transaction.tagging.common.entity.RuleMetadata;
import com.transaction.tagging.common.entity.Tag;
import com.transaction.tagging.common.entity.Transaction;
import com.transaction.tagging.datapanel.config.DataPanelConfig;
import org.drools.core.base.RuleNameEqualsAgendaFilter;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Drools-based rule engine for applying tags to transactions.
 */
public class DroolsRuleEngine {

    private static final Logger LOG = LoggerFactory.getLogger(DroolsRuleEngine.class);
    private static final String DEFAULT_PACKAGE = "com.transaction.tagging.rules";

    private final DataPanelConfig config;
    private final KieServices kieServices;
    private volatile KieContainer kieContainer;
    private volatile String currentRulesVersion;

    public DroolsRuleEngine(DataPanelConfig config) {
        this.config = config;
        this.kieServices = KieServices.Factory.get();
    }

    /**
     * Initialize or update the rule engine with new rules.
     */
    public synchronized void updateRules(List<RuleMetadata> rules) {
        if (rules == null || rules.isEmpty()) {
            LOG.warn("No rules provided, skipping update");
            return;
        }

        LOG.info("Updating rules, count: {}", rules.size());

        try {
            // Create KieFileSystem
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

            // Add all rules
            for (RuleMetadata rule : rules) {
                if (rule.getRuleContent() != null && !rule.getRuleContent().isBlank()) {
                    String drl = wrapRuleInPackage(rule);
                    String path = "src/main/resources/rules/" + rule.getRuleId() + ".drl";
                    kieFileSystem.write(path, 
                            kieServices.getResources().newByteArrayResource(drl.getBytes()));
                }
            }

            // Build
            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();

            Results results = kieBuilder.getResults();
            if (results.hasMessages(Message.Level.ERROR)) {
                LOG.error("Errors building rules: {}", results.getMessages(Message.Level.ERROR));
                return;
            }

            // Create container
            KieModule kieModule = kieBuilder.getKieModule();
            ReleaseId releaseId = kieModule.getReleaseId();
            KieContainer newContainer = kieServices.newKieContainer(releaseId);

            // Swap containers
            KieContainer oldContainer = this.kieContainer;
            this.kieContainer = newContainer;
            this.currentRulesVersion = UUID.randomUUID().toString();

            // Dispose old container
            if (oldContainer != null) {
                oldContainer.dispose();
            }

            LOG.info("Rules updated successfully, version: {}", currentRulesVersion);
        } catch (Exception e) {
            LOG.error("Error updating rules", e);
        }
    }

    /**
     * Apply rules to a transaction and add tags.
     */
    public void applyRules(Transaction transaction) {
        if (kieContainer == null) {
            LOG.warn("Rule engine not initialized, skipping transaction: {}", transaction.getTransactionId());
            return;
        }

        KieSession kieSession = null;
        try {
            // Create session
            KieSessionConfiguration sessionConfig = kieServices.newKieSessionConfiguration();
            sessionConfig.setOption(ClockTypeOption.get("realtime"));

            KieBaseConfiguration kieBaseConfig = kieServices.newKieBaseConfiguration();
            kieBaseConfig.setOption(EventProcessingOption.STREAM);

            KieBase kieBase = kieContainer.newKieBase(kieBaseConfig);
            kieSession = kieBase.newKieSession(sessionConfig, null);

            // Set global variables
            kieSession.setGlobal("log", LOG);

            // Insert transaction
            kieSession.insert(transaction);

            // Fire all rules
            int firedRules = kieSession.fireAllRules();
            
            LOG.debug("Fired {} rules for transaction {}", firedRules, transaction.getTransactionId());
        } catch (Exception e) {
            LOG.error("Error applying rules to transaction: {}", transaction.getTransactionId(), e);
        } finally {
            if (kieSession != null) {
                kieSession.dispose();
            }
        }
    }

    /**
     * Apply rules to multiple transactions.
     */
    public void applyRules(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            applyRules(transaction);
        }
    }

    /**
     * Get current rules version.
     */
    public String getCurrentRulesVersion() {
        return currentRulesVersion;
    }

    /**
     * Check if rules are loaded.
     */
    public boolean hasRules() {
        return kieContainer != null;
    }

    /**
     * Dispose the rule engine.
     */
    public synchronized void dispose() {
        if (kieContainer != null) {
            kieContainer.dispose();
            kieContainer = null;
        }
    }

    private String wrapRuleInPackage(RuleMetadata rule) {
        String content = rule.getRuleContent();
        String packageName = rule.getPackageName() != null ? 
                rule.getPackageName() : DEFAULT_PACKAGE;

        // If content already has package declaration, return as-is
        if (content.trim().startsWith("package ")) {
            return content;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append("\n\n");
        sb.append("import com.transaction.tagging.common.entity.Transaction;\n");
        sb.append("import com.transaction.tagging.common.entity.Tag;\n");
        sb.append("import java.time.Instant;\n");
        sb.append("import java.math.BigDecimal;\n\n");
        sb.append("global org.slf4j.Logger log;\n\n");
        sb.append("// Rule: ").append(rule.getName()).append("\n");
        sb.append("// ID: ").append(rule.getRuleId()).append("\n");
        sb.append("// Tag: ").append(rule.getTagCode()).append("\n\n");
        sb.append(content);

        return sb.toString();
    }
}
