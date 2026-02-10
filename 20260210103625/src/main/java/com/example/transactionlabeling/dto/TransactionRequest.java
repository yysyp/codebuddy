package com.example.transactionlabeling.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.*;
import lombok.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO for creating/updating transactions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotBlank(message = "Transaction ID is required")
    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    private String transactionId;

    @NotBlank(message = "Account number is required")
    @Size(max = 100, message = "Account number must not exceed 100 characters")
    private String accountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(max = 50, message = "Currency must not exceed 50 characters")
    private String currency;

    @Size(max = 50, message = "Transaction type must not exceed 50 characters")
    private String transactionType;

    @Size(max = 100, message = "Merchant category must not exceed 100 characters")
    private String merchantCategory;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    @Size(max = 3, message = "Country code must not exceed 3 characters")
    private String countryCode;

    private BigDecimal riskScore;

    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    @Size(max = 100, message = "Description must not exceed 100 characters")
    private String description;
}
