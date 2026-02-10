package com.transaction.rules.service;

import com.transaction.domain.entity.Transaction;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.drools.core.base.RuleNameEqualsAgendaFilter;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for Drools rule engine execution
 */
@Slf4j
@Service
public class RuleEngineService {

    private final KieSession kieSession;
    private final ExecutorService executorService;

    @Autowired
    public RuleEngineService(KieSession kieSession) {
        this.kieSession = kieSession;
        this.executorService = Executors.newFixedThreadPool(10, r -> {
            Thread thread = new Thread(r, "rule-engine-executor");
            thread.setDaemon(true);
            return thread;
        });

        // Add event listener for logging
        kieSession.addEventListener(new RuleAgendaEventListener());
    }

    @PostConstruct
    public void init() {
        log.info("RuleEngineService initialized");
    }

    @PreDestroy
    public void cleanup() {
        executorService.shutdown();
        kieSession.dispose();
        log.info("RuleEngineService cleaned up");
    }

    /**
     * Execute rules for a single transaction synchronously
     */
    @CircuitBreaker(name = "ruleEngineCircuitBreaker", fallbackMethod = "executeRulesFallback")
    @RateLimiter(name = "ruleEngineRateLimiter")
    @Retry(name = "ruleEngineRetry")
    public void executeRules(Transaction transaction) {
        log.debug("Executing rules for transaction: {}", transaction.getId());
        
        synchronized (kieSession) {
            try {
                kieSession.insert(transaction);
                kieSession.fireAllRules();
                
                log.debug("Rules executed successfully for transaction: {}", transaction.getId());
                log.debug("Transaction tags: {}, risk score: {}", 
                         transaction.getTags(), transaction.getRiskScore());
            } catch (Exception e) {
                log.error("Error executing rules for transaction: {}", transaction.getId(), e);
                throw e;
            } finally {
                kieSession.dispose();
            }
        }
    }

    /**
     * Execute rules for multiple transactions asynchronously
     */
    @TimeLimiter(name = "ruleEngineTimeLimiter")
    public CompletableFuture<Void> executeRulesAsync(List<Transaction> transactions) {
        return CompletableFuture.runAsync(() -> {
            log.info("Executing rules for {} transactions asynchronously", transactions.size());
            
            for (Transaction transaction : transactions) {
                try {
                    executeRules(transaction);
                } catch (Exception e) {
                    log.error("Error executing rules for transaction: {}", transaction.getId(), e);
                }
            }
            
            log.info("Completed rule execution for {} transactions", transactions.size());
        }, executorService);
    }

    /**
     * Execute rules with specific rule name filter
     */
    public void executeRules(Transaction transaction, String ruleName) {
        log.debug("Executing specific rule '{}' for transaction: {}", ruleName, transaction.getId());
        
        synchronized (kieSession) {
            try {
                kieSession.insert(transaction);
                kieSession.fireAllRules(new RuleNameEqualsAgendaFilter(ruleName));
            } catch (Exception e) {
                log.error("Error executing rule '{}' for transaction: {}", ruleName, transaction.getId(), e);
                throw e;
            } finally {
                kieSession.dispose();
            }
        }
    }

    /**
     * Fallback method for circuit breaker
     */
    private void executeRulesFallback(Transaction transaction, Exception e) {
        log.error("Rule engine circuit breaker triggered for transaction: {}", transaction.getId(), e);
        // Set default values as fallback
        transaction.setTags("HIGH_RISK");
        transaction.setRiskScore(new java.math.BigDecimal("100.00"));
    }

    /**
     * Custom agenda event listener for logging rule execution
     */
    private static class RuleAgendaEventListener extends DefaultAgendaEventListener {
        @Override
        public void afterMatchFired(AfterMatchFiredEvent event) {
            String ruleName = event.getMatch().getRule().getName();
            log.debug("Rule fired: {}", ruleName);
        }
    }
}
