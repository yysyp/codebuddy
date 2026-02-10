package com.example.transaction.service;

import com.example.transaction.config.AppConfig;
import com.example.transaction.database.DatabaseManager;
import com.example.transaction.output.CsvWriter;
import com.example.transaction.model.Transaction;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class TransactionProcessor {
    private static final Logger logger = Logger.getLogger(TransactionProcessor.class.getName());
    
    private final AppConfig config;
    private DatabaseManager databaseManager;
    private final ExecutorService executorService;
    private final ReentrantLock lock = new ReentrantLock();
    
    public TransactionProcessor(AppConfig config) {
        this.config = config;
        this.executorService = Executors.newFixedThreadPool(config.getParallelism());
    }
    
    public void process() throws Exception {
        String traceId = UUID.randomUUID().toString();
        logger.info("[TraceId: " + traceId + "] Starting transaction processing");
        
        try {
            databaseManager = new DatabaseManager(config);
            List<Transaction> transactions = databaseManager.getAllTransactions();
            
            if (transactions.isEmpty()) {
                logger.warning("[TraceId: " + traceId + "] No transactions found in database");
                return;
            }
            
            logger.info("[TraceId: " + traceId + "] Retrieved " + transactions.size() + " transactions for processing");
            
            RuleProcessor ruleProcessor = new RuleProcessor();
            List<Transaction> processedTransactions = processTransactions(transactions, ruleProcessor, traceId);
            
            CsvWriter csvWriter = new CsvWriter(config.getOutputDirectory());
            csvWriter.writeTransactions(processedTransactions);
            
            logger.info("[TraceId: " + traceId + "] Processing completed successfully");
            
        } finally {
            executorService.shutdown();
            executorService.awaitTermination(30, TimeUnit.SECONDS);
            if (databaseManager != null) {
                databaseManager.close();
            }
        }
    }
    
    private List<Transaction> processTransactions(List<Transaction> transactions, 
                                                   RuleProcessor ruleProcessor, String traceId) 
            throws InterruptedException, ExecutionException {
        
        List<Future<Transaction>> futures = new ArrayList<>();
        List<Transaction> processed = new ArrayList<>(transactions.size());
        
        for (Transaction tx : transactions) {
            Future<Transaction> future = executorService.submit(() -> {
                String txTraceId = UUID.randomUUID().toString();
                logger.fine("[TraceId: " + txTraceId + "] Processing transaction: " + tx.getTransactionId());
                
                try {
                    ruleProcessor.applyRules(tx);
                    return tx;
                } catch (Exception e) {
                    logger.severe("[TraceId: " + txTraceId + "] Failed to process transaction: " + 
                                 tx.getTransactionId() + ", Error: " + e.getMessage());
                    throw e;
                }
            });
            futures.add(future);
        }
        
        for (Future<Transaction> future : futures) {
            processed.add(future.get());
        }
        
        logger.info("[TraceId: " + traceId + "] Processed " + processed.size() + " transactions");
        return processed;
    }
}
