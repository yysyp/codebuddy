package com.transaction.domain.entity;

import com.transaction.common.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Transaction entity for storing financial transaction data
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_account_id", columnList = "account_id"),
    @Index(name = "idx_transaction_type", columnList = "transaction_type"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Transaction entity")
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(description = "Unique transaction ID")
    private UUID id;

    @Column(name = "account_id", nullable = false)
    @Schema(description = "Account ID")
    private String accountId;

    @Column(name = "transaction_type", nullable = false, length = 50)
    @Schema(description = "Transaction type (DEBIT, CREDIT, TRANSFER, etc.)")
    private String transactionType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    @Schema(description = "Transaction amount")
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Schema(description = "Currency code (ISO 4217)")
    private String currency;

    @Column(name = "merchant_name", length = 200)
    @Schema(description = "Merchant name")
    private String merchantName;

    @Column(name = "merchant_category", length = 100)
    @Schema(description = "Merchant category code (MCC)")
    private String merchantCategory;

    @Column(name = "location", length = 200)
    @Schema(description = "Transaction location")
    private String location;

    @Column(name = "ip_address", length = 45)
    @Schema(description = "IP address from which transaction originated")
    private String ipAddress;

    @Column(name = "device_id", length = 100)
    @Schema(description = "Device identifier")
    private String deviceId;

    @Column(name = "reference_number", length = 100)
    @Schema(description = "Transaction reference number")
    private String referenceNumber;

    @Column(name = "status", nullable = false, length = 20)
    @Schema(description = "Transaction status (PENDING, COMPLETED, FAILED, etc.)")
    private String status;

    @Column(name = "transaction_time", nullable = false)
    @Schema(description = "Transaction timestamp in UTC")
    private Instant transactionTime;

    @Column(name = "description", length = 500)
    @Schema(description = "Transaction description")
    private String description;

    @Column(name = "risk_score", precision = 5, scale = 2)
    @Schema(description = "Risk score calculated by rule engine (0-100)")
    private BigDecimal riskScore;

    @Column(name = "tags", length = 1000)
    @Schema(description = "Comma-separated tags assigned by rule engine")
    private String tags;

    @PrePersist
    protected void onCreate() {
        prePersist();
        if (this.status == null) {
            this.status = "PENDING";
        }
        if (this.riskScore == null) {
            this.riskScore = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        preUpdate();
    }

    /**
     * Add a tag to the transaction
     * Used by Drools rule engine
     */
    public void addTag(String tag) {
        if (tag == null || tag.isEmpty()) {
            return;
        }
        
        if (this.tags == null || this.tags.isEmpty()) {
            this.tags = tag;
        } else {
            // Avoid duplicate tags
            if (!this.tags.contains(tag)) {
                this.tags = this.tags + "," + tag;
            }
        }
    }
}
