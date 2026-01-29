package com.example.drools.domain.fact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Fact model for pricing rules
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingFact {

    private String customerId;
    private String customerType; // REGULAR, PREMIUM, VIP
    private BigDecimal originalPrice;
    private BigDecimal discountedPrice;
    private BigDecimal discountRate;
    private String discountReason;
    private int purchaseCount;
    private boolean specialPromotion;

    /**
     * Apply discount
     */
    public void applyDiscount(BigDecimal rate, String reason) {
        this.discountRate = rate;
        this.discountReason = reason;
        this.discountedPrice = this.originalPrice.multiply(BigDecimal.ONE.subtract(rate));
    }
}
