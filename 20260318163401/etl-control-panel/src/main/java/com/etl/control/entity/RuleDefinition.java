package com.etl.control.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Rule Definition Entity
 * Stores Drools rule definitions with versioning support
 */
@Entity
@Table(name = "rule_definitions", indexes = {
    @Index(name = "idx_rule_name", columnList = "name"),
    @Index(name = "idx_rule_status", columnList = "status"),
    @Index(name = "idx_rule_version", columnList = "name, version")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String ruleContent;

    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RuleStatus status = RuleStatus.DRAFT;

    @Column(length = 50)
    private String ruleType;

    @Column(length = 50)
    private String targetType;

    @Column(length = 20)
    private String priority;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false, updatable = false)
    private String createdBy;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Rule Status Enum
     */
    public enum RuleStatus {
        DRAFT,       // Draft status, not published
        PUBLISHED,   // Published and active
        DEPRECATED,  // Deprecated but still available
        ARCHIVED     // Archived and inactive
    }
}
