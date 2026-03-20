package com.etl.control.service;

import com.etl.control.entity.RuleDefinition;
import com.etl.control.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Drools Service
 * Handles Drools rule engine operations
 */
@Slf4j
@Service
public class DroolsService {

    /**
     * Validate a Drools rule
     */
    public void validateRule(String ruleContent) {
        log.info("Validating Drools rule content");
        
        try {
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
            
            // Write rule to file system
            kieFileSystem.write("src/main/resources/rules/validation.drl", 
                    ResourceFactory.newByteArrayResource(ruleContent.getBytes(StandardCharsets.UTF_8)));
            
            // Build and validate
            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();
            
            if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
                List<Message> errors = kieBuilder.getResults().getMessages(Message.Level.ERROR);
                StringBuilder errorMsg = new StringBuilder("Rule validation failed: ");
                for (Message error : errors) {
                    errorMsg.append(error.getText()).append("; ");
                }
                throw new BusinessException("RULE_VALIDATION_ERROR", errorMsg.toString());
            }
            
            log.info("Rule validation successful");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validating rule", e);
            throw new BusinessException("RULE_VALIDATION_ERROR", 
                    "Failed to validate rule: " + e.getMessage(), e);
        }
    }

    /**
     * Build KieContainer from rule definitions
     */
    public KieContainer buildKieContainer(List<RuleDefinition> rules) {
        log.info("Building KieContainer with {} rules", rules.size());
        
        try {
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
            
            // Add each rule to the file system
            int index = 0;
            for (RuleDefinition rule : rules) {
                String fileName = "src/main/resources/rules/rule_" + index + "_" + 
                        rule.getName().replaceAll("[^a-zA-Z0-9]", "_") + ".drl";
                kieFileSystem.write(fileName, 
                        ResourceFactory.newByteArrayResource(
                                rule.getRuleContent().getBytes(StandardCharsets.UTF_8)));
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
                throw new BusinessException("RULE_BUILD_ERROR", errorMsg.toString());
            }
            
            KieRepository kieRepository = kieServices.getRepository();
            return kieServices.newKieContainer(kieRepository.getDefaultReleaseId());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error building KieContainer", e);
            throw new BusinessException("RULE_BUILD_ERROR", 
                    "Failed to build rules: " + e.getMessage(), e);
        }
    }

    /**
     * Create a new KieSession from rules
     */
    public KieSession createKieSession(List<RuleDefinition> rules) {
        KieContainer kieContainer = buildKieContainer(rules);
        return kieContainer.newKieSession();
    }

    /**
     * Convert decision table (Excel) to DRL
     * Note: This method is deprecated in Drools 8.x and may not work as expected
     */
    @Deprecated
    public String convertDecisionTableToDrl(byte[] excelContent) {
        log.info("Converting decision table to DRL");
        log.warn("Decision table conversion is deprecated in Drools 8.x");
        
        try {
            // In Drools 8.x, decision table compilation has changed significantly
            // This method is kept for backward compatibility but may need updates
            throw new BusinessException("DECISION_TABLE_DEPRECATED", 
                    "Decision table conversion is deprecated. Please use DRL rules directly.");
        } catch (Exception e) {
            log.error("Error converting decision table", e);
            throw new BusinessException("DECISION_TABLE_ERROR", 
                    "Failed to convert decision table: " + e.getMessage(), e);
        }
    }
}
