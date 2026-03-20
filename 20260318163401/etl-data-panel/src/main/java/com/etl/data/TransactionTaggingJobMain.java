package com.etl.data;

import com.etl.data.client.ControlPanelClient;
import com.etl.data.dto.RuleDefinitionDto;
import com.etl.data.dto.SqlDefinitionDto;
import com.etl.data.model.Transaction;
import com.etl.data.service.DroolsRuleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.kie.api.runtime.KieContainer;

import java.util.List;

/**
 * Transaction Tagging Job Main
 * Standalone Flink job for transaction tagging
 * Can be run independently without Spring Boot
 */
public class TransactionTaggingJobMain {

    public static void main(String[] args) throws Exception {
        // Parse command line arguments
        String controlPanelUrl = args.length > 0 ? args[0] : "http://localhost:8080";
        String inputPath = args.length > 1 ? args[1] : "file:///data/input/transactions.csv";
        String outputPath = args.length > 2 ? args[2] : "file:///data/output/tagged_transactions.csv";

        System.out.println("========================================");
        System.out.println("Transaction Tagging Job");
        System.out.println("========================================");
        System.out.println("Control Panel URL: " + controlPanelUrl);
        System.out.println("Input Path: " + inputPath);
        System.out.println("Output Path: " + outputPath);
        System.out.println();

        // Create ObjectMapper with JSR310 module
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Create Control Panel Client
        ControlPanelClient controlPanelClient = new ControlPanelClient(controlPanelUrl, objectMapper);

        // Create Drools Rule Service
        DroolsRuleService droolsRuleService = new DroolsRuleService();

        // Get published rules from Control Panel
        System.out.println("Fetching published rules from Control Panel...");
        List<RuleDefinitionDto> rules = controlPanelClient.getPublishedRules();
        System.out.println("Loaded " + rules.size() + " published rules");

        // Get SQL definitions from Control Panel
        System.out.println("Fetching SQL definitions from Control Panel...");
        SqlDefinitionDto sourceTableSql = controlPanelClient.getSqlByName("transaction-source-table");
        SqlDefinitionDto sinkTableSql = controlPanelClient.getSqlByName("tagged-transaction-sink-table");
        System.out.println("Loaded SQL definitions");

        // Build KieContainer from rules
        System.out.println("Building Drools rule engine...");
        KieContainer kieContainer = droolsRuleService.buildKieContainer(rules);
        System.out.println("Rule engine built successfully");

        // Create Flink execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // Create Table Environment
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // Replace placeholders in SQL
        String sourceSql = sourceTableSql.getSqlContent()
                .replace("file:///data/input/transactions.csv", inputPath);
        String sinkSql = sinkTableSql.getSqlContent()
                .replace("file:///data/output/tagged_transactions.csv", outputPath);

        // Register source table
        System.out.println("Registering source table...");
        tableEnv.executeSql(sourceSql);

        // Register sink table
        System.out.println("Registering sink table...");
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
        DataStream<TaggedTransaction> taggedStream = transactionStream.map(transaction -> {
            try {
                Transaction tagged = droolsRuleService.applyRules(transaction, kieContainer);
                System.out.println("Tagged transaction " + transaction.getTransactionId() + 
                        " with tags: " + tagged.getTagsAsString());
                
                // Convert to TaggedTransaction
                TaggedTransaction result = new TaggedTransaction();
                result.setTransactionId(tagged.getTransactionId());
                result.setUserId(tagged.getUserId());
                result.setAmount(tagged.getAmount());
                result.setCurrency(tagged.getCurrency());
                result.setTransactionType(tagged.getTransactionType());
                result.setTransactionTime(tagged.getTransactionTime());
                result.setMerchantId(tagged.getMerchantId());
                result.setLocation(tagged.getLocation());
                result.setStatus(tagged.getStatus());
                result.setTags(tagged.getTagsAsString());
                return result;
            } catch (Exception e) {
                System.err.println("Error tagging transaction: " + transaction.getTransactionId());
                e.printStackTrace();
                transaction.addTag("TAGGING_ERROR");
                
                // Convert to TaggedTransaction with error
                TaggedTransaction result = new TaggedTransaction();
                result.setTransactionId(transaction.getTransactionId());
                result.setUserId(transaction.getUserId());
                result.setAmount(transaction.getAmount());
                result.setCurrency(transaction.getCurrency());
                result.setTransactionType(transaction.getTransactionType());
                result.setTransactionTime(transaction.getTransactionTime());
                result.setMerchantId(transaction.getMerchantId());
                result.setLocation(transaction.getLocation());
                result.setStatus(transaction.getStatus());
                result.setTags(transaction.getTagsAsString());
                return result;
            }
        });

        // Register as table and insert
        tableEnv.createTemporaryView("tagged_transactions_temp", taggedStream);

        // Insert into sink table
        System.out.println("Inserting tagged transactions into sink table...");
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
        
        System.out.println();
        System.out.println("========================================");
        System.out.println("Job completed successfully!");
        System.out.println("========================================");
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
