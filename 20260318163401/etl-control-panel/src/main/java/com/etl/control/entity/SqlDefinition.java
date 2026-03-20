package com.etl.control.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * SQL Definition Entity
 * Stores Flink SQL definitions for data processing
 */
@Entity
@Table(name = "sql_definitions", indexes = {
    @Index(name = "idx_sql_name", columnList = "name"),
    @Index(name = "idx_sql_type", columnList = "sqlType"),
    @Index(name = "idx_sql_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SqlDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String sqlContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SqlType sqlType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SqlStatus status = SqlStatus.ACTIVE;

    @Column(length = 100)
    private String associatedSchema;

    @Column(columnDefinition = "TEXT")
    private String parameters;

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
     * SQL Type Enum
     */
    public enum SqlType {
        SOURCE_TABLE,    // Source table creation SQL
        SINK_TABLE,      // Sink table creation SQL
        TRANSFORM,       // Data transformation SQL
        TAGGING,         // Data tagging SQL
        VALIDATION       // Data validation SQL
    }

    /**
     * SQL Status Enum
     */
    public enum SqlStatus {
        ACTIVE,      // Active and in use
        INACTIVE,    // Inactive but available
        DEPRECATED   // Deprecated
    }
}
