package com.transaction.tagging.controlpanel.service;

import com.transaction.tagging.common.entity.RuleMetadata;
import com.transaction.tagging.controlpanel.dto.CreateRuleRequest;
import com.transaction.tagging.controlpanel.dto.RuleResponse;
import com.transaction.tagging.controlpanel.dto.UpdateRuleRequest;
import com.transaction.tagging.controlpanel.entity.RuleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for rule management operations.
 */
public interface RuleService {

    /**
     * Create a new rule
     */
    RuleResponse createRule(CreateRuleRequest request);

    /**
     * Get rule by ID
     */
    RuleResponse getRule(String ruleId);

    /**
     * Update an existing rule
     */
    RuleResponse updateRule(String ruleId, UpdateRuleRequest request);

    /**
     * Delete a rule
     */
    void deleteRule(String ruleId);

    /**
     * Publish a rule (change status from DRAFT to PUBLISHED)
     */
    RuleResponse publishRule(String ruleId, String publishedBy);

    /**
     * Deprecate a rule (change status from PUBLISHED to DEPRECATED)
     */
    RuleResponse deprecateRule(String ruleId);

    /**
     * Archive a rule
     */
    RuleResponse archiveRule(String ruleId);

    /**
     * Get all rules with pagination
     */
    Page<RuleResponse> getAllRules(Pageable pageable);

    /**
     * Get rules by status
     */
    Page<RuleResponse> getRulesByStatus(RuleEntity.RuleStatus status, Pageable pageable);

    /**
     * Get rules by group
     */
    List<RuleResponse> getRulesByGroup(String ruleGroup);

    /**
     * Search rules by name
     */
    Page<RuleResponse> searchRules(String name, Pageable pageable);

    /**
     * Get all active (published and enabled) rules
     */
    List<RuleResponse> getActiveRules();

    /**
     * Get all published rules as RuleMetadata for Data Panel consumption
     */
    List<RuleMetadata> getPublishedRulesForDataPanel();

    /**
     * Validate rule content (Drools syntax check)
     */
    boolean validateRuleContent(String ruleContent);

    /**
     * Get rule statistics
     */
    RuleStatistics getStatistics();

    /**
     * Statistics DTO
     */
    record RuleStatistics(
            long totalRules,
            long draftRules,
            long publishedRules,
            long deprecatedRules,
            long archivedRules,
            long activeRules
    ) {}
}
