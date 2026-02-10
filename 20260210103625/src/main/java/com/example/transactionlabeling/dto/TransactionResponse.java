package com.example.transactionlabeling.dto;

import lombok.*;
import lombok.*;
import lombok.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

/**
 * Response DTO for transaction data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private String transactionId;
    private String accountNumber;
    private BigDecimal amount;
    private String currency;
    private String transactionType;
    private String merchantCategory;
    private String location;
    private String countryCode;
    private BigDecimal riskScore;
    private String status;
    private String description;
    private Set<String> labels;
    private Instant processedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
