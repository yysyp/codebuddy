package com.transaction.tagging.controlpanel.service.impl;

import com.transaction.tagging.common.entity.RuleMetadata;
import com.transaction.tagging.common.entity.Tag;
import com.transaction.tagging.common.entity.Transaction;
import com.transaction.tagging.controlpanel.service.DroolsService;
import lombok.extern.slf4j.Slf4j;
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
import org.kie.api.definition.rule.Rule;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.internal.io.ResourceFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of DroolsService using Drools rule engine.
 */
@Slf4j
@Service
public class DroolsServiceImpl implements DroolsService {

    private static final String DEFAULT_PACKAGE = "com.transaction.tagging.rules";
    
    private final KieServices kieServices;
    private final Map<String, KieContainer> containerCache;
    private final Map<String, KieSession> sessionCache;

    public DroolsServiceImpl() {
        this.kieServices = KieServices.Factory.get();
        this.containerCache = new ConcurrentHashMap<>();
        this.sessionCache = new ConcurrentHashMap<>();
    }

    @Override
    public boolean validateRule(String ruleContent, String packageName) {
        try {
            String pkg = packageName != null ? packageName : DEFAULT_PACKAGE;
            String drl = wrapRuleInPackage(ruleContent, pkg);
            
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
            kieFileSystem.write("src/main/resources/rule.drl", 
                    kieServices.getResources().newByteArrayResource(drl.getBytes()));
            
            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();
            
            Results results = kieBuilder.getResults();
            if (results.hasMessages(Message.Level.ERROR)) {
                log.error("Rule validation errors: {}", results.getMessages(Message.Level.ERROR));
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error validating rule", e);
            return false;
        }
    }

    @Override
    public String getValidationError(String ruleContent, String packageName) {
        try {
            String pkg = packageName != null ? packageName : DEFAULT_PACKAGE;
            String drl = wrapRuleInPackage(ruleContent, pkg);
            
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
            kieFileSystem.write("src/main/resources/rule.drl", 
                    kieServices.getResources().newByteArrayResource(drl.getBytes()));
            
            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();
            
            Results results = kieBuilder.getResults();
            if (results.hasMessages(Message.Level.ERROR)) {
                StringBuilder sb = new StringBuilder();
                for (Message msg : results.getMessages(Message.Level.ERROR)) {
                    sb.append(msg.toString()).append("; ");
                }
                return sb.toString();
            }
            
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public RuleSession createSession(List<RuleMetadata> rules) {
        try {
            String sessionId = UUID.randomUUID().toString();
            
            // Create KieFileSystem
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
            
            // Add all rules
            int index = 0;
            for (RuleMetadata rule : rules) {
                if (rule.getRuleContent() != null && !rule.getRuleContent().isBlank()) {
                    String drl = wrapRuleInPackage(rule.getRuleContent(), rule.getPackageName());
                    String path = "src/main/resources/rule_" + rule.getRuleId() + ".drl";
                    kieFileSystem.write(path, 
                            kieServices.getResources().newByteArrayResource(drl.getBytes()));
                    index++;
                }
            }
            
            // Build
            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();
            
            Results results = kieBuilder.getResults();
            if (results.hasMessages(Message.Level.ERROR)) {
                log.error("Errors building rules: {}", results.getMessages(Message.Level.ERROR));
                throw new RuntimeException("Failed to build rules: " + 
                        results.getMessages(Message.Level.ERROR));
            }
            
            // Create container
            KieModule kieModule = kieBuilder.getKieModule();
            ReleaseId releaseId = kieModule.getReleaseId();
            KieContainer kieContainer = kieServices.newKieContainer(releaseId);
            
            // Create session
            KieSessionConfiguration sessionConfig = kieServices.newKieSessionConfiguration();
            sessionConfig.setOption(ClockTypeOption.get("realtime"));
            
            KieBaseConfiguration kieBaseConfig = kieServices.newKieBaseConfiguration();
            kieBaseConfig.setOption(EventProcessingOption.STREAM);
            
            KieBase kieBase = kieContainer.newKieBase(kieBaseConfig);
            KieSession kieSession = kieBase.newKieSession(sessionConfig, null);
            
            // Cache
            containerCache.put(sessionId, kieContainer);
            sessionCache.put(sessionId, kieSession);
            
            log.info("Created rule session {} with {} rules", sessionId, rules.size());
            
            return new DroolsRuleSession(sessionId, kieSession);
        } catch (Exception e) {
            log.error("Error creating rule session", e);
            throw new RuntimeException("Failed to create rule session", e);
        }
    }

    @Override
    public void executeRules(RuleSession session, Transaction transaction) {
        if (session instanceof DroolsRuleSession droolsSession) {
            KieSession kieSession = droolsSession.getKieSession();
            kieSession.insert(transaction);
            kieSession.fireAllRules();
        }
    }

    @Override
    public void executeRules(RuleSession session, List<Transaction> transactions) {
        if (session instanceof DroolsRuleSession droolsSession) {
            KieSession kieSession = droolsSession.getKieSession();
            for (Transaction transaction : transactions) {
                kieSession.insert(transaction);
            }
            kieSession.fireAllRules();
        }
    }

    @Override
    public void disposeSession(RuleSession session) {
        if (session instanceof DroolsRuleSession droolsSession) {
            String sessionId = droolsSession.getSessionId();
            droolsSession.dispose();
            sessionCache.remove(sessionId);
            containerCache.remove(sessionId);
            log.info("Disposed rule session {}", sessionId);
        }
    }

    @Override
    public CompiledRules compileRules(List<RuleMetadata> rules) {
        String compilationId = UUID.randomUUID().toString();
        List<String> ruleIds = new ArrayList<>();
        
        for (RuleMetadata rule : rules) {
            ruleIds.add(rule.getRuleId());
        }
        
        return new DroolsCompiledRules(compilationId, ruleIds);
    }

    private String wrapRuleInPackage(String ruleContent, String packageName) {
        // If content already has package declaration, return as-is
        if (ruleContent.trim().startsWith("package ")) {
            return ruleContent;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append("\n\n");
        sb.append("import com.transaction.tagging.common.entity.Transaction;\n");
        sb.append("import com.transaction.tagging.common.entity.Tag;\n");
        sb.append("import java.time.Instant;\n");
        sb.append("import java.math.BigDecimal;\n\n");
        sb.append("global org.slf4j.Logger log;\n\n");
        sb.append(ruleContent);
        
        return sb.toString();
    }

    /**
     * Drools-based RuleSession implementation.
     */
    private static class DroolsRuleSession implements RuleSession {
        private final String sessionId;
        private final KieSession kieSession;

        public DroolsRuleSession(String sessionId, KieSession kieSession) {
            this.sessionId = sessionId;
            this.kieSession = kieSession;
        }

        @Override
        public String getSessionId() {
            return sessionId;
        }

        @Override
        public void fireAllRules() {
            kieSession.fireAllRules();
        }

        @Override
        public void dispose() {
            kieSession.dispose();
        }

        public KieSession getKieSession() {
            return kieSession;
        }
    }

    /**
     * Compiled rules implementation.
     */
    private static class DroolsCompiledRules implements CompiledRules {
        private final String compilationId;
        private final List<String> ruleIds;

        public DroolsCompiledRules(String compilationId, List<String> ruleIds) {
            this.compilationId = compilationId;
            this.ruleIds = ruleIds;
        }

        @Override
        public String getCompilationId() {
            return compilationId;
        }

        @Override
        public List<String> getRuleIds() {
            return ruleIds;
        }

        @Override
        public int getRuleCount() {
            return ruleIds.size();
        }
    }
}
