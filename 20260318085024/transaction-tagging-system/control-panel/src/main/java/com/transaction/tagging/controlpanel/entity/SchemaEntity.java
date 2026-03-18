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
 * JPA Entity for storing data schema definitions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "schemas", indexes = {
        @Index(name = "idx_schema_name", columnList = "schema_name"),
        @Index(name = "idx_schema_version", columnList = "schema_version")
})
public class SchemaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schema_name", nullable = false, length = 128)
    private String schemaName;

    @Column(name = "schema_version", nullable = false, length = 32)
    @Builder.Default
    private String schemaVersion = "1.0.0";

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "schema_type", nullable = false, length = 64)
    private String schemaType;

    @Column(name = "schema_definition", columnDefinition = "TEXT")
    private String schemaDefinition;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

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
}
