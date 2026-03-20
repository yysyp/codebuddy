package com.etl.data.service;

import com.etl.data.client.ControlPanelClient;
import com.etl.data.dto.RuleDefinitionDto;
import com.etl.data.dto.SqlDefinitionDto;
import com.etl.data.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.functions.ScalarFunction;
import org.kie.api.runtime.KieContainer;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Transaction Tagging Job
 * Flink job for reading transactions, applying rules, and writing tagged results
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionTaggingJob {

    private final ControlPanelClient controlPanelClient;
    private final DroolsRuleService droolsRuleService;

    /**
     * Custom UDF for concatenating tags
     */
    public static class ConcatTagsFunction extends ScalarFunction {
        public String eval(java.util.Set<String> tags) {
            if (tags == null || tags.isEmpty()) {
                return "";
            }
            return String.join(",", tags);
        }
    }

    /**
     * Execute the transaction tagging job
     */
    public void execute(String inputPath, String outputPath) throws Exception {
        log.info("Starting Transaction Tagging Job");
        log.info("Input path: {}", inputPath);
        log.info("Output path: {}", outputPath);

        // Get published rules from Control Panel
        List<RuleDefinitionDto> rules = controlPanelClient.getPublishedRules();
        log.info("Loaded {} published rules from Control Panel", rules.size());

        // Get SQL definitions from Control Panel
        SqlDefinitionDto sourceTableSql = controlPanelClient.getSqlByName("transaction-source-table");
        SqlDefinitionDto sinkTableSql = controlPanelClient.getSqlByName("tagged-transaction-sink-table");
        log.info("Loaded SQL definitions from Control Panel");

        // Build KieContainer from rules
        KieContainer kieContainer = droolsRuleService.buildKieContainer(rules);

        // Create Flink execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // Create Table Environment
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // Register UDF
        tableEnv.createTemporarySystemFunction("CONCAT_TAGS", ConcatTagsFunction.class);

        // Replace placeholders in SQL
        String sourceSql = sourceTableSql.getSqlContent()
                .replace("file:///data/input/transactions.csv", inputPath);
        String sinkSql = sinkTableSql.getSqlContent()
                .replace("file:///data/output/tagged_transactions.csv", outputPath);

        // Register source table
        log.info("Registering source table with SQL:\n{}", sourceSql);
        tableEnv.executeSql(sourceSql);

        // Register sink table
        log.info("Registering sink table with SQL:\n{}", sinkSql);
        tableEnv.executeSql(sinkSql);

        // Read transactions from source table
        Table transactionTable = tableEnv.sqlQuery("SELECT * FROM transactions");

        // Convert Table to DataStream
        DataStream<Transaction> transactionStream = tableEnv.toDataStream(transactionTable)
                .map(row -> {
                    Transaction transaction = Transaction.builder()
                            .transactionId(row.getFieldAs("transaction_id"))
                            .userId(row.getFieldAs("user_id"))
                            .amount(row.getFieldAs("amount"))
                            .currency(row.getFieldAs("currency"))
                            .transactionType(row.getFieldAs("transaction_type"))
                            .transactionTime(row.getFieldAs("transaction_time"))
                            .merchantId(row.getFieldAs("merchant_id"))
                            .location(row.getFieldAs("location"))
                            .status(row.getFieldAs("status"))
                            .build();
                    return transaction;
                });

        // Apply rules and tag transactions
        DataStream<Transaction> taggedStream = transactionStream.map(transaction -> {
            try {
                Transaction tagged = droolsRuleService.applyRules(transaction, kieContainer);
                log.debug("Tagged transaction {} with tags: {}", 
                        transaction.getTransactionId(), tagged.getTagsAsString());
                return tagged;
            } catch (Exception e) {
                log.error("Error tagging transaction: {}", transaction.getTransactionId(), e);
                // Add error tag
                transaction.addTag("TAGGING_ERROR");
                return transaction;
            }
        });

        // Convert tagged stream to Table with tags as string
        DataStream<TaggedTransaction> taggedWithString = taggedStream.map(transaction -> {
            TaggedTransaction tagged = new TaggedTransaction();
            tagged.setTransactionId(transaction.getTransactionId());
            tagged.setUserId(transaction.getUserId());
            tagged.setAmount(transaction.getAmount());
            tagged.setCurrency(transaction.getCurrency());
            tagged.setTransactionType(transaction.getTransactionType());
            tagged.setTransactionTime(transaction.getTransactionTime());
            tagged.setMerchantId(transaction.getMerchantId());
            tagged.setLocation(transaction.getLocation());
            tagged.setStatus(transaction.getStatus());
            tagged.setTags(transaction.getTagsAsString());
            return tagged;
        });

        // Register as table and insert
        tableEnv.createTemporaryView("tagged_transactions_temp", taggedWithString);

        // Insert into sink table
        log.info("Inserting tagged transactions into sink table");
        TableResult result = tableEnv.executeSql(
                "INSERT INTO tagged_transactions " +
                "SELECT " +
                "  transactionId, " +
                "  userId, " +
                "  amount, " +
                "  currency, " +
                "  transactionType, " +
                "  transactionTime, " +
                "  merchantId, " +
                "  location, " +
                "  status, " +
                "  tags " +
                "FROM tagged_transactions_temp"
        );

        // Wait for job completion
        result.await();
        log.info("Transaction Tagging Job completed successfully");
    }

    /**
     * Execute job with default paths
     */
    public void executeWithDefaults() throws Exception {
        String inputPath = "file:///data/input/transactions.csv";
        String outputPath = "file:///data/output/tagged_transactions.csv";
        execute(inputPath, outputPath);
    }

    /**
     * Helper class for tagged transactions with string tags
     */
    public static class TaggedTransaction {
        private String transactionId;
        private String userId;
        private java.math.BigDecimal amount;
        private String currency;
        private String transactionType;
        private java.time.Instant transactionTime;
        private String merchantId;
        private String location;
        private String status;
        private String tags;

        // Getters and setters
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public java.math.BigDecimal getAmount() { return amount; }
        public void setAmount(java.math.BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
        public java.time.Instant getTransactionTime() { return transactionTime; }
        public void setTransactionTime(java.time.Instant transactionTime) { this.transactionTime = transactionTime; }
        public String getMerchantId() { return merchantId; }
        public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
    }
}
