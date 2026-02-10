package com.example.transaction.drools;

import com.example.transaction.model.Transaction;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.io.Resource;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class DroolsRuleEngine {
    private static final Logger logger = Logger.getLogger(DroolsRuleEngine.class.getName());
    
    private KieContainer kieContainer;
    private final ReentrantLock lock = new ReentrantLock();
    
    public DroolsRuleEngine(String rulesContent) {
        loadRules(rulesContent);
    }
    
    private void loadRules(String rulesContent) {
        lock.lock();
        try {
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
            
            Resource resource = kieServices.getResources()
                .newByteArrayResource(rulesContent.getBytes())
                .setSourcePath("transaction-rules.drl");
            
            kieFileSystem.write(resource);
            
            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();
            
            if (kieBuilder.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
                throw new RuntimeException("Drools compilation errors: " + 
                    kieBuilder.getResults().toString());
            }
            
            KieModule kieModule = kieBuilder.getKieModule();
            kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
            
            logger.info("Drools rules loaded successfully");
        } finally {
            lock.unlock();
        }
    }
    
    public Transaction applyRules(Transaction transaction) {
        lock.lock();
        try {
            KieSession kieSession = kieContainer.newKieSession();
            
            kieSession.addEventListener(new org.kie.api.event.rule.RuleRuntimeEventListener() {
                @Override
                public void objectInserted(org.kie.api.event.rule.ObjectInsertedEvent event) {
                    logger.fine("Object inserted: " + event.getObject());
                }
                
                @Override
                public void objectUpdated(org.kie.api.event.rule.ObjectUpdatedEvent event) {
                    logger.fine("Object updated: " + event.getObject());
                }
                
                @Override
                public void objectDeleted(org.kie.api.event.rule.ObjectDeletedEvent event) {
                    logger.fine("Object deleted: " + event.getOldObject());
                }
            });
            
            kieSession.insert(transaction);
            int ruleCount = kieSession.fireAllRules();
            
            kieSession.dispose();
            
            logger.fine("Executed " + ruleCount + " rules for transaction " + transaction.getTransactionId());
            return transaction;
        } finally {
            lock.unlock();
        }
    }
    
    public void applyRules(List<Transaction> transactions) {
        lock.lock();
        try {
            KieSession kieSession = kieContainer.newKieSession();
            
            for (Transaction tx : transactions) {
                kieSession.insert(tx);
            }
            
            int ruleCount = kieSession.fireAllRules();
            kieSession.dispose();
            
            logger.info("Executed " + ruleCount + " rules for " + transactions.size() + " transactions");
        } finally {
            lock.unlock();
        }
    }
}
