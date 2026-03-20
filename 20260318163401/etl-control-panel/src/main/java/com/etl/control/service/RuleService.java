package com.etl.control.service;

import com.etl.control.dto.RuleRequest;
import com.etl.control.dto.RuleResponse;
import com.etl.control.entity.RuleDefinition;
import com.etl.control.entity.RuleDefinition.RuleStatus;
import com.etl.control.exception.BusinessException;
import com.etl.control.repository.RuleDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Rule Service
 * Handles rule definition operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleDefinitionRepository ruleRepository;
    private final DroolsService droolsService;

    /**
     * Create a new rule
     */
    @Transactional
    public RuleResponse createRule(RuleRequest request, String username) {
        log.info("Creating new rule: {}", request.getName());

        // Check if rule name already exists
        if (ruleRepository.existsByName(request.getName())) {
            throw new BusinessException("RULE_EXISTS", 
                    "Rule with name '" + request.getName() + "' already exists");
        }

        // Validate rule content
        droolsService.validateRule(request.getRuleContent());

        // Create rule entity
        RuleDefinition rule = RuleDefinition.builder()
                .name(request.getName())
                .description(request.getDescription())
                .ruleContent(request.getRuleContent())
                .version(1)
                .status(RuleStatus.DRAFT)
                .ruleType(request.getRuleType())
                .targetType(request.getTargetType())
                .priority(request.getPriority())
                .tags(request.getTags())
                .createdBy(username)
                .updatedBy(username)
                .build();

        rule = ruleRepository.save(rule);
        log.info("Rule created successfully with ID: {}", rule.getId());

        return RuleResponse.fromEntity(rule);
    }

    /**
     * Update an existing rule
     */
    @Transactional
    public RuleResponse updateRule(Long id, RuleRequest request, String username) {
        log.info("Updating rule with ID: {}", id);

        RuleDefinition rule = ruleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("RULE_NOT_FOUND", 
                        "Rule not found with ID: " + id));

        // Validate rule content
        droolsService.validateRule(request.getRuleContent());

        // Update rule
        rule.setDescription(request.getDescription());
        rule.setRuleContent(request.getRuleContent());
        rule.setRuleType(request.getRuleType());
        rule.setTargetType(request.getTargetType());
        rule.setPriority(request.getPriority());
        rule.setTags(request.getTags());
        rule.setUpdatedBy(username);

        // Increment version
        rule.setVersion(rule.getVersion() + 1);

        rule = ruleRepository.save(rule);
        log.info("Rule updated successfully: {}", rule.getId());

        return RuleResponse.fromEntity(rule);
    }

    /**
     * Publish a rule
     */
    @Transactional
    public RuleResponse publishRule(Long id, String username) {
        log.info("Publishing rule with ID: {}", id);

        RuleDefinition rule = ruleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("RULE_NOT_FOUND", 
                        "Rule not found with ID: " + id));

        // Validate rule before publishing
        droolsService.validateRule(rule.getRuleContent());

        rule.setStatus(RuleStatus.PUBLISHED);
        rule.setUpdatedBy(username);
        rule = ruleRepository.save(rule);

        log.info("Rule published successfully: {}", rule.getId());
        return RuleResponse.fromEntity(rule);
    }

    /**
     * Deprecate a rule
     */
    @Transactional
    public RuleResponse deprecateRule(Long id, String username) {
        log.info("Deprecating rule with ID: {}", id);

        RuleDefinition rule = ruleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("RULE_NOT_FOUND", 
                        "Rule not found with ID: " + id));

        rule.setStatus(RuleStatus.DEPRECATED);
        rule.setUpdatedBy(username);
        rule = ruleRepository.save(rule);

        log.info("Rule deprecated successfully: {}", rule.getId());
        return RuleResponse.fromEntity(rule);
    }

    /**
     * Get rule by ID
     */
    public RuleResponse getRuleById(Long id) {
        RuleDefinition rule = ruleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("RULE_NOT_FOUND", 
                        "Rule not found with ID: " + id));
        return RuleResponse.fromEntity(rule);
    }

    /**
     * Get rule by name
     */
    public RuleResponse getRuleByName(String name) {
        RuleDefinition rule = ruleRepository.findByName(name)
                .orElseThrow(() -> new BusinessException("RULE_NOT_FOUND", 
                        "Rule not found with name: " + name));
        return RuleResponse.fromEntity(rule);
    }

    /**
     * Get all rules with pagination
     */
    public Page<RuleResponse> getAllRules(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<RuleDefinition> rules = ruleRepository.findAll(pageable);
        return rules.map(RuleResponse::fromEntity);
    }

    /**
     * Get rules by status
     */
    public Page<RuleResponse> getRulesByStatus(RuleStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, 
                Sort.by("priority").descending().and(Sort.by("updatedAt").descending()));
        
        Page<RuleDefinition> rules = ruleRepository.findByStatus(status, pageable);
        return rules.map(RuleResponse::fromEntity);
    }

    /**
     * Get all published rules
     */
    public List<RuleResponse> getAllPublishedRules() {
        List<RuleDefinition> rules = ruleRepository.findAllPublishedRules();
        return rules.stream()
                .map(RuleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Delete a rule (archive)
     */
    @Transactional
    public void deleteRule(Long id, String username) {
        log.info("Archiving rule with ID: {}", id);

        RuleDefinition rule = ruleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("RULE_NOT_FOUND", 
                        "Rule not found with ID: " + id));

        rule.setStatus(RuleStatus.ARCHIVED);
        rule.setUpdatedBy(username);
        ruleRepository.save(rule);

        log.info("Rule archived successfully: {}", id);
    }
}
