package com.transaction.tagging.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * RuleMetadata entity representing rule definition and metadata.
 * This is used by Control Panel to manage rules and by Data Panel to fetch rules.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique rule identifier
     */
    private String ruleId;

    /**
     * Rule name for display
     */
    private String name;

    /**
     * Detailed description of the rule
     */
    private String description;

    /**
     * Rule type (e.g., DROOLS, SP EL, GROOVY)
     */
    @Builder.Default
    private String ruleType = "DROOLS";

    /**
     * Rule version
     */
    @Builder.Default
    private String version = "1.0.0";

    /**
     * Rule status (DRAFT, PUBLISHED, DEPRECATED, ARCHIVED)
     */
    @Builder.Default
    private RuleStatus status = RuleStatus.DRAFT;

    /**
     * Drools rule content (DRL format)
     */
    private String ruleContent;

    /**
     * Rule package name for Drools
     */
    @Builder.Default
    private String packageName = "com.transaction.tagging.rules";

    /**
     * Rule group for organization
     */
    private String ruleGroup;

    /**
     * Priority of the rule (higher number = higher priority)
     */
    @Builder.Default
    private int priority = 0;

    /**
     * Flag to enable/disable the rule
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * Tag code to apply when rule matches
     */
    private String tagCode;

    /**
     * Tag name to apply when rule matches
     */
    private String tagName;

    /**
     * Tag category
     */
    private String tagCategory;

    /**
     * Tag severity
     */
    private String tagSeverity;

    /**
     * Effective start time
     */
    private Instant effectiveFrom;

    /**
     * Effective end time
     */
    private Instant effectiveTo;

    /**
     * Creation timestamp
     */
    private Instant createdAt;

    /**
     * User who created the rule
     */
    private String createdBy;

    /**
     * Last update timestamp
     */
    private Instant updatedAt;

    /**
     * User who last updated the rule
     */
    private String updatedBy;

    /**
     * Publication timestamp
     */
    private Instant publishedAt;

    /**
     * User who published the rule
     */
    private String publishedBy;

    /**
     * Rule status enum
     */
    public enum RuleStatus {
        DRAFT,
        PUBLISHED,
        DEPRECATED,
        ARCHIVED
    }

    /**
     * Check if rule is currently effective
     */
    public boolean isEffective(Instant atTime) {
        if (!enabled || status != RuleStatus.PUBLISHED) {
            return false;
        }
        Instant now = atTime != null ? atTime : Instant.now();
        boolean afterStart = effectiveFrom == null || !now.isBefore(effectiveFrom);
        boolean beforeEnd = effectiveTo == null || !now.isAfter(effectiveTo);
        return afterStart && beforeEnd;
    }
}
