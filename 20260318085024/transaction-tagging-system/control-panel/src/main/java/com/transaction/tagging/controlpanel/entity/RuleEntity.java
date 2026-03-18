package com.transaction.tagging.controlpanel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * JPA Entity for storing rule definitions in the database.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rules", indexes = {
        @Index(name = "idx_rule_id", columnList = "rule_id"),
        @Index(name = "idx_rule_status", columnList = "status"),
        @Index(name = "idx_rule_group", columnList = "rule_group")
})
public class RuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_id", unique = true, nullable = false, length = 64)
    private String ruleId;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String ruleType = "DROOLS";

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String version = "1.0.0";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private RuleStatus status = RuleStatus.DRAFT;

    @Column(name = "rule_content", columnDefinition = "TEXT")
    private String ruleContent;

    @Column(name = "package_name", length = 256)
    @Builder.Default
    private String packageName = "com.transaction.tagging.rules";

    @Column(name = "rule_group", length = 64)
    private String ruleGroup;

    @Column(nullable = false)
    @Builder.Default
    private int priority = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "tag_code", length = 64)
    private String tagCode;

    @Column(name = "tag_name", length = 128)
    private String tagName;

    @Column(name = "tag_category", length = 64)
    private String tagCategory;

    @Column(name = "tag_severity", length = 32)
    private String tagSeverity;

    @Column(name = "effective_from")
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "published_by", length = 64)
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
}
