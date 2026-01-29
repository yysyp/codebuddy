package com.example.drools.domain.fact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Fact model for risk assessment rules
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessmentFact {

    private String requestId;
    private String userId;
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private int riskScore;
    private boolean requiresVerification;
    private boolean flagged;
    private String flagReason;
    private int failedAttempts;
    private boolean isNewUser;
    private boolean unusualLocation;
    private String recommendedAction;

    /**
     * Set risk level based on score
     */
    public void calculateRiskLevel(int score) {
        this.riskScore = score;
        if (score <= 30) {
            this.riskLevel = "LOW";
        } else if (score <= 60) {
            this.riskLevel = "MEDIUM";
        } else if (score <= 80) {
            this.riskLevel = "HIGH";
        } else {
            this.riskLevel = "CRITICAL";
        }
    }

    /**
     * Flag the request
     */
    public void flag(String reason) {
        this.flagged = true;
        this.flagReason = reason;
    }

    /**
     * Require verification
     */
    public void requireVerification() {
        this.requiresVerification = true;
    }

    /**
     * Set recommended action
     */
    public void setRecommendedAction(String action) {
        this.recommendedAction = action;
    }

    // Explicit getters for boolean fields to work with Drools
    public boolean isRequiresVerification() {
        return requiresVerification;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public boolean isNewUser() {
        return isNewUser;
    }

    public boolean isUnusualLocation() {
        return unusualLocation;
    }
}
