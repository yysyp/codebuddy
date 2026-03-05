package com.example.flink;

import com.example.flink.config.DatabaseInitializer;
import com.example.flink.job.TransactionTaggingJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Main application entry point for the Transaction Tagging Flink Application.
 * 
 * This application:
 * 1. Initializes the H2 database with mock transaction data
 * 2. Runs the Flink job to process transactions using Drools rules
 * 3. Outputs tagged transactions to Parquet files
 */
public class TransactionTaggingApplication {
    
    private static final Logger LOG = LoggerFactory.getLogger(TransactionTaggingApplication.class);
    
    public static void main(String[] args) {
        LOG.info("=========================================");
        LOG.info("Transaction Tagging Application Starting");
        LOG.info("=========================================");
        
        try {
            // Step 1: Initialize database
            LOG.info("Step 1: Initializing database...");
            initializeDatabase();
            
            // Step 2: Run Flink job
            LOG.info("Step 2: Starting Flink job...");
            runFlinkJob(args);
            
            LOG.info("=========================================");
            LOG.info("Application completed successfully");
            LOG.info("=========================================");
            
        } catch (Exception e) {
            LOG.error("Application failed: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
    
    /**
     * Initializes the H2 database with schema and mock data.
     */
    private static void initializeDatabase() {
        // Create data directory if it doesn't exist
        File dataDir = new File("./data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
            LOG.info("Created data directory: {}", dataDir.getAbsolutePath());
        }
        
        // Initialize database
        DatabaseInitializer initializer = new DatabaseInitializer();
        initializer.initialize();
        
        LOG.info("Database initialized successfully");
    }
    
    /**
     * Runs the Flink transaction tagging job.
     */
    private static void runFlinkJob(String[] args) throws Exception {
        // Create output directory
        File outputDir = new File("./output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
            LOG.info("Created output directory: {}", outputDir.getAbsolutePath());
        }
        
        // Parse job configuration
        TransactionTaggingJob.JobConfig config = TransactionTaggingJob.JobConfig.fromArgs(args);
        
        LOG.info("Job configuration:");
        LOG.info("  Database URL: {}", config.getDbUrl());
        LOG.info("  Output Path: {}", config.getOutputPath());
        LOG.info("  Parallelism: {}", config.getParallelism());
        LOG.info("  Checkpoint Interval: {}ms", config.getCheckpointIntervalMs());
        
        // Create and run job
        TransactionTaggingJob job = new TransactionTaggingJob(config);
        job.execute();
    }
}
