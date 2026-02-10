package com.example.flink.transaction.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Transaction data model for processing in Flink
 * All timestamps are stored as UTC Instant for consistency
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    /**
     * Unique transaction identifier
     */
    private String transactionId;

    /**
     * Customer/account identifier
     */
    private String customerId;

    /**
     * Source account number (encrypted in real scenarios)
     */
    private String sourceAccount;

    /**
     * Destination account number (encrypted in real scenarios)
     */
    private String destinationAccount;

    /**
     * Transaction amount
     */
    private BigDecimal amount;

    /**
     * Currency code (e.g., USD, EUR)
     */
    private String currency;

    /**
     * Transaction type (e.g., TRANSFER, PAYMENT, WITHDRAWAL, DEPOSIT)
     */
    private String transactionType;

    /**
     * Transaction timestamp in UTC
     */
    private Instant timestamp;

    /**
     * Geographic location of transaction (country code)
     */
    private String locationCountry;

    /**
     * Merchant category code (MCC)
     */
    private String merchantCategory;

    /**
     * IP address of the transaction initiator
     */
    private String ipAddress;

    /**
     * Device identifier used for transaction
     */
    private String deviceId;

    /**
     * Transaction status (e.g., PENDING, COMPLETED, FAILED)
     */
    private String status;

    /**
     * Risk score calculated by system (0-100)
     */
    private Integer riskScore;

    /**
     * Tags assigned by rule engine
     */
    private List<String> tags;

    /**
     * Trace ID for observability and distributed tracing
     */
    private String traceId;

    /**
     * Constructor with required fields
     */
    public Transaction(String transactionId, String customerId, String sourceAccount,
                      String destinationAccount, BigDecimal amount, String currency,
                      String transactionType, Instant timestamp) {
        this.transactionId = transactionId;
        this.customerId = customerId;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.amount = amount;
        this.currency = currency;
        this.transactionType = transactionType;
        this.timestamp = timestamp;
        this.tags = new ArrayList<>();
        this.status = "PENDING";
    }

    /**
     * Add a tag to the transaction
     * Thread-safe operation for concurrent processing
     */
    public synchronized void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        if (tag != null && !this.tags.contains(tag)) {
            this.tags.add(tag);
        }
    }

    /**
     * Check if transaction has a specific tag
     */
    public boolean hasTag(String tag) {
        return this.tags != null && this.tags.contains(tag);
    }

    /**
     * Get tags as comma-separated string
     */
    public String getTagsAsString() {
        return this.tags != null ? String.join(",", this.tags) : "";
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
}
