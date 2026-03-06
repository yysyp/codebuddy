package com.example.flink.job;

import com.example.flink.config.FlinkJobConfig;
import com.example.flink.function.TransactionTaggingFunction;
import com.example.flink.model.TaggedTransaction;
import com.example.flink.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.reader.TextLineInputFormat;
import org.apache.flink.core.fs.Path;
import org.apache.flink.formats.csv.CsvReaderFormat;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.data.TimestampData;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.types.Row;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Flink SQL Job for transaction tagging using Drools rules.
 * Supports both DataStream API and Table/SQL API.
 */
@Slf4j
public class TransactionTaggingJob {

    private final FlinkJobConfig config;
    private transient StreamExecutionEnvironment env;
    private transient StreamTableEnvironment tableEnv;

    // Metrics counters
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);

    public TransactionTaggingJob(FlinkJobConfig config) {
        this.config = Objects.requireNonNull(config, "Config cannot be null");
    }

    /**
     * Initialize Flink environment with configurations
     */
    private void initializeEnvironment() {
        log.info("Initializing Flink environment with config: {}", config);

        // Create execution environment
        Configuration flinkConfig = new Configuration();
        flinkConfig.setString("table.exec.source.idle-timeout", "1 min");

        env = StreamExecutionEnvironment.getExecutionEnvironment(flinkConfig);
        env.setParallelism(config.getParallelism());

        // Configure checkpointing for fault tolerance
        if (config.isCheckpointingEnabled()) {
            env.enableCheckpointing(config.getCheckpointInterval().toMillis());
            env.getCheckpointConfig().setMinPauseBetweenCheckpoints(
                    config.getMinPauseBetweenCheckpoints().toMillis());
            env.getCheckpointConfig().setCheckpointTimeout(
                    config.getCheckpointTimeout().toMillis());
            env.getCheckpointConfig().setMaxConcurrentCheckpoints(
                    config.getMaxConcurrentCheckpoints());
            env.getCheckpointConfig().enableUnalignedCheckpoints(
                    config.isUnalignedCheckpoints());
        }

        // Configure restart strategy for resilience
        configureRestartStrategy();

        // Create table environment
        EnvironmentSettings settings = EnvironmentSettings.newInstance()
                .inStreamingMode()
                .build();

        tableEnv = StreamTableEnvironment.create(env, settings);

        log.info("Flink environment initialized successfully");
    }

    /**
     * Configure restart strategy based on configuration
     */
    private void configureRestartStrategy() {
        switch (config.getRestartStrategy().toLowerCase()) {
            case "fixed-delay":
                env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
                        config.getRestartAttempts(),
                        Time.milliseconds(config.getRestartDelay().toMillis())));
                break;
            case "exponential-delay":
                env.setRestartStrategy(RestartStrategies.exponentialDelayRestart(
                        Time.milliseconds(1000),
                        Time.milliseconds(config.getRestartDelay().toMillis() * 10),
                        2.0,
                        Time.milliseconds(config.getRestartDelay().toMillis()),
                        config.getRestartAttempts()));
                break;
            case "no-restart":
            default:
                env.setRestartStrategy(RestartStrategies.noRestart());
        }
    }

    /**
     * Execute the job using Flink SQL approach
     */
    public void executeSqlJob() throws Exception {
        initializeEnvironment();

        String traceId = java.util.UUID.randomUUID().toString();
        log.info("[traceId={}] Starting Flink SQL Transaction Tagging Job", traceId);

        try {
            // Create source table using DDL
            createSourceTable();

            // Create sink table using DDL
            createSinkTable();

            // Execute SQL transformation with custom tagging
            String insertSql = buildInsertSql();

            log.info("[traceId={}] Executing SQL: {}", traceId, insertSql);

            TableResult result = tableEnv.executeSql(insertSql);

            // Wait for job completion
            result.await();

            log.info("[traceId={}] Flink SQL job completed successfully", traceId);

        } catch (Exception e) {
            log.error("[traceId={}] Flink SQL job failed: {}", traceId, e.getMessage(), e);
            errorCount.incrementAndGet();
            throw e;
        }
    }

    /**
     * Create source table for CSV input
     */
    private void createSourceTable() {
        String createSourceTableSql = String.format(
                "CREATE TABLE transactions_source (\n" +
                        "    transaction_id STRING,\n" +
                        "    account_id STRING,\n" +
                        "    amount DECIMAL(19,4),\n" +
                        "    currency STRING,\n" +
                        "    transaction_type STRING,\n" +
                        "    counterparty_id STRING,\n" +
                        "    counterparty_name STRING,\n" +
                        "    description STRING,\n" +
                        "    transaction_time TIMESTAMP_LTZ(3),\n" +
                        "    country_code STRING,\n" +
                        "    ip_address STRING,\n" +
                        "    device_id STRING,\n" +
                        "    risk_score INT,\n" +
                        "    proctime AS PROCTIME(),\n" +
                        "    PRIMARY KEY (transaction_id) NOT ENFORCED\n" +
                        ") WITH (\n" +
                        "    'connector' = 'filesystem',\n" +
                        "    'path' = '%s',\n" +
                        "    'format' = 'csv',\n" +
                        "    'csv.ignore-parse-errors' = 'true',\n" +
                        "    'csv.allow-comments' = 'true',\n" +
                        "    'csv.field-delimiter' = ','\n" +
                        ")",
                config.getAbsoluteInputPath().replace("\\", "/")
        );

        log.info("Creating source table with SQL:\n{}", createSourceTableSql);
        tableEnv.executeSql(createSourceTableSql);
    }

    /**
     * Create sink table for CSV output
     */
    private void createSinkTable() {
        String createSinkTableSql = String.format(
                "CREATE TABLE tagged_transactions_sink (\n" +
                        "    transaction_id STRING,\n" +
                        "    account_id STRING,\n" +
                        "    amount DECIMAL(19,4),\n" +
                        "    currency STRING,\n" +
                        "    transaction_type STRING,\n" +
                        "    counterparty_id STRING,\n" +
                        "    counterparty_name STRING,\n" +
                        "    description STRING,\n" +
                        "    transaction_time TIMESTAMP_LTZ(3),\n" +
                        "    country_code STRING,\n" +
                        "    ip_address STRING,\n" +
                        "    device_id STRING,\n" +
                        "    risk_score INT,\n" +
                        "    tags STRING,\n" +
                        "    primary_tag STRING,\n" +
                        "    tag_count INT,\n" +
                        "    processing_time TIMESTAMP_LTZ(3),\n" +
                        "    trace_id STRING,\n" +
                        "    PRIMARY KEY (transaction_id) NOT ENFORCED\n" +
                        ") WITH (\n" +
                        "    'connector' = 'filesystem',\n" +
                        "    'path' = '%s',\n" +
                        "    'format' = 'csv',\n" +
                        "    'csv.field-delimiter' = ',',\n" +
                        "    'sink.parallelism' = '%d'\n" +
                        ")",
                config.getAbsoluteOutputPath().replace("\\", "/"),
                config.getParallelism()
        );

        log.info("Creating sink table with SQL:\n{}", createSinkTableSql);
        tableEnv.executeSql(createSinkTableSql);
    }

    /**
     * Build the INSERT SQL statement with tagging logic
     * Uses SQL CASE statements for simple rule-based tagging
     */
    private String buildInsertSql() {
        return "INSERT INTO tagged_transactions_sink\n" +
                "SELECT\n" +
                "    transaction_id,\n" +
                "    account_id,\n" +
                "    amount,\n" +
                "    currency,\n" +
                "    transaction_type,\n" +
                "    counterparty_id,\n" +
                "    counterparty_name,\n" +
                "    description,\n" +
                "    transaction_time,\n" +
                "    country_code,\n" +
                "    ip_address,\n" +
                "    device_id,\n" +
                "    risk_score,\n" +
                "    CONCAT_WS(',',\n" +
                "        CASE WHEN amount > 5000 THEN 'HIGH_AMOUNT' END,\n" +
                "        CASE WHEN risk_score > 70 THEN 'HIGH_RISK' END,\n" +
                "        CASE WHEN transaction_type = 'TRANSFER' THEN 'TRANSFER' END,\n" +
                "        CASE WHEN country_code IS NULL OR country_code = '' THEN 'NO_COUNTRY' END,\n" +
                "        CASE WHEN risk_score > 80 AND amount > 10000 THEN 'CRITICAL_RISK' END\n" +
                "    ) AS tags,\n" +
                "    CASE\n" +
                "        WHEN risk_score > 80 AND amount > 10000 THEN 'CRITICAL_RISK'\n" +
                "        WHEN risk_score > 70 THEN 'HIGH_RISK'\n" +
                "        WHEN amount > 5000 THEN 'HIGH_AMOUNT'\n" +
                "        WHEN transaction_type = 'TRANSFER' THEN 'TRANSFER'\n" +
                "        ELSE 'NORMAL'\n" +
                "    END AS primary_tag,\n" +
                "    (\n" +
                "        CASE WHEN amount > 5000 THEN 1 ELSE 0 END +\n" +
                "        CASE WHEN risk_score > 70 THEN 1 ELSE 0 END +\n" +
                "        CASE WHEN transaction_type = 'TRANSFER' THEN 1 ELSE 0 END +\n" +
                "        CASE WHEN country_code IS NULL OR country_code = '' THEN 1 ELSE 0 END +\n" +
                "        CASE WHEN risk_score > 80 AND amount > 10000 THEN 1 ELSE 0 END\n" +
                "    ) AS tag_count,\n" +
                "    CURRENT_TIMESTAMP AS processing_time,\n" +
                "    CONCAT('FLINK-', CAST(UUID() AS STRING)) AS trace_id\n" +
                "FROM transactions_source";
    }

    /**
     * Execute job using DataStream API with Drools integration
     */
    public void executeDataStreamJob() throws Exception {
        initializeEnvironment();

        String traceId = java.util.UUID.randomUUID().toString();
        log.info("[traceId={}] Starting Flink DataStream Transaction Tagging Job", traceId);

        try {
            // Create DataStream from table source
            DataStream<Transaction> transactionStream = createTransactionDataStream();

            // Apply watermark strategy
            DataStream<Transaction> withWatermarks = transactionStream
                    .assignTimestampsAndWatermarks(
                            WatermarkStrategy.<Transaction>forBoundedOutOfOrderness(
                                            Duration.ofSeconds(5))
                                    .withTimestampAssigner((event, timestamp) ->
                                            event.getTransactionTime() != null
                                                    ? event.getTransactionTime().toEpochMilli()
                                                    : System.currentTimeMillis())
                                    .withIdleness(Duration.ofMinutes(1))
                    );

            // Apply tagging function with Drools rules
            SingleOutputStreamOperator<TaggedTransaction> taggedStream = withWatermarks
                    .map(new TransactionTaggingFunction())
                    .name("TransactionTagging")
                    .uid("transaction-tagging-map")
                    .setParallelism(config.getParallelism())
                    .filter(Objects::nonNull)
                    .name("FilterNullResults")
                    .uid("filter-null-results");

            // Convert to Row format for CSV output
            // Define explicit row type info with field names and types
            TypeInformation<Row> rowTypeInfo = Types.ROW_NAMED(
                    new String[]{
                            "transaction_id", "account_id", "amount", "currency", "transaction_type",
                            "counterparty_id", "counterparty_name", "description", "transaction_time",
                            "country_code", "ip_address", "device_id", "risk_score",
                            "tags", "primary_tag", "tag_count", "processing_time", "trace_id"
                    },
                    Types.STRING, Types.STRING, Types.BIG_DEC, Types.STRING, Types.STRING,
                    Types.STRING, Types.STRING, Types.STRING, Types.INSTANT,
                    Types.STRING, Types.STRING, Types.STRING, Types.INT,
                    Types.STRING, Types.STRING, Types.INT, Types.INSTANT, Types.STRING
            );

            DataStream<Row> rowStream = taggedStream
                    .map(new TransactionToRowMapper()::map)
                    .returns(rowTypeInfo)
                    .name("ToRow")
                    .uid("to-row-map");

            // Create sink table for output
            createSinkTableForDataStream();

            // Convert DataStream to Table and insert
            Table resultTable = tableEnv.fromDataStream(rowStream);
            tableEnv.createTemporaryView("tagged_data", resultTable);

            String insertSql = "INSERT INTO tagged_transactions_sink SELECT * FROM tagged_data";
            TableResult result = tableEnv.executeSql(insertSql);

            // Wait for completion
            result.await();

            log.info("[traceId={}] Flink DataStream job completed successfully", traceId);

        } catch (Exception e) {
            log.error("[traceId={}] Flink DataStream job failed: {}", traceId, e.getMessage(), e);
            errorCount.incrementAndGet();
            throw e;
        }
    }

    /**
     * Create DataStream from source table
     */
    private DataStream<Transaction> createTransactionDataStream() {
        createSourceTable();

        // Read from table and convert to Transaction objects
        Table sourceTable = tableEnv.sqlQuery(
                "SELECT * FROM transactions_source"
        );

        return tableEnv.toDataStream(sourceTable)
                .map(row -> {
                    Transaction tx = new Transaction();
                    tx.setTransactionId(row.getFieldAs("transaction_id"));
                    tx.setAccountId(row.getFieldAs("account_id"));
                    tx.setAmount(row.getFieldAs("amount"));
                    tx.setCurrency(row.getFieldAs("currency"));
                    tx.setTransactionType(row.getFieldAs("transaction_type"));
                    tx.setCounterpartyId(row.getFieldAs("counterparty_id"));
                    tx.setCounterpartyName(row.getFieldAs("counterparty_name"));
                    tx.setDescription(row.getFieldAs("description"));

                    java.time.LocalDateTime localTime = row.getFieldAs("transaction_time");
                    if (localTime != null) {
                        tx.setTransactionTime(localTime.atZone(
                                java.time.ZoneId.systemDefault()).toInstant());
                    }

                    tx.setCountryCode(row.getFieldAs("country_code"));
                    tx.setIpAddress(row.getFieldAs("ip_address"));
                    tx.setDeviceId(row.getFieldAs("device_id"));
                    tx.setRiskScore(row.getFieldAs("risk_score"));
                    tx.initialize();

                    return tx;
                })
                .returns(TypeInformation.of(Transaction.class));
    }

    /**
     * Create sink table for DataStream output
     */
    private void createSinkTableForDataStream() {
        createSinkTable();
    }

    /**
     * Convert TaggedTransaction to Row
     */
    private Row toRow(TaggedTransaction tx) {
        return TransactionToRowMapper.toRowStatic(tx);
    }

    /**
     * Mapper to convert TaggedTransaction to Row
     * Must be Serializable for Flink distributed execution
     */
    private static class TransactionToRowMapper implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        public Row map(TaggedTransaction tx) {
            return toRowStatic(tx);
        }

        public static Row toRowStatic(TaggedTransaction tx) {
            return Row.of(
                    tx.getTransactionId(),
                    tx.getAccountId(),
                    tx.getAmount(),
                    tx.getCurrency(),
                    tx.getTransactionType(),
                    tx.getCounterpartyId(),
                    tx.getCounterpartyName(),
                    tx.getDescription(),
                    tx.getTransactionTime(),
                    tx.getCountryCode(),
                    tx.getIpAddress(),
                    tx.getDeviceId(),
                    tx.getRiskScore(),
                    tx.getTags(),
                    tx.getPrimaryTag(),
                    tx.getTagCount(),
                    tx.getProcessingTime(),
                    tx.getTraceId()
            );
        }
    }

    /**
     * Execute hybrid job combining SQL and DataStream APIs
     */
    public void executeHybridJob() throws Exception {
        initializeEnvironment();

        String traceId = java.util.UUID.randomUUID().toString();
        log.info("[traceId={}] Starting Hybrid Flink Job", traceId);

        try {
            // Create source table
            createSourceTable();

            // Use SQL for initial filtering and transformation
            Table filteredTable = tableEnv.sqlQuery(
                    "SELECT * FROM transactions_source " +
                            "WHERE amount IS NOT NULL AND account_id IS NOT NULL"
            );

            // Convert to DataStream for complex processing with Drools
            DataStream<Transaction> stream = tableEnv.toDataStream(filteredTable)
                    .map(row -> {
                        Transaction tx = new Transaction();
                        tx.setTransactionId(row.getFieldAs("transaction_id"));
                        tx.setAccountId(row.getFieldAs("account_id"));
                        tx.setAmount(row.getFieldAs("amount"));
                        tx.setCurrency(row.getFieldAs("currency"));
                        tx.setTransactionType(row.getFieldAs("transaction_type"));
                        tx.setCounterpartyId(row.getFieldAs("counterparty_id"));
                        tx.setCounterpartyName(row.getFieldAs("counterparty_name"));
                        tx.setDescription(row.getFieldAs("description"));

                        java.time.LocalDateTime localTime = row.getFieldAs("transaction_time");
                        if (localTime != null) {
                            tx.setTransactionTime(localTime.atZone(
                                    java.time.ZoneId.systemDefault()).toInstant());
                        }

                        tx.setCountryCode(row.getFieldAs("country_code"));
                        tx.setIpAddress(row.getFieldAs("ip_address"));
                        tx.setDeviceId(row.getFieldAs("device_id"));
                        tx.setRiskScore(row.getFieldAs("risk_score"));
                        tx.initialize();

                        return tx;
                    })
                    .returns(TypeInformation.of(Transaction.class));

            // Apply Drools tagging
            DataStream<TaggedTransaction> taggedStream = stream
                    .map(new TransactionTaggingFunction())
                    .filter(Objects::nonNull);

            // Convert back to Table for SQL-based aggregation
            Table taggedTable = tableEnv.fromDataStream(taggedStream);
            tableEnv.createTemporaryView("tagged", taggedTable);

            // Use SQL for final output
            Table resultTable = tableEnv.sqlQuery(
                    "SELECT * FROM tagged"
            );

            // Create sink and insert
            createSinkTable();
            tableEnv.executeSql("INSERT INTO tagged_transactions_sink SELECT * FROM " + resultTable);

            log.info("[traceId={}] Hybrid job submitted successfully", traceId);

        } catch (Exception e) {
            log.error("[traceId={}] Hybrid job failed: {}", traceId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get processing metrics
     */
    public String getMetrics() {
        return String.format(
                "TransactionTaggingJob{processed=%d, errors=%d}",
                processedCount.get(), errorCount.get()
        );
    }
}
