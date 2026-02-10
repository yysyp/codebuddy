package com.example.transactionlabeling.service;

import com.example.transactionlabeling.entity.Transaction;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for Flink-based distributed transaction processing
 */

@Service
public class FlinkProcessingService {

    private static final Logger log = LoggerFactory.getLogger(FlinkProcessingService.class);

    private final DroolsRuleEngine droolsRuleEngine;
    private final ParquetExportService parquetExportService;

    @Value("${flink.parallelism:2}")
    private int flinkParallelism;

    @Value("${output.directory:./output}")
    private String outputDirectory;

    @Autowired
    public FlinkProcessingService(DroolsRuleEngine droolsRuleEngine, ParquetExportService parquetExportService) {
        this.droolsRuleEngine = droolsRuleEngine;
        this.parquetExportService = parquetExportService;
    }

    /**
     * Process transactions asynchronously using Flink
     */
    public CompletableFuture<Long> processTransactionsAsync(List<Transaction> transactions, boolean exportToParquet) {
        return CompletableFuture.supplyAsync(() -> {
            return processTransactions(transactions, exportToParquet);
        });
    }

    /**
     * Process transactions using Flink
     */
    public long processTransactions(List<Transaction> transactions, boolean exportToParquet) {
        log.info("Starting Flink processing for {} transactions", transactions.size());
        long startTime = System.currentTimeMillis();

        try {
            // Create Flink execution environment
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            env.setParallelism(flinkParallelism);

            // Create data source from transaction list
            DataStream<Transaction> transactionStream = env.addSource(
                    new TransactionListSource(transactions)
            );

            // Process transactions with Drools rules
            DataStream<Transaction> processedStream = transactionStream
                    .map(new DroolsRuleMapper(droolsRuleEngine));

            // Collect results
            List<Transaction> processedTransactions = processedStream.executeAndCollect(100);

            // Update processed transactions
            long processedCount = processedTransactions.size();

            // Export to CSV as Parquet alternative for now
            if (exportToParquet) {
                // parquetExportService.exportTransactionsToParquet(processedTransactions, outputDirectory);
                log.info("Parquet export is temporarily disabled. Processed {} transactions.", processedCount);
            }

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Flink processing completed. Processed {} transactions in {} ms", processedCount, executionTime);

            return processedCount;
        } catch (Exception e) {
            log.error("Error processing transactions with Flink", e);
            throw new RuntimeException("Flink processing failed", e);
        }
    }

    /**
     * Source function for reading transactions from a list
     */
    private static class TransactionListSource implements SourceFunction<Transaction> {
        private volatile boolean isRunning = true;
        private final List<Transaction> transactions;
        private int index = 0;

        public TransactionListSource(List<Transaction> transactions) {
            this.transactions = transactions;
        }

        @Override
        public void run(SourceContext<Transaction> ctx) throws Exception {
            while (isRunning && index < transactions.size()) {
                ctx.collect(transactions.get(index++));
                Thread.sleep(10); // Simulate streaming delay
            }
        }

        @Override
        public void cancel() {
            isRunning = false;
        }
    }

    /**
     * Map function for applying Drools rules
     */
    private static class DroolsRuleMapper implements MapFunction<Transaction, Transaction> {
        private final transient DroolsRuleEngine droolsRuleEngine;

        public DroolsRuleMapper(DroolsRuleEngine droolsRuleEngine) {
            this.droolsRuleEngine = droolsRuleEngine;
        }

        @Override
        public Transaction map(Transaction transaction) throws Exception {
            try {
                // Apply Drools rules
                droolsRuleEngine.processObject(transaction);

                // Update processing timestamp
                transaction.setProcessedAt(Instant.now());

                return transaction;
            } catch (Exception e) {
                throw new RuntimeException("Error processing transaction: " + transaction.getTransactionId(), e);
            }
        }
    }
}
