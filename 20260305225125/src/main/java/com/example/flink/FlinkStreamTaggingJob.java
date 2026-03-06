package com.example.flink;

import com.example.flink.database.H2DatabaseManager;
import com.example.flink.model.Transaction;
import com.example.flink.rules.DroolsRuleEngine;
import com.example.flink.rules.TaggingResult;
import com.example.flink.source.TransactionSourceFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Flink DataStream Job for applying Drools-based tagging rules to transaction data.
 * Reads from H2 database, applies rules, and outputs to CSV.
 */
public class FlinkStreamTaggingJob {
    
    private static final Logger LOG = LoggerFactory.getLogger(FlinkStreamTaggingJob.class);
    private static final String JOB_NAME = "TransactionTaggingJob";
    private static final String DEFAULT_OUTPUT_PATH = "./output/tagged_transactions.csv";
    
    public static void main(String[] args) throws Exception {
        LOG.info("Starting Flink Stream Tagging Job...");
        
        // Parse command line arguments
        ParameterTool params = ParameterTool.fromArgs(args);
        String outputPath = params.get("output", DEFAULT_OUTPUT_PATH);
        boolean localMode = params.getBoolean("local", true);
        
        // Ensure output directory exists
        Path outputDir = Paths.get(outputPath).getParent();
        if (outputDir != null) {
            Files.createDirectories(outputDir);
        }
        
        // Set up Flink environment
        StreamExecutionEnvironment env = createExecutionEnvironment(localMode);
        
        // Configure job
        configureEnvironment(env);
        
        // Create DataStream from custom source function
        DataStream<Transaction> transactionStream = env.addSource(new TransactionSourceFunction())
                .name("H2 Transaction Source")
                .uid("h2-transaction-source");
        
        // Apply tagging rules using map function
        DataStream<String> taggedStream = transactionStream.map(transaction -> {
            // Initialize Drools engine on first use
            DroolsRuleEngine.initialize();
            DroolsRuleEngine ruleEngine = new DroolsRuleEngine();
            
            // Apply rules
            TaggingResult result = ruleEngine.applyRules(transaction);
            
            // Format output as CSV row
            return formatCsvRow(transaction, result);
        }).name("Tagging Mapper").uid("tagging-mapper");
        
        // Write to CSV file
        taggedStream.addSink(new CsvSinkFunction(outputPath))
                .name("CSV Sink")
                .uid("csv-sink");
        
        // Execute job
        env.execute(JOB_NAME);
        
        LOG.info("Flink Stream Tagging Job completed. Output written to: {}", outputPath);
    }
    
    /**
     * Formats a transaction and tagging result as a CSV row.
     */
    private static String formatCsvRow(Transaction tx, TaggingResult result) {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
            escapeCsv(tx.getTransactionId()),
            escapeCsv(tx.getAccountId()),
            escapeCsv(tx.getCounterpartyAccount()),
            tx.getAmount() != null ? tx.getAmount().toPlainString() : "",
            escapeCsv(tx.getCurrency()),
            escapeCsv(tx.getTransactionType()),
            escapeCsv(tx.getChannel()),
            escapeCsv(tx.getCountryCode()),
            tx.getTransactionTime() != null ? tx.getTransactionTime().toString() : "",
            escapeCsv(tx.getDescription()),
            escapeCsv(result.getRiskLevel()),
            escapeCsv(result.getTagsAsString()),
            escapeCsv(result.getAppliedRulesAsString()),
            java.time.Instant.now().toString()
        );
    }
    
    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    /**
     * Initializes the H2 database with sample transaction data.
     */
    private static H2DatabaseManager initializeDatabase() throws Exception {
        LOG.info("Initializing H2 database...");
        try {
            H2DatabaseManager dbManager = H2DatabaseManager.getInstance();
            dbManager.initialize();
            LOG.info("H2 database initialized successfully");
            return dbManager;
        } catch (Exception e) {
            LOG.error("Failed to initialize H2 database: {}", e.getMessage(), e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * Creates the Flink execution environment.
     */
    private static StreamExecutionEnvironment createExecutionEnvironment(boolean localMode) {
        Configuration config = new Configuration();
        
        if (localMode) {
            // Local execution with single parallelism for simplicity
            config.setInteger("parallelism.default", 1);
            return StreamExecutionEnvironment.getExecutionEnvironment(config);
        } else {
            // Cluster execution
            return StreamExecutionEnvironment.getExecutionEnvironment();
        }
    }
    
    /**
     * Configures the execution environment with checkpointing and restart strategy.
     */
    private static void configureEnvironment(StreamExecutionEnvironment env) {
        // Configure restart strategy with fixed delay for simplicity
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
                3,                              // max restart attempts
                Time.seconds(10)                // delay between restarts
        ));
        
        LOG.info("Environment configured with restart strategy");
    }
    
    /**
     * Custom CSV sink function that writes to a file with header.
     */
    private static class CsvSinkFunction implements SinkFunction<String> {
        
        private static final long serialVersionUID = 1L;
        private static final Logger LOG = LoggerFactory.getLogger(CsvSinkFunction.class);
        
        private final String outputPath;
        private transient boolean headerWritten = false;
        
        public CsvSinkFunction(String outputPath) {
            this.outputPath = outputPath;
        }
        
        @Override
        public void invoke(String value, Context context) throws Exception {
            Path path = Paths.get(outputPath);
            
            // Write header on first record
            if (!headerWritten) {
                writeHeader(path);
                headerWritten = true;
            }
            
            // Append the data row
            try (BufferedWriter writer = Files.newBufferedWriter(path, 
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                writer.write(value);
                writer.newLine();
            }
        }
        
        private void writeHeader(Path path) throws IOException {
            String header = "transaction_id,account_id,counterparty_account,amount,currency," +
                    "transaction_type,channel,country_code,transaction_time,description," +
                    "risk_level,tags,applied_rules,processing_time";
            
            // Delete file if exists to start fresh
            Files.deleteIfExists(path);
            
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE)) {
                writer.write(header);
                writer.newLine();
            }
            LOG.info("CSV header written to {}", outputPath);
        }
    }
}
