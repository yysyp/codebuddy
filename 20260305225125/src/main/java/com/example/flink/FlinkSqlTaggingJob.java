package com.example.flink;

import com.example.flink.database.H2DatabaseManager;
import com.example.flink.model.Transaction;
import com.example.flink.udf.TaggingStringUdf;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

/**
 * Flink SQL Job for applying Drools-based tagging rules to transaction data.
 * Reads from H2 database, applies rules via UDF, and outputs to CSV.
 */
public class FlinkSqlTaggingJob {
    
    private static final Logger LOG = LoggerFactory.getLogger(FlinkSqlTaggingJob.class);
    private static final String JOB_NAME = "TransactionTaggingJob";
    private static final String DEFAULT_OUTPUT_PATH = "./output/tagged_transactions.csv";
    
    public static void main(String[] args) throws Exception {
        LOG.info("Starting Flink SQL Tagging Job...");
        
        // Parse command line arguments
        ParameterTool params = ParameterTool.fromArgs(args);
        String outputPath = params.get("output", DEFAULT_OUTPUT_PATH);
        boolean localMode = params.getBoolean("local", true);
        
        // Ensure output directory exists
        Path outputDir = Paths.get(outputPath).getParent();
        if (outputDir != null) {
            Files.createDirectories(outputDir);
        }
        
        // Initialize H2 database with sample data
        H2DatabaseManager dbManager = initializeDatabase();
        
        // Set up Flink environment
        StreamExecutionEnvironment env = createExecutionEnvironment(localMode);
        
        // Configure job
        configureEnvironment(env);
        
        EnvironmentSettings settings = EnvironmentSettings.newInstance()
                .inStreamingMode()
                .build();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, settings);
        
        // Register UDF
        registerUdfs(tableEnv);
        
        // Read data from H2 using DataStream API
        List<Transaction> transactions = dbManager.getAllTransactions();
        LOG.info("Loaded {} transactions from H2 database", transactions.size());
        
        // Create DataStream from collection
        DataStream<Transaction> transactionStream = env.fromCollection(transactions);
        
        // Register as temporary view for SQL processing
        tableEnv.createTemporaryView("transactions_source", transactionStream);
        
        // Create sink table for CSV output
        createSinkTable(tableEnv, outputPath);
        
        // Execute tagging logic using SQL
        executeTaggingSql(tableEnv);
        
        // Close database manager
        dbManager.close();
        
        // Execute job
        env.execute(JOB_NAME);
        
        LOG.info("Flink SQL Tagging Job completed. Output written to: {}", outputPath);
    }
    
    /**
     * Initializes the H2 database with sample transaction data.
     */
    private static void initializeDatabase() {
        LOG.info("Initializing H2 database...");
        try {
            H2DatabaseManager dbManager = H2DatabaseManager.getInstance();
            dbManager.initialize();
            LOG.info("H2 database initialized successfully");
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
        // Enable checkpointing for fault tolerance
        env.enableCheckpointing(60000);
        env.getCheckpointConfig().setCheckpointTimeout(300000);
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(30000);
        
        // Configure restart strategy with fixed delay for simplicity
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
                3,                              // max restart attempts
                org.apache.flink.api.common.time.Time.seconds(10)  // delay between restarts
        ));
        
        LOG.info("Environment configured with checkpointing and restart strategy");
    }
    
    /**
     * Registers UDFs with the TableEnvironment.
     */
    private static void registerUdfs(StreamTableEnvironment tableEnv) {
        LOG.info("Registering UDFs...");
        
        // Register the tagging UDF
        tableEnv.createTemporarySystemFunction("ApplyTaggingRules", TaggingStringUdf.class);
        
        LOG.info("UDFs registered successfully");
    }
    
    /**
     * Creates the source table connecting to H2 database.
     */
    private static void createSourceTable(StreamTableEnvironment tableEnv) {
        LOG.info("Creating source table from H2 database...");
        
        String jdbcUrl = H2DatabaseManager.getJdbcUrl();
        String dbUser = H2DatabaseManager.getDbUser();
        String dbPassword = H2DatabaseManager.getDbPassword();
        
        String createSourceSql = String.format("""
            CREATE TABLE transactions_source (
                transaction_id STRING,
                account_id STRING,
                counterparty_account STRING,
                amount DECIMAL(19, 4),
                currency STRING,
                transaction_type STRING,
                channel STRING,
                country_code STRING,
                transaction_time TIMESTAMP(3) WITH LOCAL TIME ZONE,
                description STRING,
                PRIMARY KEY (transaction_id) NOT ENFORCED
            ) WITH (
                'connector' = 'jdbc',
                'url' = '%s',
                'table-name' = 'transactions',
                'username' = '%s',
                'password' = '%s',
                'driver' = 'org.h2.Driver',
                'scan.fetch-size' = '100'
            )
            """, jdbcUrl, dbUser, dbPassword);
        
        tableEnv.executeSql(createSourceSql);
        LOG.info("Source table 'transactions_source' created");
    }
    
    /**
     * Creates the sink table for CSV output.
     */
    private static void createSinkTable(StreamTableEnvironment tableEnv, String outputPath) {
        LOG.info("Creating sink table for CSV output at: {}", outputPath);
        
        String createSinkSql = String.format("""
            CREATE TABLE tagged_transactions_sink (
                transaction_id STRING,
                account_id STRING,
                counterparty_account STRING,
                amount DECIMAL(19, 4),
                currency STRING,
                transaction_type STRING,
                channel STRING,
                country_code STRING,
                transaction_time TIMESTAMP(3) WITH LOCAL TIME ZONE,
                description STRING,
                risk_level STRING,
                tags STRING,
                applied_rules STRING,
                processing_time TIMESTAMP(3) WITH LOCAL TIME ZONE
            ) WITH (
                'connector' = 'filesystem',
                'path' = '%s',
                'format' = 'csv',
                'csv.field-delimiter' = ',',
                'csv.quote-character' = '"',
                'csv.allow-comments' = 'false',
                'csv.ignore-parse-errors' = 'true',
                'csv.null-literal' = ''
            )
            """, outputPath);
        
        tableEnv.executeSql(createSinkSql);
        LOG.info("Sink table 'tagged_transactions_sink' created");
    }
    
    /**
     * Executes the tagging SQL logic.
     */
    private static void executeTaggingSql(StreamTableEnvironment tableEnv) {
        LOG.info("Executing tagging SQL...");
        
        // Create a view that applies the UDF to tag transactions
        String createTaggedViewSql = """
            CREATE TEMPORARY VIEW tagged_transactions AS
            SELECT 
                transaction_id,
                account_id,
                counterparty_account,
                amount,
                currency,
                transaction_type,
                channel,
                country_code,
                transaction_time,
                description,
                -- Apply tagging rules using UDF
                -- UDF returns format: RISK_LEVEL|TAGS|APPLIED_RULES
                -- Cast amount to STRING for UDF compatibility
                SPLIT_INDEX(ApplyTaggingRules(
                    transaction_id, account_id, CAST(amount AS STRING), currency, transaction_type
                ), '|', 0) AS risk_level,
                SPLIT_INDEX(ApplyTaggingRules(
                    transaction_id, account_id, CAST(amount AS STRING), currency, transaction_type
                ), '|', 1) AS tags,
                SPLIT_INDEX(ApplyTaggingRules(
                    transaction_id, account_id, CAST(amount AS STRING), currency, transaction_type
                ), '|', 2) AS applied_rules,
                CURRENT_TIMESTAMP AS processing_time
            FROM transactions_source
            """;
        
        tableEnv.executeSql(createTaggedViewSql);
        LOG.info("View 'tagged_transactions' created");
        
        // Insert tagged data into sink
        String insertSql = """
            INSERT INTO tagged_transactions_sink
            SELECT 
                transaction_id,
                account_id,
                counterparty_account,
                amount,
                currency,
                transaction_type,
                channel,
                country_code,
                transaction_time,
                description,
                risk_level,
                tags,
                applied_rules,
                processing_time
            FROM tagged_transactions
            """;
        
        tableEnv.executeSql(insertSql);
        LOG.info("Tagging SQL executed successfully");
    }
}
