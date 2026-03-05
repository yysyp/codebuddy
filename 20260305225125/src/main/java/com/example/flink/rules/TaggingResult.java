package com.example.flink.rules;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Result holder for rule-based tagging operations.
 * Thread-safe implementation for concurrent access in Flink.
 */
public class TaggingResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final Set<String> tags;
    private final Set<String> appliedRules;
    private volatile String riskLevel;
    private volatile boolean flaggedForReview;
    private volatile String reviewReason;
    
    public TaggingResult() {
        // Using concurrent collections for thread safety
        this.tags = ConcurrentHashMap.newKeySet();
        this.appliedRules = ConcurrentHashMap.newKeySet();
        this.riskLevel = "LOW";
        this.flaggedForReview = false;
        this.reviewReason = "";
    }
    
    /**
     * Adds a tag to the result.
     * Thread-safe operation.
     */
    public void addTag(String tag) {
        if (tag != null && !tag.isBlank()) {
            tags.add(tag.trim().toUpperCase());
        }
    }
    
    /**
     * Adds multiple tags to the result.
     */
    public void addTags(Set<String> newTags) {
        if (newTags != null) {
            newTags.stream()
                   .filter(t -> t != null && !t.isBlank())
                   .map(t -> t.trim().toUpperCase())
                   .forEach(tags::add);
        }
    }
    
    /**
     * Records that a rule was applied.
     */
    public void recordAppliedRule(String ruleName) {
        if (ruleName != null && !ruleName.isBlank()) {
            appliedRules.add(ruleName.trim());
        }
    }
    
    /**
     * Updates the risk level if the new level is higher.
     * Risk levels: LOW < MEDIUM < HIGH < CRITICAL
     */
    public void updateRiskLevel(String newRiskLevel) {
        if (newRiskLevel == null || newRiskLevel.isBlank()) {
            return;
        }
        
        String normalizedLevel = newRiskLevel.trim().toUpperCase();
        int currentPriority = getRiskPriority(this.riskLevel);
        int newPriority = getRiskPriority(normalizedLevel);
        
        if (newPriority > currentPriority) {
            this.riskLevel = normalizedLevel;
        }
    }
    
    /**
     * Flags this transaction for manual review.
     */
    public void flagForReview(String reason) {
        this.flaggedForReview = true;
        if (reason != null && !reason.isBlank()) {
            if (!this.reviewReason.isEmpty()) {
                this.reviewReason += "; ";
            }
            this.reviewReason += reason;
        }
    }
    
    private int getRiskPriority(String level) {
        return switch (level) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }
    
    // Getters
    public Set<String> getTags() {
        return new CopyOnWriteArraySet<>(tags);
    }
    
    public String getTagsAsString() {
        return String.join(",", tags);
    }
    
    public Set<String> getAppliedRules() {
        return new CopyOnWriteArraySet<>(appliedRules);
    }
    
    public String getAppliedRulesAsString() {
        return String.join(",", appliedRules);
    }
    
    public String getRiskLevel() {
        return riskLevel;
    }
    
    public boolean isFlaggedForReview() {
        return flaggedForReview;
    }
    
    public String getReviewReason() {
        return reviewReason;
    }
    
    /**
     * Merges another tagging result into this one.
     * Used when combining results from multiple rule executions.
     */
    public void merge(TaggingResult other) {
        if (other == null) {
            return;
        }
        this.tags.addAll(other.tags);
        this.appliedRules.addAll(other.appliedRules);
        updateRiskLevel(other.riskLevel);
        if (other.flaggedForReview) {
            this.flaggedForReview = true;
            if (!other.reviewReason.isEmpty()) {
                if (!this.reviewReason.isEmpty()) {
                    this.reviewReason += "; ";
                }
                this.reviewReason += other.reviewReason;
            }
        }
    }
    
    @Override
    public String toString() {
        return "TaggingResult{" +
                "tags=" + tags +
                ", appliedRules=" + appliedRules +
                ", riskLevel='" + riskLevel + '\'' +
                ", flaggedForReview=" + flaggedForReview +
                ", reviewReason='" + reviewReason + '\'' +
                '}';
    }
}
