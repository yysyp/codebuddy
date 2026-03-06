package com.example.flink.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Transaction entity representing a financial transaction.
 * Uses UTC Instant for all timestamp fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Unique transaction identifier
     */
    private String transactionId;

    /**
     * Account identifier
     */
    private String accountId;

    /**
     * Transaction amount
     */
    private BigDecimal amount;

    /**
     * Transaction currency
     */
    private String currency;

    /**
     * Transaction type: DEBIT, CREDIT, TRANSFER
     */
    private String transactionType;

    /**
     * Counterparty identifier (merchant, recipient, etc.)
     */
    private String counterpartyId;

    /**
     * Counterparty name
     */
    private String counterpartyName;

    /**
     * Transaction description
     */
    private String description;

    /**
     * Transaction timestamp in UTC
     */
    private Instant transactionTime;

    /**
     * Geographic location (country code)
     */
    private String countryCode;

    /**
     * IP address of the transaction
     */
    private String ipAddress;

    /**
     * Device identifier
     */
    private String deviceId;

    /**
     * Risk score (0-100)
     */
    private Integer riskScore;

    /**
     * Tags assigned by rule engine
     */
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    /**
     * Trace ID for distributed tracing
     */
    private String traceId;

    /**
     * Initialize default values and generate traceId
     */
    public void initialize() {
        if (this.transactionId == null) {
            this.transactionId = UUID.randomUUID().toString();
        }
        if (this.traceId == null) {
            this.traceId = UUID.randomUUID().toString();
        }
        if (this.transactionTime == null) {
            this.transactionTime = Instant.now();
        }
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
    }

    /**
     * Add a tag to the transaction (thread-safe for single-threaded Flink processing)
     *
     * @param tag the tag to add
     */
    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty()) {
            // Synchronized for potential concurrent access scenarios
            synchronized (this) {
                if (this.tags == null) {
                    this.tags = new ArrayList<>();
                }
                if (!this.tags.contains(tag)) {
                    this.tags.add(tag);
                }
            }
        }
    }

    /**
     * Check if transaction has a specific tag
     *
     * @param tag the tag to check
     * @return true if transaction has the tag
     */
    public boolean hasTag(String tag) {
        return this.tags != null && this.tags.contains(tag);
    }

    /**
     * Get tags as comma-separated string
     *
     * @return comma-separated tags
     */
    public String getTagsAsString() {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return String.join(",", tags);
    }

    /**
     * Validate transaction data integrity
     *
     * @return true if valid
     */
    public boolean isValid() {
        return transactionId != null && !transactionId.isEmpty()
                && accountId != null && !accountId.isEmpty()
                && amount != null
                && transactionType != null && !transactionType.isEmpty()
                && transactionTime != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", transactionTime=" + transactionTime +
                ", tags=" + tags +
                ", traceId='" + traceId + '\'' +
                '}';
    }
}
