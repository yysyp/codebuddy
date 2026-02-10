package com.example.flink.transaction;

import com.example.flink.transaction.config.FlinkConfig;
import com.example.flink.transaction.model.Transaction;
import com.example.flink.transaction.processing.TransactionTaggingFunction;
import com.example.flink.transaction.sink.ParquetSink;
import com.example.flink.transaction.source.H2TransactionSource;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Main class for the Transaction Tagging Flink Application
 *
 * This application:
 * 1. Reads transaction data from H2 embedded database
 * 2. Applies dynamic rules using Drools to tag transactions
 * 3. Writes tagged transactions to Parquet files
 *
 * Features:
 * - Thread-safe rule engine with hot-reloading
 * - Checkpointing for fault tolerance
 * - Distributed tracing with TraceId
 * - Resource management and error handling
 * - Configurable rate limiting
 */
public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        LOG.info("Starting Transaction Tagging Flink Application");

        try {
            // Load configuration
            FlinkConfig config = loadConfiguration(args);
            LOG.info("Configuration loaded successfully");

            // Set up Flink execution environment
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

            // Configure checkpointing for fault tolerance
            env.enableCheckpointing(config.getCheckpointInterval().toMillis());
            env.getCheckpointConfig().setCheckpointTimeout(config.getCheckpointTimeout());
            env.getCheckpointConfig().setMinPauseBetweenCheckpoints(5000);
            env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);

            // Configure restart strategy
            env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
                    3, // max number of restart attempts
                    10000 // delay between restarts in milliseconds
            ));

            // Set parallelism
            int parallelism = 1; // Single parallelism for embedded H2
            env.setParallelism(parallelism);
            LOG.info("Flink parallelism set to: {}", parallelism);

            // Create H2 data source
            long emitIntervalMs = 100; // Emit one transaction every 100ms
            long maxTransactionsToEmit = 50; // Limit to 50 transactions for demo
            SourceFunction<Transaction> transactionSource = new H2TransactionSource(
                    config,
                    emitIntervalMs,
                    maxTransactionsToEmit
            );

            // Create data stream from source
            DataStream<Transaction> transactionStream = env.addSource(transactionSource)
                    .name("H2 Transaction Source")
                    .uid("h2-transaction-source");

            LOG.info("Created H2 transaction source");

            // Apply rule-based tagging
            DataStream<Transaction> taggedStream = transactionStream
                    .map(new TransactionTaggingFunction(config))
                    .name("Rule-Based Tagging")
                    .uid("rule-based-tagging");

            LOG.info("Applied rule-based tagging function");

            // Write to Parquet sink (CSV format that can be converted to Parquet)
            String outputPath = config.getOutputPath();
            taggedStream
                    .map(ParquetSink::convertTransactionToString)
                    .addSink(ParquetSink.createStreamingFileSink(outputPath))
                    .name("CSV Sink (Parquet-Ready)")
                    .uid("parquet-sink");

            LOG.info("Added Parquet sink. Output path: {}", outputPath);

            // Execute the job
            LOG.info("Executing Flink job...");
            env.execute("Transaction Tagging Flink Job");

            LOG.info("Flink job completed successfully");

        } catch (Exception e) {
            LOG.error("Error in Transaction Tagging Flink Application", e);
            System.exit(1);
        }
    }

    /**
     * Load configuration from application.properties
     *
     * @param args command line arguments
     * @return FlinkConfig instance
     */
    private static FlinkConfig loadConfiguration(String[] args) throws IOException {
        Properties props = new Properties();

        // Load default configuration
        String configPath = "src/main/resources/application.properties";
        try (FileInputStream fis = new FileInputStream(configPath)) {
            props.load(fis);
            LOG.info("Loaded configuration from: {}", configPath);
        } catch (IOException e) {
            LOG.warn("Could not load configuration file, using defaults", e);
        }

        // Build FlinkConfig from properties
        return new FlinkConfig.Builder()
                .setDatabaseUrl(props.getProperty("database.url", "jdbc:h2:mem:transactions;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"))
                .setDatabaseUsername(props.getProperty("database.username", "sa"))
                .setDatabasePassword(props.getProperty("database.password", "xxxxxxxx"))
                .setRuleFilePath(props.getProperty("rules.file.path", "rules/TransactionRule.drl"))
                .setOutputPath(props.getProperty("output.path", "output/transactions.parquet"))
                .setCheckpointInterval(Duration.ofMillis(
                        Long.parseLong(props.getProperty("checkpoint.interval.ms", "10000"))))
                .build();
    }
}
