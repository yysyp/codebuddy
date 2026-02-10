package com.example.transactionlabeling.controller;

import com.example.transactionlabeling.dto.ApiResponse;
import com.example.transactionlabeling.dto.ProcessingRequest;
import com.example.transactionlabeling.dto.TransactionRequest;
import com.example.transactionlabeling.dto.TransactionResponse;
import com.example.transactionlabeling.service.TransactionService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for managing transactions
 */

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "APIs for managing transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;

    /**
     * Create a new transaction
     */
    @PostMapping
    @Operation(summary = "Create a new transaction", description = "Create a new transaction with the provided data")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @RateLimiter(name = "rateLimiter", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @Valid @RequestBody TransactionRequest request) {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Creating transaction: {}, traceId: {}", request.getTransactionId(), traceId);
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.ok(ApiResponse.success(response).withTraceId(traceId));
    }

    /**
     * Get transaction by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieve a transaction by its database ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @CircuitBreaker(name = "circuitBreaker", fallbackMethod = "circuitBreakerFallback")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @Parameter(description = "Transaction ID") @PathVariable Long id) {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Fetching transaction by ID: {}, traceId: {}", id, traceId);
        TransactionResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(ApiResponse.success(response).withTraceId(traceId));
    }

    /**
     * Get transaction by transaction ID
     */
    @GetMapping("/transaction-id/{transactionId}")
    @Operation(summary = "Get transaction by transaction ID", description = "Retrieve a transaction by its business transaction ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionByTransactionId(
            @Parameter(description = "Business transaction ID") @PathVariable String transactionId) {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Fetching transaction by transaction ID: {}, traceId: {}", transactionId, traceId);
        TransactionResponse response = transactionService.getTransactionByTransactionId(transactionId);
        return ResponseEntity.ok(ApiResponse.success(response).withTraceId(traceId));
    }

    /**
     * Get all transactions with pagination
     */
    @GetMapping
    @Operation(summary = "Get all transactions", description = "Retrieve all transactions with pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transactions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactions(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDir) {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Fetching transactions - page: {}, size: {}, sortBy: {}, sortDir: {}, traceId: {}", page, size, sortBy, sortDir, traceId);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TransactionResponse> transactions = transactionService.getAllTransactions(pageable);

        ApiResponse.ApiResponseMeta meta = ApiResponse.ApiResponseMeta.builder()
                .pageNumber(page)
                .pageSize(size)
                .totalPages(transactions.getTotalPages())
                .totalElements(transactions.getTotalElements())
                .build();

        return ResponseEntity.ok(ApiResponse.success(transactions.getContent()).withMeta(meta).withTraceId(traceId));
    }

    /**
     * Get transactions by account number
     */
    @GetMapping("/account/{accountNumber}")
    @Operation(summary = "Get transactions by account number", description = "Retrieve all transactions for a specific account")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transactions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByAccountNumber(
            @Parameter(description = "Account number") @PathVariable String accountNumber) {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Fetching transactions for account: {}, traceId: {}", accountNumber, traceId);
        List<TransactionResponse> transactions = transactionService.getTransactionsByAccountNumber(accountNumber);
        return ResponseEntity.ok(ApiResponse.success(transactions).withTraceId(traceId));
    }

    /**
     * Get unprocessed transactions
     */
    @GetMapping("/unprocessed")
    @Operation(summary = "Get unprocessed transactions", description = "Retrieve all transactions that haven't been processed yet")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Unprocessed transactions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getUnprocessedTransactions() {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Fetching unprocessed transactions, traceId: {}", traceId);
        List<TransactionResponse> transactions = transactionService.getUnprocessedTransactions();
        return ResponseEntity.ok(ApiResponse.success(transactions).withTraceId(traceId));
    }

    /**
     * Count unprocessed transactions
     */
    @GetMapping("/unprocessed/count")
    @Operation(summary = "Count unprocessed transactions", description = "Get the count of unprocessed transactions")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Count retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<Long>> countUnprocessedTransactions() {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Counting unprocessed transactions, traceId: {}", traceId);
        Long count = transactionService.countUnprocessedTransactions();
        return ResponseEntity.ok(ApiResponse.success(count).withTraceId(traceId));
    }

    /**
     * Process unprocessed transactions
     */
    @PostMapping("/process")
    @Operation(summary = "Process unprocessed transactions", description = "Process unprocessed transactions using Flink and Drools rules")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Processing started successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Processing failed")
    })
    public ResponseEntity<ApiResponse<String>> processTransactions(
            @Valid @RequestBody ProcessingRequest request) {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Starting transaction processing, batchSize: {}, exportToParquet: {}, traceId: {}",
                request.getBatchSize(), request.getUseParquetOutput(), traceId);

        CompletableFuture<Long> future = transactionService.processUnprocessedTransactions(
                request.getBatchSize(),
                request.getUseParquetOutput() != null && request.getUseParquetOutput()
        );

        return ResponseEntity.ok(ApiResponse.success("Processing started. Use GET /api/processing/logs to check status.").withTraceId(traceId));
    }

    /**
     * Delete transaction by ID
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transaction", description = "Delete a transaction by its database ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction deleted successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(
            @Parameter(description = "Transaction ID") @PathVariable Long id) {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Deleting transaction: {}, traceId: {}", id, traceId);
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Transaction deleted successfully").withTraceId(traceId));
    }

    /**
     * Fallback method for rate limiter
     */
    public ResponseEntity<ApiResponse<TransactionResponse>> rateLimitFallback(TransactionRequest request, Exception e) {
        String traceId = UUID.randomUUID().toString();
        log.warn("Rate limit exceeded, traceId: {}", traceId);
        return ResponseEntity.status(429)
                .body(ApiResponse.<TransactionResponse>error("429", "Rate limit exceeded").withTraceId(traceId));
    }

    /**
     * Fallback method for circuit breaker
     */
    public ResponseEntity<ApiResponse<TransactionResponse>> circuitBreakerFallback(Long id, Exception e) {
        String traceId = UUID.randomUUID().toString();
        log.error("Circuit breaker opened, traceId: {}", traceId, e);
        return ResponseEntity.status(503)
                .body(ApiResponse.<TransactionResponse>error("503", "Service temporarily unavailable").withTraceId(traceId));
    }
}
