package com.transaction.application.controller;

import com.transaction.application.dto.CreateTransactionRequest;
import com.transaction.application.dto.QueryTransactionsRequest;
import com.transaction.application.dto.TransactionResponse;
import com.transaction.application.service.TransactionService;
import com.transaction.common.model.ApiResponse;
import com.transaction.common.model.PaginationMeta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Transaction operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction API", description = "APIs for managing financial transactions with rule-based tagging")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(
            summary = "Create a new transaction",
            description = "Create a new financial transaction and apply rule-based tagging using Drools and Flink"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Transaction created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content
            )
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        
        log.info("Creating transaction for account: {}", request.getAccountId());
        
        TransactionResponse response = transactionService.createTransaction(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction created successfully", response));
    }

    @PostMapping("/batch")
    @Operation(
            summary = "Create multiple transactions in batch",
            description = "Create multiple financial transactions in batch and apply rule-based tagging"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Transactions created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            )
    })
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> createTransactionsBatch(
            @Valid @RequestBody List<CreateTransactionRequest> requests) {
        
        log.info("Creating batch of {} transactions", requests.size());
        
        List<TransactionResponse> responses = transactionService.createTransactionsBatch(requests);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Batch transactions created successfully", responses));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get transaction by ID",
            description = "Retrieve a specific transaction by its unique ID"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transaction retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found"
            )
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @Parameter(description = "Transaction ID", required = true)
            @PathVariable UUID id) {
        
        log.info("Retrieving transaction: {}", id);
        
        TransactionResponse response = transactionService.getTransactionById(id);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(
            summary = "Query transactions with filters and pagination",
            description = "Query transactions with optional filters for account, type, status, date range, etc."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transactions retrieved successfully"
            )
    })
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> queryTransactions(
            @Valid QueryTransactionsRequest request) {
        
        log.info("Querying transactions with filters");
        
        Page<TransactionResponse> page = transactionService.queryTransactions(request);
        
        PaginationMeta meta = PaginationMeta.builder()
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(page.getContent(), meta));
    }

    @GetMapping("/high-risk")
    @Operation(
            summary = "Get high-risk transactions",
            description = "Retrieve all transactions with risk score above the specified threshold"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "High-risk transactions retrieved successfully"
            )
    })
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getHighRiskTransactions(
            @Parameter(description = "Risk score threshold", required = true)
            @RequestParam(defaultValue = "70.0") double threshold) {
        
        log.info("Retrieving high-risk transactions with threshold: {}", threshold);
        
        List<TransactionResponse> responses = transactionService.getHighRiskTransactions(threshold);
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/by-tag")
    @Operation(
            summary = "Get transactions by tag",
            description = "Retrieve all transactions containing a specific tag"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Transactions retrieved successfully"
            )
    })
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByTag(
            @Parameter(description = "Tag to search for", required = true)
            @RequestParam String tag) {
        
        log.info("Retrieving transactions with tag: {}", tag);
        
        List<TransactionResponse> responses = transactionService.getTransactionsByTag(tag);
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/process-pending")
    @Operation(
            summary = "Process pending transactions",
            description = "Process all pending transactions with rule engine and generate Parquet files"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pending transactions processed successfully"
            )
    })
    public ResponseEntity<ApiResponse<Void>> processPendingTransactions() {
        log.info("Processing pending transactions");
        
        transactionService.processPendingTransactions();
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .code("200")
                .message("Pending transactions processed successfully")
                .timestamp(java.time.Instant.now())
                .data(null)
                .build());
    }

    @PostMapping("/generate-parquet")
    @Operation(
            summary = "Generate Parquet file",
            description = "Generate Parquet file for specified transactions"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Parquet file generated successfully"
            )
    })
    public ResponseEntity<ApiResponse<Void>> generateParquetFile(
            @Parameter(description = "List of transaction IDs", required = true)
            @RequestBody List<UUID> transactionIds) {
        
        log.info("Generating Parquet file for {} transactions", transactionIds.size());
        
        transactionService.generateParquetFile(transactionIds);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .code("200")
                .message("Parquet file generated successfully")
                .timestamp(java.time.Instant.now())
                .data(null)
                .build());
    }
}
