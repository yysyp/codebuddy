package com.transaction.tagging.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Tag entity representing a label applied to transactions.
 * Tags are applied by the rule engine based on defined rules.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique tag code
     */
    private String code;

    /**
     * Display name of the tag
     */
    private String name;

    /**
     * Detailed description of what this tag represents
     */
    private String description;

    /**
     * Category of the tag (e.g., RISK, FRAUD, COMPLIANCE, BUSINESS)
     */
    private String category;

    /**
     * Severity level (e.g., LOW, MEDIUM, HIGH, CRITICAL)
     */
    private String severity;

    /**
     * Color code for UI display (hex format)
     */
    private String color;

    /**
     * Flag indicating if this tag requires manual review
     */
    @Builder.Default
    private boolean requiresReview = false;

    /**
     * Additional properties as JSON string
     */
    private String properties;

    /**
     * Timestamp when the tag was applied
     */
    private Instant appliedAt;

    /**
     * ID of the rule that applied this tag
     */
    private String appliedByRuleId;

    /**
     * Name of the rule that applied this tag
     */
    private String appliedByRuleName;

    /**
     * Confidence score of the tag application (0.0 - 1.0)
     */
    @Builder.Default
    private double confidence = 1.0;

    /**
     * Create a simple tag with code and name only
     */
    public static Tag of(String code, String name) {
        return Tag.builder()
                .code(code)
                .name(name)
                .appliedAt(Instant.now())
                .build();
    }

    /**
     * Create a tag with rule information
     */
    public static Tag of(String code, String name, String ruleId, String ruleName) {
        return Tag.builder()
                .code(code)
                .name(name)
                .appliedAt(Instant.now())
                .appliedByRuleId(ruleId)
                .appliedByRuleName(ruleName)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return code != null && code.equals(tag.code);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
