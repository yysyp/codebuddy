package com.etl.data.service;

import com.etl.data.dto.RuleDefinitionDto;
import com.etl.data.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Drools Rule Service
 * Handles rule loading and execution using Drools engine
 */
@Slf4j
@Service
public class DroolsRuleService {

    private final Map<String, KieContainer> kieContainerCache = new ConcurrentHashMap<>();
    
    /**
     * Build KieContainer from rule definitions
     */
    public KieContainer buildKieContainer(List<RuleDefinitionDto> rules) {
        log.info("Building KieContainer with {} rules", rules.size());
        
        try {
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
            
            // Add each rule to the file system
            int index = 0;
            for (RuleDefinitionDto rule : rules) {
                String fileName = "src/main/resources/rules/rule_" + index + "_" + 
                        rule.getName().replaceAll("[^a-zA-Z0-9]", "_") + ".drl";
                
                log.debug("Adding rule: {} from file: {}", rule.getName(), fileName);
                
                Resource resource = ResourceFactory.newByteArrayResource(
                        rule.getRuleContent().getBytes(StandardCharsets.UTF_8));
                kieFileSystem.write(fileName, resource);
                index++;
            }
            
            // Build the KieContainer
            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();
            
            if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
                List<Message> errors = kieBuilder.getResults().getMessages(Message.Level.ERROR);
                StringBuilder errorMsg = new StringBuilder("Failed to build rules: ");
                for (Message error : errors) {
                    errorMsg.append(error.getText()).append("; ");
                }
                log.error("Rule build errors: {}", errorMsg);
                throw new RuntimeException(errorMsg.toString());
            }
            
            KieRepository kieRepository = kieServices.getRepository();
            KieContainer kieContainer = kieServices.newKieContainer(kieRepository.getDefaultReleaseId());
            
            log.info("KieContainer built successfully");
            return kieContainer;
        } catch (Exception e) {
            log.error("Error building KieContainer", e);
            throw new RuntimeException("Failed to build rules: " + e.getMessage(), e);
        }
    }
    
    /**
     * Apply rules to a transaction
     */
    public Transaction applyRules(Transaction transaction, KieContainer kieContainer) {
        KieSession kieSession = null;
        try {
            kieSession = kieContainer.newKieSession();
            
            // Insert transaction into session
            kieSession.insert(transaction);
            
            // Fire all rules
            int rulesFired = kieSession.fireAllRules();
            
            log.debug("Fired {} rules for transaction: {}", rulesFired, transaction.getTransactionId());
            
            return transaction;
        } catch (Exception e) {
            log.error("Error applying rules to transaction: {}", transaction.getTransactionId(), e);
            throw new RuntimeException("Failed to apply rules: " + e.getMessage(), e);
        } finally {
            if (kieSession != null) {
                kieSession.dispose();
            }
        }
    }
    
    /**
     * Get or create cached KieContainer
     */
    public KieContainer getOrCreateKieContainer(String cacheKey, List<RuleDefinitionDto> rules) {
        return kieContainerCache.computeIfAbsent(cacheKey, key -> buildKieContainer(rules));
    }
    
    /**
     * Clear cached KieContainer
     */
    public void clearCache(String cacheKey) {
        kieContainerCache.remove(cacheKey);
        log.info("Cleared KieContainer cache for key: {}", cacheKey);
    }
    
    /**
     * Clear all cached KieContainers
     */
    public void clearAllCache() {
        kieContainerCache.clear();
        log.info("Cleared all KieContainer cache");
    }
}
