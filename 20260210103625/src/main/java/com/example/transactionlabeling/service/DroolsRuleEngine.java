package com.example.transactionlabeling.service;

import com.example.transactionlabeling.entity.Rule;
import com.example.transactionlabeling.repository.RuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.definition.KiePackage;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Service for managing Drools rule engine and processing transactions
 */

@Service
public class DroolsRuleEngine {

    private static final Logger log = LoggerFactory.getLogger(DroolsRuleEngine.class);

    private final RuleRepository ruleRepository;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private volatile KieContainer kieContainer;
    private volatile KieBase kieBase;

    @Autowired
    public DroolsRuleEngine(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
        initializeRuleEngine();
    }

    /**
     * Initialize the rule engine with active rules from database
     */
    private void initializeRuleEngine() {
        try {
            lock.writeLock().lock();
            List<Rule> activeRules = ruleRepository.findActiveRulesOrderedByPriority();
            reloadRules(activeRules);
            log.info("Drools rule engine initialized with {} rules", activeRules.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Reload rules from database
     */
    public void reloadRules() {
        List<Rule> activeRules = ruleRepository.findActiveRulesOrderedByPriority();
        reloadRules(activeRules);
    }

    /**
     * Reload rules with provided list
     */
    private void reloadRules(List<Rule> rules) {
        try {
            lock.writeLock().lock();
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

            int ruleIndex = 0;
            for (Rule rule : rules) {
                String fileName = "rule_" + ruleIndex++ + ".drl";
                kieFileSystem.write("src/main/resources/rules/" + fileName, rule.getRuleContent());
                log.debug("Loaded rule: {}", rule.getRuleName());
            }

            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();

            if (kieBuilder.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
                throw new RuntimeException("Drools build errors: " + kieBuilder.getResults().toString());
            }

            KieModule kieModule = kieBuilder.getKieModule();
            kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
            kieBase = kieContainer.getKieBase();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Process an object against the rules and return labels
     */
    public List<String> processObject(Object fact) {
        try {
            lock.readLock().lock();
            if (kieBase == null) {
                log.warn("KieBase not initialized, reloading rules");
                initializeRuleEngine();
            }

            KieSession kieSession = kieBase.newKieSession();
            try {
                kieSession.insert(fact);
                kieSession.fireAllRules();
                return extractLabels(fact);
            } finally {
                kieSession.dispose();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Extract labels from a fact object
     */
    private List<String> extractLabels(Object fact) {
        List<String> labels = new ArrayList<>();
        // This will be implemented based on the actual rule output structure
        // For now, return empty list
        return labels;
    }

    /**
     * Get all loaded rule names
     */
    public List<String> getLoadedRuleNames() {
        List<String> ruleNames = new ArrayList<>();
        try {
            lock.readLock().lock();
            if (kieBase != null) {
                for (KiePackage kp : kieBase.getKiePackages()) {
                    kp.getRules().forEach(rule -> ruleNames.add(rule.getName()));
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        return ruleNames;
    }

    /**
     * Validate rule content
     */
    public boolean validateRuleContent(String ruleContent) {
        try {
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
            kieFileSystem.write("src/main/resources/rules/temp.drl", ruleContent);

            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();

            return !kieBuilder.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR);
        } catch (Exception e) {
            log.error("Error validating rule content", e);
            return false;
        }
    }
}
