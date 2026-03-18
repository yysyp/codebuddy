package com.transaction.tagging.datapanel;

import com.transaction.tagging.datapanel.config.DataPanelConfig;
import com.transaction.tagging.datapanel.function.TransactionTaggingFunction;
import com.transaction.tagging.datapanel.sink.TaggedTransactionSink;
import com.transaction.tagging.datapanel.source.TransactionSource;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Data Panel Application - Flink-based transaction tagging processor.
 * 
 * This application:
 * 1. Fetches rules from Control Panel
 * 2. Reads transaction data from source (Kafka, file, etc.)
 * 3. Applies rules using Drools engine
 * 4. Outputs tagged transactions to sink
 */
public class DataPanelApplication {

    private static final Logger LOG = LoggerFactory.getLogger(DataPanelApplication.class);

    public static void main(String[] args) throws Exception {
        LOG.info("Starting Data Panel Application...");

        // Load configuration
        DataPanelConfig config = DataPanelConfig.fromArgs(args);
        LOG.info("Configuration loaded: {}", config);

        // Create execution environment
        StreamExecutionEnvironment env = createExecutionEnvironment(config);

        // Create data sources
        SourceFunction<com.transaction.tagging.common.entity.Transaction> transactionSource = 
                TransactionSource.create(config);

        // Build the data pipeline
        DataStream<com.transaction.tagging.common.entity.Transaction> transactions = 
                env.addSource(transactionSource)
                   .name("Transaction Source")
                   .uid("transaction-source");

        // Apply tagging rules
        DataStream<com.transaction.tagging.common.entity.Transaction> taggedTransactions = 
                transactions.process(new TransactionTaggingFunction(config))
                           .name("Transaction Tagging")
                           .uid("transaction-tagging");

        // Output to sink
        taggedTransactions.addSink(TaggedTransactionSink.create(config))
                          .name("Tagged Transaction Sink")
                          .uid("tagged-transaction-sink");

        // Execute the job
        String jobName = "Transaction Tagging Job - " + config.getJobId();
        LOG.info("Executing Flink job: {}", jobName);
        env.execute(jobName);
    }

    /**
     * Create and configure the Flink execution environment.
     */
    private static StreamExecutionEnvironment createExecutionEnvironment(DataPanelConfig config) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Enable checkpointing for fault tolerance
        if (config.isCheckpointingEnabled()) {
            env.enableCheckpointing(config.getCheckpointIntervalMs());
            
            CheckpointConfig checkpointConfig = env.getCheckpointConfig();
            checkpointConfig.setCheckpointTimeout(config.getCheckpointTimeoutMs());
            checkpointConfig.setMaxConcurrentCheckpoints(1);
            checkpointConfig.setMinPauseBetweenCheckpoints(500);
            checkpointConfig.setTolerableCheckpointFailureNumber(3);
            checkpointConfig.setExternalizedCheckpointCleanup(
                    CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        }

        // Configure restart strategy
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
                config.getRestartAttempts(),
                Time.of(config.getRestartDelaySeconds(), TimeUnit.SECONDS)
        ));

        // Set parallelism
        env.setParallelism(config.getParallelism());

        return env;
    }
}
