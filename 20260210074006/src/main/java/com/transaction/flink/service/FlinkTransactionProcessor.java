package com.transaction.flink.service;

import com.transaction.domain.entity.Transaction;
import com.transaction.rules.service.RuleEngineService;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for processing transactions using Apache Flink
 */
@Slf4j
@Service
public class FlinkTransactionProcessor {

    private final StreamExecutionEnvironment env;
    private final RuleEngineService ruleEngineService;
    private final ExecutorService executorService;

    @Autowired
    public FlinkTransactionProcessor(StreamExecutionEnvironment env, RuleEngineService ruleEngineService) {
        this.env = env;
        this.ruleEngineService = ruleEngineService;
        this.executorService = Executors.newFixedThreadPool(4, r -> {
            Thread thread = new Thread(r, "flink-processor-executor");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Process a single transaction synchronously
     */
    public Transaction processTransaction(Transaction transaction) {
        log.info("Processing transaction with Flink: {}", transaction.getId());
        
        try {
            // Execute rules using Drools
            ruleEngineService.executeRules(transaction);
            
            log.info("Transaction processed successfully: {}", transaction.getId());
            return transaction;
        } catch (Exception e) {
            log.error("Error processing transaction: {}", transaction.getId(), e);
            throw new RuntimeException("Failed to process transaction", e);
        }
    }

    /**
     * Process multiple transactions in batch
     */
    public List<Transaction> processTransactionsBatch(List<Transaction> transactions) {
        log.info("Processing batch of {} transactions with Flink", transactions.size());
        
        return transactions.stream()
                .map(this::processTransaction)
                .toList();
    }

    /**
     * Process transactions asynchronously using Flink stream processing
     */
    public CompletableFuture<List<Transaction>> processTransactionsAsync(List<Transaction> transactions) {
        log.info("Processing {} transactions asynchronously with Flink", transactions.size());
        
        return CompletableFuture.supplyAsync(() -> {
            // Create a data stream from the list
            DataStream<Transaction> transactionStream = env
                    .fromCollection(transactions)
                    .name("TransactionStream");

            // Process each transaction with rule engine
            SingleOutputStreamOperator<Transaction> processedStream = transactionStream
                    .process(new TransactionRuleProcessor())
                    .name("RuleEngineProcessor");

            // Collect results
            List<Transaction> processedTransactions = processedStream.executeAndCollect(
                    "TransactionProcessingJob",
                    transactions.size()
            );

            log.info("Completed processing {} transactions", processedTransactions.size());
            return processedTransactions;
        }, executorService);
    }

    /**
     * Process transactions continuously using Flink streaming
     * This is useful for real-time transaction processing
     */
    public void startStreamProcessing(DataStream<Transaction> inputStream) {
        log.info("Starting Flink stream processing");

        // Process incoming transactions
        SingleOutputStreamOperator<Transaction> processedStream = inputStream
                .process(new TransactionRuleProcessor())
                .name("RealTimeRuleEngineProcessor");

        // Add aggregation logic
        SingleOutputStreamOperator<TransactionAggregation> aggregatedStream = processedStream
                .keyBy("accountId")
                .process(new TransactionAggregator())
                .name("TransactionAggregator");

        log.info("Flink stream processing started");
    }

    /**
     * Flink ProcessFunction for applying rules to transactions
     */
    private static class TransactionRuleProcessor extends ProcessFunction<Transaction, Transaction> {
        @Override
        public void processElement(Transaction transaction, Context ctx, Collector<Transaction> out) {
            try {
                log.debug("Processing transaction: {}", transaction.getId());
                
                // Apply rules (simulate rule execution)
                applyRules(transaction);
                
                // Collect processed transaction
                out.collect(transaction);
                
                log.debug("Transaction processed: {}", transaction.getId());
            } catch (Exception e) {
                log.error("Error processing transaction: {}", transaction.getId(), e);
                throw new RuntimeException("Failed to process transaction", e);
            }
        }

        private void applyRules(Transaction transaction) {
            // Initialize risk score if null
            if (transaction.getRiskScore() == null) {
                transaction.setRiskScore(BigDecimal.ZERO);
            }

            // Simple rule application for demonstration
            if (transaction.getAmount().compareTo(new BigDecimal("10000")) > 0) {
                transaction.setRiskScore(transaction.getRiskScore().add(new BigDecimal("10")));
                addTag(transaction, "HIGH_VALUE");
            }

            if (transaction.getTransactionType() != null && 
                transaction.getTransactionType().equals("CREDIT") &&
                transaction.getAmount().compareTo(new BigDecimal("50000")) > 0) {
                transaction.setRiskScore(transaction.getRiskScore().add(new BigDecimal("15")));
                addTag(transaction, "LARGE_CREDIT");
            }

            // Add risk category based on score
            addRiskCategory(transaction);
        }

        private void addTag(Transaction transaction, String tag) {
            if (transaction.getTags() == null || transaction.getTags().isEmpty()) {
                transaction.setTags(tag);
            } else if (!transaction.getTags().contains(tag)) {
                transaction.setTags(transaction.getTags() + "," + tag);
            }
        }

        private void addRiskCategory(Transaction transaction) {
            BigDecimal score = transaction.getRiskScore();
            String category;
            
            if (score.compareTo(new BigDecimal("70")) >= 0) {
                category = "VERY_HIGH_RISK";
            } else if (score.compareTo(new BigDecimal("50")) >= 0) {
                category = "MEDIUM_RISK";
            } else {
                category = "LOW_RISK";
            }
            
            addTag(transaction, category);
        }
    }

    /**
     * Transaction aggregation result
     */
    public static class TransactionAggregation {
        private String accountId;
        private int transactionCount;
        private BigDecimal totalAmount;
        private BigDecimal averageRiskScore;
        private Instant windowStart;
        private Instant windowEnd;

        public TransactionAggregation() {
        }

        public TransactionAggregation(String accountId, int transactionCount, 
                                      BigDecimal totalAmount, BigDecimal averageRiskScore,
                                      Instant windowStart, Instant windowEnd) {
            this.accountId = accountId;
            this.transactionCount = transactionCount;
            this.totalAmount = totalAmount;
            this.averageRiskScore = averageRiskScore;
            this.windowStart = windowStart;
            this.windowEnd = windowEnd;
        }

        // Getters and setters
        public String getAccountId() { return accountId; }
        public void setAccountId(String accountId) { this.accountId = accountId; }
        
        public int getTransactionCount() { return transactionCount; }
        public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public BigDecimal getAverageRiskScore() { return averageRiskScore; }
        public void setAverageRiskScore(BigDecimal averageRiskScore) { this.averageRiskScore = averageRiskScore; }
        
        public Instant getWindowStart() { return windowStart; }
        public void setWindowStart(Instant windowStart) { this.windowStart = windowStart; }
        
        public Instant getWindowEnd() { return windowEnd; }
        public void setWindowEnd(Instant windowEnd) { this.windowEnd = windowEnd; }
    }

    /**
     * Flink ProcessFunction for aggregating transactions
     */
    private static class TransactionAggregator extends org.apache.flink.streaming.api.functions.KeyedProcessFunction<String, Transaction, TransactionAggregation> {
        // Simplified aggregator for demonstration
        @Override
        public void processElement(Transaction transaction, Context ctx, Collector<TransactionAggregation> out) {
            log.debug("Aggregating transaction for account: {}", transaction.getAccountId());
            
            TransactionAggregation aggregation = new TransactionAggregation(
                    transaction.getAccountId(),
                    1,
                    transaction.getAmount(),
                    transaction.getRiskScore(),
                    Instant.now().minusSeconds(300),
                    Instant.now()
            );
            
            out.collect(aggregation);
        }
    }

    public void cleanup() {
        executorService.shutdown();
    }
}
