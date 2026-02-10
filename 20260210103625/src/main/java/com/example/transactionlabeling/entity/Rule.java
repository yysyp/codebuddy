package com.example.transactionlabeling.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.*;
import lombok.*;
import lombok.*;

import java.time.Instant;

/**
 * Rule entity for storing Drools rules
 */
@Entity
@Table(name = "rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String ruleName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String ruleContent;

    @Column(nullable = false, length = 50)
    private String ruleCategory;

    @Column
    private Integer priority;

    @Column
    private Boolean active;

    @Column(length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
