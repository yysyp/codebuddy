package com.transaction.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for transaction filtering and pagination
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for querying transactions")
public class QueryTransactionsRequest {

    @Schema(description = "Account ID filter")
    private String accountId;

    @Schema(description = "Transaction type filter")
    private String transactionType;

    @Schema(description = "Transaction status filter")
    private String status;

    @Schema(description = "Merchant name filter")
    private String merchantName;

    @Schema(description = "Start timestamp for date range filter")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant startTime;

    @Schema(description = "End timestamp for date range filter")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant endTime;

    @NotNull(message = "Page number is required")
    @Schema(description = "Page number (0-based)", example = "0", required = true)
    private Integer pageNumber;

    @NotNull(message = "Page size is required")
    @Schema(description = "Page size", example = "10", required = true)
    private Integer pageSize;

    @Schema(description = "Sort field", example = "transactionTime")
    private String sortBy;

    @Schema(description = "Sort direction", example = "DESC")
    private String sortDirection;
}
