package com.etl.data.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Transaction Model
 * Represents a financial transaction with tagging support
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    private String transactionId;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private String transactionType;
    private Instant transactionTime;
    private String merchantId;
    private String location;
    private String status;
    
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    /**
     * Add a tag to the transaction
     */
    public void addTag(String tag) {
        if (tags == null) {
            tags = new HashSet<>();
        }
        tags.add(tag);
    }

    /**
     * Remove a tag from the transaction
     */
    public void removeTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
        }
    }

    /**
     * Check if transaction has a specific tag
     */
    public boolean hasTag(String tag) {
        return tags != null && tags.contains(tag);
    }

    /**
     * Get tags as comma-separated string
     */
    public String getTagsAsString() {
        return tags == null || tags.isEmpty() ? "" : String.join(",", tags);
    }

    /**
     * Parse tags from comma-separated string
     */
    public static Set<String> parseTags(String tagsString) {
        Set<String> tags = new HashSet<>();
        if (tagsString != null && !tagsString.trim().isEmpty()) {
            String[] tagArray = tagsString.split(",");
            for (String tag : tagArray) {
                if (!tag.trim().isEmpty()) {
                    tags.add(tag.trim());
                }
            }
        }
        return tags;
    }
}
