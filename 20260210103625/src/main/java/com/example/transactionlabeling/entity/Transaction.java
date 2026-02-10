package com.example.transactionlabeling.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.*;
import lombok.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Transaction entity for storing transaction data
 */
@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String transactionId;

    @Column(nullable = false, length = 100)
    private String accountNumber;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 50)
    private String currency;

    @Column(length = 50)
    private String transactionType;

    @Column(length = 100)
    private String merchantCategory;

    @Column(length = 100)
    private String location;

    @Column(length = 3)
    private String countryCode;

    @Column
    private BigDecimal riskScore;

    @Column(length = 20)
    private String status;

    @Column(length = 100)
    private String description;

    @ElementCollection
    @CollectionTable(name = "transaction_labels", joinColumns = @JoinColumn(name = "transaction_id"))
    @Column(name = "label")
    private Set<String> labels = new HashSet<>();

    @Column(name = "processed_at")
    private Instant processedAt;

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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
