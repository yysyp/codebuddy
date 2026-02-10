package com.transaction.application.service;

import com.transaction.application.dto.CreateTransactionRequest;
import com.transaction.application.dto.QueryTransactionsRequest;
import com.transaction.application.dto.TransactionResponse;
import com.transaction.common.exception.ResourceNotFoundException;
import com.transaction.domain.entity.Transaction;
import com.transaction.domain.repository.TransactionRepository;
import com.transaction.flink.service.FlinkTransactionProcessor;
import com.transaction.parquet.service.ParquetService;
import com.transaction.rules.service.RuleEngineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for transaction operations
 */
@Slf4j
@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final RuleEngineService ruleEngineService;
    private final FlinkTransactionProcessor flinkProcessor;
    private final ParquetService parquetService;

    @Autowired
    public TransactionService(
            TransactionRepository transactionRepository,
            RuleEngineService ruleEngineService,
            FlinkTransactionProcessor flinkProcessor,
            ParquetService parquetService) {
        this.transactionRepository = transactionRepository;
        this.ruleEngineService = ruleEngineService;
        this.flinkProcessor = flinkProcessor;
        this.parquetService = parquetService;
    }

    /**
     * Create a new transaction and apply rules
     */
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        log.info("Creating new transaction for account: {}", request.getAccountId());
        
        Transaction transaction = mapToEntity(request);
        
        // Save transaction first
        transaction = transactionRepository.save(transaction);
        
        // Apply rules using Drools
        try {
            ruleEngineService.executeRules(transaction);
        } catch (Exception e) {
            log.error("Error executing rules for transaction: {}", transaction.getId(), e);
        }
        
        // Update with rule results
        transaction = transactionRepository.save(transaction);
        
        // Write to Parquet
        try {
            parquetService.writeTransaction(transaction);
        } catch (Exception e) {
            log.error("Error writing transaction to parquet: {}", transaction.getId(), e);
        }
        
        log.info("Transaction created successfully: {}", transaction.getId());
        return mapToResponse(transaction);
    }

    /**
     * Create multiple transactions in batch
     */
    public List<TransactionResponse> createTransactionsBatch(List<CreateTransactionRequest> requests) {
        log.info("Creating batch of {} transactions", requests.size());
        
        List<Transaction> transactions = requests.stream()
                .map(this::mapToEntity)
                .toList();
        
        // Save all transactions
        transactions = transactionRepository.saveAll(transactions);
        
        // Process with Flink
        try {
            transactions = flinkProcessor.processTransactionsBatch(transactions);
        } catch (Exception e) {
            log.error("Error processing transactions with Flink", e);
        }
        
        // Update with rule results
        transactions = transactionRepository.saveAll(transactions);
        
        // Write to Parquet
        try {
            parquetService.writeTransactions(transactions);
        } catch (Exception e) {
            log.error("Error writing transactions to parquet", e);
        }
        
        log.info("Batch transactions created successfully: {}", transactions.size());
        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get transaction by ID
     */
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID id) {
        log.info("Retrieving transaction: {}", id);
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction not found with ID: " + id));
        
        return mapToResponse(transaction);
    }

    /**
     * Query transactions with filters and pagination
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> queryTransactions(QueryTransactionsRequest request) {
        log.info("Querying transactions with filters: {}", request);
        
        // Build pageable
        Sort sort = buildSort(request);
        Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize(), sort);
        
        // Query based on filters
        Page<Transaction> page;
        if (request.getAccountId() != null && request.getStartTime() != null && request.getEndTime() != null) {
            page = transactionRepository.findByAccountIdAndDateRange(
                    request.getAccountId(),
                    request.getStartTime(),
                    request.getEndTime(),
                    pageable
            );
        } else if (request.getAccountId() != null) {
            page = transactionRepository.findByAccountId(request.getAccountId(), pageable);
        } else if (request.getTransactionType() != null) {
            page = transactionRepository.findByTransactionType(request.getTransactionType(), pageable);
        } else if (request.getStatus() != null) {
            page = transactionRepository.findByStatus(request.getStatus(), pageable);
        } else if (request.getMerchantName() != null) {
            page = transactionRepository.findByMerchantName(request.getMerchantName(), pageable);
        } else {
            page = transactionRepository.findAll(pageable);
        }
        
        log.info("Found {} transactions", page.getTotalElements());
        return page.map(this::mapToResponse);
    }

    /**
     * Get high-risk transactions
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getHighRiskTransactions(double threshold) {
        log.info("Retrieving high-risk transactions with threshold: {}", threshold);
        
        List<Transaction> transactions = transactionRepository.findHighRiskTransactions(
                new java.math.BigDecimal(threshold)
        );
        
        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get transactions by tag
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByTag(String tag) {
        log.info("Retrieving transactions with tag: {}", tag);
        
        List<Transaction> transactions = transactionRepository.findByTag(tag);
        
        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Process existing transactions with rules
     */
    public void processPendingTransactions() {
        log.info("Processing pending transactions");
        
        List<Transaction> pendingTransactions = transactionRepository.findByStatus(
                "PENDING", org.springframework.data.domain.Pageable.unpaged()
        ).getContent();
        log.info("Found {} pending transactions", pendingTransactions.size());
        
        pendingTransactions = flinkProcessor.processTransactionsBatch(pendingTransactions);
        
        transactionRepository.saveAll(pendingTransactions);
        
        // Write to Parquet
        parquetService.writeTransactions(pendingTransactions);
        
        log.info("Processed {} pending transactions", pendingTransactions.size());
    }

    /**
     * Generate Parquet file for transactions
     */
    public void generateParquetFile(List<UUID> transactionIds) {
        log.info("Generating Parquet file for {} transactions", transactionIds.size());
        
        List<Transaction> transactions = transactionRepository.findAllById(transactionIds);
        
        if (!transactions.isEmpty()) {
            parquetService.writeTransactions(transactions);
            log.info("Parquet file generated successfully");
        }
    }

    /**
     * Map request to entity
     */
    private Transaction mapToEntity(CreateTransactionRequest request) {
        return Transaction.builder()
                .accountId(request.getAccountId())
                .transactionType(request.getTransactionType())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .merchantName(request.getMerchantName())
                .merchantCategory(request.getMerchantCategory())
                .location(request.getLocation())
                .ipAddress(request.getIpAddress())
                .deviceId(request.getDeviceId())
                .referenceNumber(request.getReferenceNumber())
                .status(request.getStatus())
                .transactionTime(request.getTransactionTime())
                .description(request.getDescription())
                .riskScore(java.math.BigDecimal.ZERO)
                .build();
    }

    /**
     * Map entity to response
     */
    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccountId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .merchantName(transaction.getMerchantName())
                .merchantCategory(transaction.getMerchantCategory())
                .location(transaction.getLocation())
                .ipAddress(transaction.getIpAddress())
                .deviceId(transaction.getDeviceId())
                .referenceNumber(transaction.getReferenceNumber())
                .status(transaction.getStatus())
                .transactionTime(transaction.getTransactionTime())
                .description(transaction.getDescription())
                .riskScore(transaction.getRiskScore())
                .tags(transaction.getTags())
                .createdAt(transaction.getCreatedAt())
                .createdBy(transaction.getCreatedBy())
                .updatedAt(transaction.getUpdatedAt())
                .updatedBy(transaction.getUpdatedBy())
                .build();
    }

    /**
     * Build sort from request
     */
    private Sort buildSort(QueryTransactionsRequest request) {
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "transactionTime";
        String direction = request.getSortDirection() != null ? 
                request.getSortDirection().toUpperCase() : "DESC";
        
        return Sort.by(Sort.Direction.fromString(direction), sortBy);
    }
}
