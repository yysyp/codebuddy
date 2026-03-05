package com.example.flink;

import com.example.flink.database.H2DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standalone data generator for initializing H2 database with sample transactions.
 * Can be run independently to prepare the database before running the Flink job.
 */
public class DataGenerator {
    
    private static final Logger LOG = LoggerFactory.getLogger(DataGenerator.class);
    
    public static void main(String[] args) {
        LOG.info("Starting Data Generator...");
        
        try {
            // Initialize the database
            H2DatabaseManager dbManager = H2DatabaseManager.getInstance();
            dbManager.initialize();
            
            // Get transaction count
            var transactions = dbManager.getAllTransactions();
            LOG.info("Database initialized with {} transactions", transactions.size());
            
            // Print sample of transactions
            LOG.info("Sample transactions:");
            transactions.stream()
                    .limit(10)
                    .forEach(t -> LOG.info("  - {}: {} {} ({})", 
                            t.getTransactionId(), 
                            t.getAmount(), 
                            t.getCurrency(),
                            t.getTransactionType()));
            
            LOG.info("Data Generator completed successfully");
            
        } catch (Exception e) {
            LOG.error("Data Generator failed: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}
