package com.example.transactionlabeling.service;

import com.example.transactionlabeling.dto.RuleRequest;
import com.example.transactionlabeling.dto.RuleResponse;
import com.example.transactionlabeling.entity.Rule;
import com.example.transactionlabeling.repository.RuleRepository;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing Drools rules
 */

@Service
@RequiredArgsConstructor
public class RuleManagementService {

    private static final Logger log = LoggerFactory.getLogger(RuleManagementService.class);

    private final RuleRepository ruleRepository;
    private final DroolsRuleEngine droolsRuleEngine;

    /**
     * Create a new rule
     */
    @Transactional
    public RuleResponse createRule(RuleRequest request) {
        log.info("Creating rule: {}", request.getRuleName());

        // Validate rule content
        boolean isValid = droolsRuleEngine.validateRuleContent(request.getRuleContent());
        if (!isValid) {
            throw new RuntimeException("Invalid rule content. Please check the rule syntax.");
        }

        Rule rule = mapToEntity(request);
        Rule saved = ruleRepository.save(rule);

        // Reload rules in the engine
        droolsRuleEngine.reloadRules();

        log.info("Rule created with ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    /**
     * Get rule by ID
     */
    @Transactional(readOnly = true)
    public RuleResponse getRuleById(Long id) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found with id: " + id));
        return mapToResponse(rule);
    }

    /**
     * Get rule by rule name
     */
    @Transactional(readOnly = true)
    public RuleResponse getRuleByRuleName(String ruleName) {
        Rule rule = ruleRepository.findByRuleName(ruleName)
                .orElseThrow(() -> new RuntimeException("Rule not found with ruleName: " + ruleName));
        return mapToResponse(rule);
    }

    /**
     * Get all rules with pagination
     */
    @Transactional(readOnly = true)
    public Page<RuleResponse> getAllRules(Pageable pageable) {
        Page<Rule> rules = ruleRepository.findAll(pageable);
        return rules.map(this::mapToResponse);
    }

    /**
     * Get active rules
     */
    @Transactional(readOnly = true)
    public List<RuleResponse> getActiveRules() {
        List<Rule> rules = ruleRepository.findByActive(true);
        return rules.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get rules by category
     */
    @Transactional(readOnly = true)
    public List<RuleResponse> getRulesByCategory(String category) {
        List<Rule> rules = ruleRepository.findByRuleCategory(category);
        return rules.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing rule
     */
    @Transactional
    public RuleResponse updateRule(Long id, RuleRequest request) {
        log.info("Updating rule with ID: {}", id);

        Rule existingRule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found with id: " + id));

        // Validate rule content
        boolean isValid = droolsRuleEngine.validateRuleContent(request.getRuleContent());
        if (!isValid) {
            throw new RuntimeException("Invalid rule content. Please check the rule syntax.");
        }

        updateEntityFromRequest(existingRule, request);
        Rule updated = ruleRepository.save(existingRule);

        // Reload rules in the engine
        droolsRuleEngine.reloadRules();

        log.info("Rule updated: {}", id);
        return mapToResponse(updated);
    }

    /**
     * Delete rule by ID
     */
    @Transactional
    public void deleteRule(Long id) {
        log.info("Deleting rule with ID: {}", id);
        ruleRepository.deleteById(id);

        // Reload rules in the engine
        droolsRuleEngine.reloadRules();
    }

    /**
     * Activate rule
     */
    @Transactional
    public RuleResponse activateRule(Long id) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found with id: " + id));
        rule.setActive(true);
        Rule updated = ruleRepository.save(rule);

        // Reload rules in the engine
        droolsRuleEngine.reloadRules();

        log.info("Rule activated: {}", id);
        return mapToResponse(updated);
    }

    /**
     * Deactivate rule
     */
    @Transactional
    public RuleResponse deactivateRule(Long id) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found with id: " + id));
        rule.setActive(false);
        Rule updated = ruleRepository.save(rule);

        // Reload rules in the engine
        droolsRuleEngine.reloadRules();

        log.info("Rule deactivated: {}", id);
        return mapToResponse(updated);
    }

    /**
     * Reload all rules in the engine
     */
    public void reloadAllRules() {
        log.info("Reloading all rules in the Drools engine");
        droolsRuleEngine.reloadRules();
    }

    /**
     * Get loaded rule names from the engine
     */
    public List<String> getLoadedRuleNames() {
        return droolsRuleEngine.getLoadedRuleNames();
    }

    /**
     * Validate rule content without saving
     */
    public boolean validateRule(String ruleContent) {
        return droolsRuleEngine.validateRuleContent(ruleContent);
    }

    private Rule mapToEntity(RuleRequest request) {
        return Rule.builder()
                .ruleName(request.getRuleName())
                .ruleContent(request.getRuleContent())
                .ruleCategory(request.getRuleCategory())
                .priority(request.getPriority())
                .active(request.getActive() != null ? request.getActive() : true)
                .description(request.getDescription())
                .build();
    }

    private void updateEntityFromRequest(Rule rule, RuleRequest request) {
        rule.setRuleName(request.getRuleName());
        rule.setRuleContent(request.getRuleContent());
        rule.setRuleCategory(request.getRuleCategory());
        rule.setPriority(request.getPriority());
        rule.setActive(request.getActive());
        rule.setDescription(request.getDescription());
    }

    private RuleResponse mapToResponse(Rule entity) {
        return RuleResponse.builder()
                .id(entity.getId())
                .ruleName(entity.getRuleName())
                .ruleContent(entity.getRuleContent())
                .ruleCategory(entity.getRuleCategory())
                .priority(entity.getPriority())
                .active(entity.getActive())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
