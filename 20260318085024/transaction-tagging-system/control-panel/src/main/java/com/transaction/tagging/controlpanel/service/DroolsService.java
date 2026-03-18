package com.transaction.tagging.controlpanel.service;

import com.transaction.tagging.common.entity.RuleMetadata;
import com.transaction.tagging.common.entity.Transaction;
import com.transaction.tagging.controlpanel.entity.RuleEntity;

import java.util.List;

/**
 * Service interface for Drools rule engine operations.
 */
public interface DroolsService {

    /**
     * Compile and validate a rule
     * @return true if valid, false otherwise
     */
    boolean validateRule(String ruleContent, String packageName);

    /**
     * Get validation error message for an invalid rule
     */
    String getValidationError(String ruleContent, String packageName);

    /**
     * Create a rule session with the given rules
     */
    RuleSession createSession(List<RuleMetadata> rules);

    /**
     * Execute rules against a transaction
     */
    void executeRules(RuleSession session, Transaction transaction);

    /**
     * Execute rules against multiple transactions
     */
    void executeRules(RuleSession session, List<Transaction> transactions);

    /**
     * Dispose a rule session
     */
    void disposeSession(RuleSession session);

    /**
     * Get all active rules as compiled knowledge base
     */
    CompiledRules compileRules(List<RuleMetadata> rules);

    /**
     * Rule session wrapper
     */
    interface RuleSession {
        String getSessionId();
        void fireAllRules();
        void dispose();
    }

    /**
     * Compiled rules container
     */
    interface CompiledRules {
        String getCompilationId();
        List<String> getRuleIds();
        int getRuleCount();
    }
}
