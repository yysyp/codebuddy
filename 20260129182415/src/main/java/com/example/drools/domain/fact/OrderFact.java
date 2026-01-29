package com.example.drools.domain.fact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Fact model for order processing rules
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderFact {

    private String orderId;
    private String customerId;
    private String status; // PENDING, APPROVED, REJECTED, MANUAL_REVIEW
    private BigDecimal totalAmount;
    private int itemCount;
    private boolean highValue;
    private boolean suspicious;
    private boolean requiresApproval;
    private String approvalReason;
    private boolean expressShipping;
    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    /**
     * Add warning
     */
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    /**
     * Set status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Mark as high value
     */
    public void markAsHighValue() {
        this.highValue = true;
        this.requiresApproval = true;
        this.approvalReason = "High value order requires manual approval";
    }

    /**
     * Mark as suspicious
     */
    public void markAsSuspicious() {
        this.suspicious = true;
        this.requiresApproval = true;
        this.approvalReason = "Suspicious activity detected";
    }

    /**
     * Auto-approve
     */
    public void autoApprove() {
        this.status = "APPROVED";
        this.requiresApproval = false;
    }

    /**
     * Auto-reject
     */
    public void autoReject() {
        this.status = "REJECTED";
        this.requiresApproval = false;
    }
}
