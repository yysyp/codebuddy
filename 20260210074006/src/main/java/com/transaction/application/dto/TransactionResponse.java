package com.transaction.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for transaction response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transaction response DTO")
public class TransactionResponse {

    @Schema(description = "Transaction ID")
    private UUID id;

    @Schema(description = "Account ID")
    private String accountId;

    @Schema(description = "Transaction type")
    private String transactionType;

    @Schema(description = "Transaction amount")
    private BigDecimal amount;

    @Schema(description = "Currency")
    private String currency;

    @Schema(description = "Merchant name")
    private String merchantName;

    @Schema(description = "Merchant category")
    private String merchantCategory;

    @Schema(description = "Location")
    private String location;

    @Schema(description = "IP address")
    private String ipAddress;

    @Schema(description = "Device ID")
    private String deviceId;

    @Schema(description = "Reference number")
    private String referenceNumber;

    @Schema(description = "Transaction status")
    private String status;

    @Schema(description = "Transaction timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant transactionTime;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Risk score")
    private BigDecimal riskScore;

    @Schema(description = "Tags")
    private String tags;

    @Schema(description = "Created timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    @Schema(description = "Created by")
    private String createdBy;

    @Schema(description = "Updated timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;

    @Schema(description = "Updated by")
    private String updatedBy;
}
