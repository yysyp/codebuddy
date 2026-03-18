package com.transaction.tagging.controlpanel.service.impl;

import com.transaction.tagging.common.entity.RuleMetadata;
import com.transaction.tagging.common.exception.BusinessException;
import com.transaction.tagging.common.exception.ErrorCode;
import com.transaction.tagging.controlpanel.dto.CreateRuleRequest;
import com.transaction.tagging.controlpanel.dto.RuleResponse;
import com.transaction.tagging.controlpanel.dto.UpdateRuleRequest;
import com.transaction.tagging.controlpanel.entity.RuleEntity;
import com.transaction.tagging.controlpanel.repository.RuleRepository;
import com.transaction.tagging.controlpanel.service.DroolsService;
import com.transaction.tagging.controlpanel.service.RuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of RuleService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleServiceImpl implements RuleService {

    private final RuleRepository ruleRepository;
    private final DroolsService droolsService;

    @Override
    @Transactional
    public RuleResponse createRule(CreateRuleRequest request) {
        log.info("Creating rule with ID: {}", request.getRuleId());

        // Check if rule already exists
        if (ruleRepository.existsByRuleId(request.getRuleId())) {
            throw ErrorCode.RULE_ALREADY_EXISTS.toException("Rule already exists with ID: " + request.getRuleId());
        }

        // Validate rule content if provided
        if (request.getRuleContent() != null && !request.getRuleContent().isBlank()) {
            if (!droolsService.validateRule(request.getRuleContent(), request.getPackageName())) {
                String error = droolsService.getValidationError(request.getRuleContent(), request.getPackageName());
                throw ErrorCode.RULE_COMPILATION_ERROR.toException("Rule validation failed: " + error);
            }
        }

        // Create entity
        RuleEntity entity = RuleEntity.builder()
                .ruleId(request.getRuleId())
                .name(request.getName())
                .description(request.getDescription())
                .ruleType(request.getRuleType())
                .version(request.getVersion())
                .status(RuleEntity.RuleStatus.DRAFT)
                .ruleContent(request.getRuleContent())
                .packageName(request.getPackageName())
                .ruleGroup(request.getRuleGroup())
                .priority(request.getPriority())
                .enabled(request.isEnabled())
                .tagCode(request.getTagCode())
                .tagName(request.getTagName())
                .tagCategory(request.getTagCategory())
                .tagSeverity(request.getTagSeverity())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .createdBy(request.getCreatedBy() != null ? request.getCreatedBy() : "system")
                .build();

        entity = ruleRepository.save(entity);
        log.info("Rule created successfully: {}", entity.getRuleId());

        return toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public RuleResponse getRule(String ruleId) {
        RuleEntity entity = findRuleById(ruleId);
        return toResponse(entity);
    }

    @Override
    @Transactional
    public RuleResponse updateRule(String ruleId, UpdateRuleRequest request) {
        log.info("Updating rule: {}", ruleId);

        RuleEntity entity = findRuleById(ruleId);

        // Only draft rules can be updated
        if (entity.getStatus() != RuleEntity.RuleStatus.DRAFT) {
            throw ErrorCode.RULE_INVALID_STATUS.toException(
                    "Only DRAFT rules can be updated. Current status: " + entity.getStatus());
        }

        // Validate new rule content if provided
        if (request.getRuleContent() != null && !request.getRuleContent().isBlank()) {
            String packageName = request.getPackageName() != null ? 
                    request.getPackageName() : entity.getPackageName();
            if (!droolsService.validateRule(request.getRuleContent(), packageName)) {
                String error = droolsService.getValidationError(request.getRuleContent(), packageName);
                throw ErrorCode.RULE_COMPILATION_ERROR.toException("Rule validation failed: " + error);
            }
        }

        // Update fields
        if (request.getName() != null) entity.setName(request.getName());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getRuleContent() != null) entity.setRuleContent(request.getRuleContent());
        if (request.getPackageName() != null) entity.setPackageName(request.getPackageName());
        if (request.getRuleGroup() != null) entity.setRuleGroup(request.getRuleGroup());
        if (request.getPriority() != null) entity.setPriority(request.getPriority());
        if (request.getEnabled() != null) entity.setEnabled(request.getEnabled());
        if (request.getTagCode() != null) entity.setTagCode(request.getTagCode());
        if (request.getTagName() != null) entity.setTagName(request.getTagName());
        if (request.getTagCategory() != null) entity.setTagCategory(request.getTagCategory());
        if (request.getTagSeverity() != null) entity.setTagSeverity(request.getTagSeverity());
        if (request.getEffectiveFrom() != null) entity.setEffectiveFrom(request.getEffectiveFrom());
        if (request.getEffectiveTo() != null) entity.setEffectiveTo(request.getEffectiveTo());
        if (request.getUpdatedBy() != null) entity.setUpdatedBy(request.getUpdatedBy());

        entity = ruleRepository.save(entity);
        log.info("Rule updated successfully: {}", entity.getRuleId());

        return toResponse(entity);
    }

    @Override
    @Transactional
    public void deleteRule(String ruleId) {
        log.info("Deleting rule: {}", ruleId);

        RuleEntity entity = findRuleById(ruleId);

        // Only draft or archived rules can be deleted
        if (entity.getStatus() != RuleEntity.RuleStatus.DRAFT && 
            entity.getStatus() != RuleEntity.RuleStatus.ARCHIVED) {
            throw ErrorCode.RULE_INVALID_STATUS.toException(
                    "Only DRAFT or ARCHIVED rules can be deleted. Current status: " + entity.getStatus());
        }

        ruleRepository.deleteByRuleId(ruleId);
        log.info("Rule deleted successfully: {}", ruleId);
    }

    @Override
    @Transactional
    public RuleResponse publishRule(String ruleId, String publishedBy) {
        log.info("Publishing rule: {}", ruleId);

        RuleEntity entity = findRuleById(ruleId);

        // Only draft rules can be published
        if (entity.getStatus() != RuleEntity.RuleStatus.DRAFT) {
            throw ErrorCode.RULE_INVALID_STATUS.toException(
                    "Only DRAFT rules can be published. Current status: " + entity.getStatus());
        }

        // Validate rule content before publishing
        if (entity.getRuleContent() == null || entity.getRuleContent().isBlank()) {
            throw ErrorCode.RULE_COMPILATION_ERROR.toException("Cannot publish rule without content");
        }

        if (!droolsService.validateRule(entity.getRuleContent(), entity.getPackageName())) {
            String error = droolsService.getValidationError(entity.getRuleContent(), entity.getPackageName());
            throw ErrorCode.RULE_COMPILATION_ERROR.toException("Rule validation failed: " + error);
        }

        entity.setStatus(RuleEntity.RuleStatus.PUBLISHED);
        entity.setPublishedAt(Instant.now());
        entity.setPublishedBy(publishedBy != null ? publishedBy : "system");

        entity = ruleRepository.save(entity);
        log.info("Rule published successfully: {}", entity.getRuleId());

        return toResponse(entity);
    }

    @Override
    @Transactional
    public RuleResponse deprecateRule(String ruleId) {
        log.info("Deprecating rule: {}", ruleId);

        RuleEntity entity = findRuleById(ruleId);

        // Only published rules can be deprecated
        if (entity.getStatus() != RuleEntity.RuleStatus.PUBLISHED) {
            throw ErrorCode.RULE_INVALID_STATUS.toException(
                    "Only PUBLISHED rules can be deprecated. Current status: " + entity.getStatus());
        }

        entity.setStatus(RuleEntity.RuleStatus.DEPRECATED);
        entity = ruleRepository.save(entity);

        log.info("Rule deprecated successfully: {}", entity.getRuleId());
        return toResponse(entity);
    }

    @Override
    @Transactional
    public RuleResponse archiveRule(String ruleId) {
        log.info("Archiving rule: {}", ruleId);

        RuleEntity entity = findRuleById(ruleId);

        entity.setStatus(RuleEntity.RuleStatus.ARCHIVED);
        entity.setEnabled(false);
        entity = ruleRepository.save(entity);

        log.info("Rule archived successfully: {}", entity.getRuleId());
        return toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RuleResponse> getAllRules(Pageable pageable) {
        return ruleRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RuleResponse> getRulesByStatus(RuleEntity.RuleStatus status, Pageable pageable) {
        return ruleRepository.findByStatus(status, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RuleResponse> getRulesByGroup(String ruleGroup) {
        return ruleRepository.findByRuleGroup(ruleGroup).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RuleResponse> searchRules(String name, Pageable pageable) {
        return ruleRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RuleResponse> getActiveRules() {
        return ruleRepository.findAllActiveRules().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RuleMetadata> getPublishedRulesForDataPanel() {
        return ruleRepository.findAllPublishedOrderByPriority().stream()
                .map(this::toRuleMetadata)
                .collect(Collectors.toList());
    }

    @Override
    public boolean validateRuleContent(String ruleContent) {
        return droolsService.validateRule(ruleContent, "com.transaction.tagging.rules");
    }

    @Override
    @Transactional(readOnly = true)
    public RuleStatistics getStatistics() {
        return new RuleStatistics(
                ruleRepository.count(),
                ruleRepository.countByStatus(RuleEntity.RuleStatus.DRAFT),
                ruleRepository.countByStatus(RuleEntity.RuleStatus.PUBLISHED),
                ruleRepository.countByStatus(RuleEntity.RuleStatus.DEPRECATED),
                ruleRepository.countByStatus(RuleEntity.RuleStatus.ARCHIVED),
                ruleRepository.findAllActiveRules().size()
        );
    }

    private RuleEntity findRuleById(String ruleId) {
        return ruleRepository.findByRuleId(ruleId)
                .orElseThrow(() -> ErrorCode.RULE_NOT_FOUND.toException("Rule not found: " + ruleId));
    }

    private RuleResponse toResponse(RuleEntity entity) {
        return RuleResponse.builder()
                .ruleId(entity.getRuleId())
                .name(entity.getName())
                .description(entity.getDescription())
                .ruleType(entity.getRuleType())
                .version(entity.getVersion())
                .status(entity.getStatus())
                .ruleContent(entity.getRuleContent())
                .packageName(entity.getPackageName())
                .ruleGroup(entity.getRuleGroup())
                .priority(entity.getPriority())
                .enabled(entity.isEnabled())
                .tagCode(entity.getTagCode())
                .tagName(entity.getTagName())
                .tagCategory(entity.getTagCategory())
                .tagSeverity(entity.getTagSeverity())
                .effectiveFrom(entity.getEffectiveFrom())
                .effectiveTo(entity.getEffectiveTo())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .publishedAt(entity.getPublishedAt())
                .publishedBy(entity.getPublishedBy())
                .build();
    }

    private RuleMetadata toRuleMetadata(RuleEntity entity) {
        return RuleMetadata.builder()
                .ruleId(entity.getRuleId())
                .name(entity.getName())
                .description(entity.getDescription())
                .ruleType(entity.getRuleType())
                .version(entity.getVersion())
                .status(RuleMetadata.RuleStatus.valueOf(entity.getStatus().name()))
                .ruleContent(entity.getRuleContent())
                .packageName(entity.getPackageName())
                .ruleGroup(entity.getRuleGroup())
                .priority(entity.getPriority())
                .enabled(entity.isEnabled())
                .tagCode(entity.getTagCode())
                .tagName(entity.getTagName())
                .tagCategory(entity.getTagCategory())
                .tagSeverity(entity.getTagSeverity())
                .effectiveFrom(entity.getEffectiveFrom())
                .effectiveTo(entity.getEffectiveTo())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .publishedAt(entity.getPublishedAt())
                .publishedBy(entity.getPublishedBy())
                .build();
    }
}
