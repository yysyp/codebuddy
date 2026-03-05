package com.example.flink.job;

import com.example.flink.model.TaggedTransaction;
import com.example.flink.model.Transaction;
import com.example.flink.source.TransactionSource;
import com.example.flink.udf.DroolsTaggingFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.OutputFileConfig;
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.ZoneId;

/**
 * Flink Job for transaction tagging using Drools rules.
 * 
 * This job:
 * 1. Reads transactions from H2 database using JDBC connector
 * 2. Applies Drools rules to tag transactions
 * 3. Writes tagged results to Parquet files
 */
public class TransactionTaggingJob {
    
    private static final Logger LOG = LoggerFactory.getLogger(TransactionTaggingJob.class);
    
    // Configuration defaults
    private static final String DEFAULT_DB_URL = "jdbc:h2:file:./data/transactions;DB_CLOSE_DELAY=-1;MODE=MySQL";
    private static final String DEFAULT_DB_USER = "sa";
    private static final String DEFAULT_DB_PASSWORD = "xxxxxxxx";
    private static final String DEFAULT_OUTPUT_PATH = "./output/tagged-transactions";
    private static final int DEFAULT_PARALLELISM = 2;
    private static final int DEFAULT_CHECKPOINT_INTERVAL_MS = 60000;
    
    private final StreamExecutionEnvironment env;
    private final JobConfig config;
    
    public TransactionTaggingJob(JobConfig config) {
        this.config = config;
        this.env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        configureEnvironment();
    }
    
    /**
     * Configures the Flink execution environment.
     */
    private void configureEnvironment() {
        // Set parallelism
        env.setParallelism(config.getParallelism());
        
        // Disable closure cleaner to avoid reflection issues with Java 21
        env.getConfig().setClosureCleanerLevel(org.apache.flink.api.common.ExecutionConfig.ClosureCleanerLevel.NONE);
        
        // Enable checkpointing for fault tolerance
        env.enableCheckpointing(config.getCheckpointIntervalMs());
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(30000);
        env.getCheckpointConfig().setCheckpointTimeout(600000);
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
        
        // Restart strategy for fault tolerance
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
                3,  // number of restart attempts
                Time.seconds(10)  // delay between attempts
        ));
        
        LOG.info("Flink environment configured with parallelism={}, checkpointInterval={}ms",
                config.getParallelism(), config.getCheckpointIntervalMs());
    }
    
    /**
     * Executes the job using DataStream API with RichMapFunction.
     * This approach properly handles Drools engine lifecycle in distributed environment.
     */
    public void execute() throws Exception {
        LOG.info("Starting Transaction Tagging Job using DataStream API...");
        
        // Create custom JDBC source
        DataStream<Transaction> sourceStream = env
                .addSource(new TransactionSource(
                        config.getDbUrl(),
                        config.getDbUser(),
                        config.getDbPassword()
                ))
                .name("Transaction Source")
                .uid("transaction-source");
        
        // Apply Drools rules to tag transactions
        DataStream<TaggedTransaction> taggedStream = sourceStream
                .filter(t -> t != null)
                .name("Filter Null")
                .uid("filter-null")
                .keyBy(Transaction::getTransactionId)
                .map(new DroolsTaggingFunction())
                .name("Drools Tagging")
                .uid("drools-tagging");
        
        // Write to Parquet files
        writeToParquet(taggedStream);
        
        // Execute the job
        env.execute("Transaction Tagging Job");
    }
    
    /**
     * Writes tagged transactions to JSON files.
     */
    private void writeToParquet(DataStream<TaggedTransaction> stream) {
        // Use JSON format for simplicity and compatibility
        org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy<String, String> rollingPolicy = 
                org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy
                        .builder()
                        .withRolloverInterval(java.time.Duration.ofMinutes(5))
                        .withInactivityInterval(java.time.Duration.ofMinutes(1))
                        .withMaxPartSize(1024 * 1024 * 128) // 128MB
                        .build();
        
        // Configure output file naming
        OutputFileConfig outputConfig = OutputFileConfig
                .builder()
                .withPartPrefix("tagged-transactions")
                .withPartSuffix(".json")
                .build();
        
        // Convert to JSON string and write as text
        stream.map(TransactionTaggingJob::toJsonString)
                .name("To JSON")
                .uid("to-json")
                .addSink(StreamingFileSink
                        .forRowFormat(
                                new Path(config.getOutputPath()),
                                new org.apache.flink.api.common.serialization.SimpleStringEncoder<String>("UTF-8")
                        )
                        .withBucketAssigner(new DateTimeBucketAssigner<>("yyyy-MM-dd", ZoneId.of("UTC")))
                        .withRollingPolicy(rollingPolicy)
                        .withOutputFileConfig(outputConfig)
                        .build())
                .name("JSON Sink")
                .uid("json-sink");
        
        LOG.info("JSON sink configured with output path: {}", config.getOutputPath());
    }
    
    /**
     * Converts TaggedTransaction to JSON string.
     */
    private static String toJsonString(TaggedTransaction t) {
        if (t == null) return "{}";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"transactionId\":").append(t.getTransactionId()).append(",");
        sb.append("\"accountId\":\"").append(escapeJson(t.getAccountId())).append("\",");
        sb.append("\"amount\":").append(t.getAmount()).append(",");
        sb.append("\"transactionType\":\"").append(escapeJson(t.getTransactionType())).append("\",");
        sb.append("\"counterparty\":\"").append(escapeJson(t.getCounterparty())).append("\",");
        sb.append("\"transactionTime\":\"").append(t.getTransactionTime() != null ? t.getTransactionTime().toString() : "").append("\",");
        sb.append("\"currency\":\"").append(escapeJson(t.getCurrency())).append("\",");
        sb.append("\"channel\":\"").append(escapeJson(t.getChannel())).append("\",");
        sb.append("\"location\":\"").append(escapeJson(t.getLocation())).append("\",");
        sb.append("\"description\":\"").append(escapeJson(t.getDescription())).append("\",");
        sb.append("\"riskScore\":").append(t.getRiskScore()).append(",");
        sb.append("\"tags\":\"").append(escapeJson(t.getTags())).append("\",");
        sb.append("\"processingTime\":\"").append(t.getProcessingTime() != null ? t.getProcessingTime().toString() : "").append("\",");
        sb.append("\"traceId\":\"").append(escapeJson(t.getTraceId())).append("\"");
        sb.append("}");
        return sb.toString();
    }
    
    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    
    /**
     * Job configuration class.
     */
    public static class JobConfig implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String dbUrl = DEFAULT_DB_URL;
        private String dbUser = DEFAULT_DB_USER;
        private String dbPassword = DEFAULT_DB_PASSWORD;
        private String outputPath = DEFAULT_OUTPUT_PATH;
        private int parallelism = DEFAULT_PARALLELISM;
        private int checkpointIntervalMs = DEFAULT_CHECKPOINT_INTERVAL_MS;
        
        public static JobConfig fromArgs(String[] args) {
            ParameterTool params = ParameterTool.fromArgs(args);
            JobConfig config = new JobConfig();
            
            config.dbUrl = params.get("db-url", DEFAULT_DB_URL);
            config.dbUser = params.get("db-user", DEFAULT_DB_USER);
            config.dbPassword = params.get("db-password", DEFAULT_DB_PASSWORD);
            config.outputPath = params.get("output-path", DEFAULT_OUTPUT_PATH);
            config.parallelism = params.getInt("parallelism", DEFAULT_PARALLELISM);
            config.checkpointIntervalMs = params.getInt("checkpoint-interval", DEFAULT_CHECKPOINT_INTERVAL_MS);
            
            return config;
        }
        
        // Getters
        public String getDbUrl() { return dbUrl; }
        public String getDbUser() { return dbUser; }
        public String getDbPassword() { return dbPassword; }
        public String getOutputPath() { return outputPath; }
        public int getParallelism() { return parallelism; }
        public int getCheckpointIntervalMs() { return checkpointIntervalMs; }
    }
}
