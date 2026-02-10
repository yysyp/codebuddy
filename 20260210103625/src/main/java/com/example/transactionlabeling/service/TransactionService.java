package com.example.transactionlabeling.service;

import com.example.transactionlabeling.dto.TransactionRequest;
import com.example.transactionlabeling.dto.TransactionResponse;
import com.example.transactionlabeling.entity.Transaction;
import com.example.transactionlabeling.entity.ProcessingLog;
import com.example.transactionlabeling.repository.TransactionRepository;
import com.example.transactionlabeling.repository.ProcessingLogRepository;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing transactions
 */

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final ProcessingLogRepository processingLogRepository;
    private final FlinkProcessingService flinkProcessingService;

    /**
     * Create a new transaction
     */
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        log.info("Creating transaction: {}", request.getTransactionId());
        Transaction transaction = mapToEntity(request);
        transaction.setStatus("PENDING");
        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction created with ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    /**
     * Get transaction by ID
     */
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        return mapToResponse(transaction);
    }

    /**
     * Get transaction by transaction ID
     */
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByTransactionId(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with transactionId: " + transactionId));
        return mapToResponse(transaction);
    }

    /**
     * Get all transactions with pagination
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findAll(pageable);
        return transactions.map(this::mapToResponse);
    }

    /**
     * Get transactions by account number
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByAccountNumber(String accountNumber) {
        List<Transaction> transactions = transactionRepository.findByAccountNumber(accountNumber);
        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get unprocessed transactions
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getUnprocessedTransactions() {
        List<Transaction> transactions = transactionRepository.findUnprocessedTransactions();
        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get unprocessed transactions with limit
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getUnprocessedTransactionsWithLimit(int limit) {
        List<Transaction> transactions = transactionRepository.findUnprocessedTransactionsWithLimit(limit);
        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Count unprocessed transactions
     */
    @Transactional(readOnly = true)
    public long countUnprocessedTransactions() {
        return transactionRepository.countUnprocessedTransactions();
    }

    /**
     * Process unprocessed transactions
     */
    @Async
    @Transactional
    public Long processUnprocessedTransactions(int batchSize, boolean exportToParquet) {
        log.info("Starting processing of unprocessed transactions, batch size: {}", batchSize);

        Instant startTime = Instant.now();
        ProcessingLog processingLog = createProcessingLog("PROCESS_TRANSACTIONS", "STARTED", startTime);

        try {
            // Get unprocessed transactions
            List<Transaction> transactions = transactionRepository.findUnprocessedTransactionsWithLimit(batchSize);
            log.info("Found {} unprocessed transactions", transactions.size());

            if (transactions.isEmpty()) {
                processingLog.setStatus("COMPLETED");
                processingLog.setEndTime(Instant.now());
                processingLog.setRecordsProcessed(0L);
                processingLogRepository.save(processingLog);
                return 0L;
            }

            // Process with Flink
            long processedCount = flinkProcessingService.processTransactions(transactions, exportToParquet);

            // Save processed transactions
            transactionRepository.saveAll(transactions);

            Instant endTime = Instant.now();
            long executionTimeMs = endTime.toEpochMilli() - startTime.toEpochMilli();

            processingLog.setStatus("COMPLETED");
            processingLog.setEndTime(endTime);
            processingLog.setRecordsProcessed(processedCount);
            processingLog.setExecutionTimeMs(executionTimeMs);
            processingLogRepository.save(processingLog);

            log.info("Processing completed. Processed {} transactions in {} ms", processedCount, executionTimeMs);
            return processedCount;

        } catch (Exception e) {
            log.error("Error processing transactions", e);
            processingLog.setStatus("FAILED");
            processingLog.setEndTime(Instant.now());
            processingLog.setErrorMessage(e.getMessage());
            processingLogRepository.save(processingLog);
            throw new RuntimeException("Failed to process transactions", e);
        }
    }

    /**
     * Delete transaction by ID
     */
    @Transactional
    public void deleteTransaction(Long id) {
        log.info("Deleting transaction with ID: {}", id);
        transactionRepository.deleteById(id);
    }

    /**
     * Delete transaction by transaction ID
     */
    @Transactional
    public void deleteTransactionByTransactionId(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with transactionId: " + transactionId));
        transactionRepository.delete(transaction);
        log.info("Deleted transaction: {}", transactionId);
    }

    private ProcessingLog createProcessingLog(String operationName, String status, Instant startTime) {
        ProcessingLog processingLog = ProcessingLog.builder()
                .operationName(operationName)
                .status(status)
                .startTime(startTime)
                .build();
        return processingLogRepository.save(processingLog);
    }

    private Transaction mapToEntity(TransactionRequest request) {
        return Transaction.builder()
                .transactionId(request.getTransactionId())
                .accountNumber(request.getAccountNumber())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .transactionType(request.getTransactionType())
                .merchantCategory(request.getMerchantCategory())
                .location(request.getLocation())
                .countryCode(request.getCountryCode())
                .riskScore(request.getRiskScore())
                .status(request.getStatus())
                .description(request.getDescription())
                .build();
    }

    private TransactionResponse mapToResponse(Transaction entity) {
        return TransactionResponse.builder()
                .id(entity.getId())
                .transactionId(entity.getTransactionId())
                .accountNumber(entity.getAccountNumber())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .transactionType(entity.getTransactionType())
                .merchantCategory(entity.getMerchantCategory())
                .location(entity.getLocation())
                .countryCode(entity.getCountryCode())
                .riskScore(entity.getRiskScore())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .labels(entity.getLabels())
                .processedAt(entity.getProcessedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
