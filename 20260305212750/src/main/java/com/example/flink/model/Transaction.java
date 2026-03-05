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

/**
 * Transaction entity representing a financial transaction.
 * This class is used as the fact in Drools rules engine.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** Unique transaction identifier */
    private Long transactionId;
    
    /** Account identifier */
    private String accountId;
    
    /** Transaction amount */
    private BigDecimal amount;
    
    /** Transaction type: DEBIT, CREDIT, TRANSFER, etc. */
    private String transactionType;
    
    /** Counterparty information */
    private String counterparty;
    
    /** Transaction timestamp in UTC */
    private Instant transactionTime;
    
    /** Currency code: USD, EUR, CNY, etc. */
    private String currency;
    
    /** Transaction channel: WEB, MOBILE, ATM, BRANCH, etc. */
    private String channel;
    
    /** Geographic location of transaction */
    private String location;
    
    /** Risk score calculated by rules */
    @Builder.Default
    private Integer riskScore = 0;
    
    /** Tags assigned by rules engine */
    private List<String> tags = new ArrayList<>();
    
    /** Description or notes */
    private String description;
    
    /**
     * Adds a tag to the transaction.
     * Thread-safe for concurrent rule execution.
     *
     * @param tag the tag to add
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
     * Increases the risk score.
     *
     * @param score the score to add
     */
    public synchronized void increaseRiskScore(int score) {
        this.riskScore += score;
    }
    
    /**
     * Checks if the transaction amount exceeds a threshold.
     *
     * @param threshold the threshold to compare
     * @return true if amount exceeds threshold
     */
    public boolean isAmountAbove(BigDecimal threshold) {
        if (this.amount == null || threshold == null) {
            return false;
        }
        return this.amount.compareTo(threshold) > 0;
    }
    
    /**
     * Checks if the transaction occurred outside business hours.
     *
     * @return true if outside business hours (before 9:00 or after 18:00 UTC)
     */
    public boolean isOutsideBusinessHours() {
        if (this.transactionTime == null) {
            return false;
        }
        int hour = this.transactionTime.atZone(java.time.ZoneOffset.UTC).getHour();
        return hour < 9 || hour >= 18;
    }
    
    /**
     * Checks if transaction is from a high-risk location.
     *
     * @return true if from high-risk location
     */
    public boolean isHighRiskLocation() {
        if (this.location == null) {
            return false;
        }
        String[] highRiskCountries = {"XX", "YY", "ZZ"}; // Example high-risk country codes
        for (String country : highRiskCountries) {
            if (this.location.toUpperCase().contains(country)) {
                return true;
            }
        }
        return false;
    }
}
