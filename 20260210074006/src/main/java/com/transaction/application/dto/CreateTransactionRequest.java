package com.transaction.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for creating a new transaction
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a transaction")
public class CreateTransactionRequest {

    @NotNull(message = "Account ID is required")
    @NotBlank(message = "Account ID cannot be blank")
    @Size(max = 100, message = "Account ID must be at most 100 characters")
    @Schema(description = "Account ID", example = "ACC123456", required = true)
    private String accountId;

    @NotNull(message = "Transaction type is required")
    @NotBlank(message = "Transaction type cannot be blank")
    @Size(max = 50, message = "Transaction type must be at most 50 characters")
    @Schema(description = "Transaction type", example = "CREDIT", required = true)
    private String transactionType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 15, fraction = 2, message = "Amount must have at most 2 decimal places")
    @Schema(description = "Transaction amount", example = "1000.50", required = true)
    private BigDecimal amount;

    @NotNull(message = "Currency is required")
    @NotBlank(message = "Currency cannot be blank")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be valid ISO 4217 code")
    @Schema(description = "Currency code", example = "USD", required = true)
    private String currency;

    @Size(max = 200, message = "Merchant name must be at most 200 characters")
    @Schema(description = "Merchant name", example = "Amazon")
    private String merchantName;

    @Size(max = 100, message = "Merchant category must be at most 100 characters")
    @Schema(description = "Merchant category", example = "5411")
    private String merchantCategory;

    @Size(max = 200, message = "Location must be at most 200 characters")
    @Schema(description = "Transaction location", example = "New York, USA")
    private String location;

    @Size(max = 45, message = "IP address must be at most 45 characters")
    @Schema(description = "IP address", example = "192.168.1.1")
    private String ipAddress;

    @Size(max = 100, message = "Device ID must be at most 100 characters")
    @Schema(description = "Device ID", example = "DEVICE001")
    private String deviceId;

    @Size(max = 100, message = "Reference number must be at most 100 characters")
    @Schema(description = "Reference number", example = "REF123456")
    private String referenceNumber;

    @Size(max = 20, message = "Status must be at most 20 characters")
    @Schema(description = "Transaction status", example = "PENDING")
    private String status;

    @NotNull(message = "Transaction time is required")
    @Schema(description = "Transaction timestamp in UTC", example = "2024-01-01T10:00:00Z", required = true)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant transactionTime;

    @Size(max = 500, message = "Description must be at most 500 characters")
    @Schema(description = "Transaction description", example = "Purchase from Amazon")
    private String description;
}
