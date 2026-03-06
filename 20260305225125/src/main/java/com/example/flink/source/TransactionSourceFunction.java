package com.example.flink.source;

import com.example.flink.database.H2DatabaseManager;
import com.example.flink.model.Transaction;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Flink SourceFunction that reads transactions from H2 database.
 * Runs in a single instance (non-parallel) to read all data.
 */
public class TransactionSourceFunction extends RichSourceFunction<Transaction> {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(TransactionSourceFunction.class);
    
    private volatile boolean isRunning = false;
    
    @Override
    public void run(SourceContext<Transaction> ctx) throws Exception {
        isRunning = true;
        
        LOG.info("Starting TransactionSourceFunction...");
        
        try {
            // Get database manager instance
            H2DatabaseManager dbManager = H2DatabaseManager.getInstance();
            dbManager.initialize();
            
            // Read all transactions
            List<Transaction> transactions = dbManager.getAllTransactions();
            LOG.info("Source function loaded {} transactions from database", transactions.size());
            
            // Emit all transactions
            for (Transaction transaction : transactions) {
                if (!isRunning) {
                    break;
                }
                synchronized (ctx.getCheckpointLock()) {
                    ctx.collect(transaction);
                }
            }
            
            LOG.info("Source function finished emitting {} transactions", transactions.size());
            
        } catch (Exception e) {
            LOG.error("Error reading transactions from database: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public void cancel() {
        LOG.info("TransactionSourceFunction cancelled");
        isRunning = false;
    }
}
