package com.transaction.tagging.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Transaction entity representing financial transaction data.
 * This is the core data model that will be tagged by the rule engine.
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
     * Account ID of the transaction initiator
     */
    private String accountId;

    /**
     * Transaction type (e.g., TRANSFER, PAYMENT, WITHDRAWAL, DEPOSIT)
     */
    private String transactionType;

    /**
     * Transaction amount
     */
    private BigDecimal amount;

    /**
     * Transaction currency (e.g., USD, CNY, EUR)
     */
    private String currency;

    /**
     * Merchant or recipient name
     */
    private String merchantName;

    /**
     * Merchant category code
     */
    private String merchantCategory;

    /**
     * Transaction timestamp in UTC
     */
    private Instant transactionTime;

    /**
     * Geographic location of the transaction
     */
    private String location;

    /**
     * IP address of the transaction device
     */
    private String ipAddress;

    /**
     * Device ID used for the transaction
     */
    private String deviceId;

    /**
     * Channel of transaction (e.g., WEB, MOBILE, ATM, POS)
     */
    private String channel;

    /**
     * Status of the transaction (e.g., PENDING, COMPLETED, FAILED)
     */
    private String status;

    /**
     * Description or memo of the transaction
     */
    private String description;

    /**
     * Parent transaction ID for related transactions
     */
    private String parentTransactionId;

    /**
     * Transaction tags applied by rule engine
     */
    @Builder.Default
    private List<Tag> tags = new ArrayList<>();

    /**
     * Additional metadata as key-value pairs
     */
    @Builder.Default
    private Map<String, Object> metadata = new ConcurrentHashMap<>();

    /**
     * Entity creation timestamp (for audit)
     */
    private Instant createdAt;

    /**
     * User who created this record
     */
    private String createdBy;

    /**
     * Last update timestamp
     */
    private Instant updatedAt;

    /**
     * User who last updated this record
     */
    private String updatedBy;

    /**
     * Add a tag to the transaction
     */
    public void addTag(Tag tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    /**
     * Remove a tag from the transaction
     */
    public void removeTag(String tagCode) {
        if (tags != null) {
            tags.removeIf(t -> t.getCode().equals(tagCode));
        }
    }

    /**
     * Check if transaction has a specific tag
     */
    public boolean hasTag(String tagCode) {
        if (tags == null) {
            return false;
        }
        return tags.stream().anyMatch(t -> t.getCode().equals(tagCode));
    }

    /**
     * Get a metadata value
     */
    public Object getMetadataValue(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    /**
     * Set a metadata value
     */
    public void setMetadataValue(String key, Object value) {
        if (metadata == null) {
            metadata = new ConcurrentHashMap<>();
        }
        metadata.put(key, value);
    }
}
