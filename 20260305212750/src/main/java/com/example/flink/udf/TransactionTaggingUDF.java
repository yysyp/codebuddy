package com.example.flink.udf;

import com.example.flink.model.TaggedTransaction;
import com.example.flink.model.Transaction;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.types.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

/**
 * Flink Table Function for tagging transactions.
 * This UDF wraps the Drools rule engine execution.
 * 
 * Note: For actual rule execution in distributed environment,
 * rules should be evaluated in a RichMapFunction or ProcessFunction
 * that can properly manage the rule engine lifecycle.
 */
@FunctionHint(output = @DataTypeHint("ROW<" +
    "transactionId BIGINT, " +
    "accountId STRING, " +
    "amount DECIMAL(18,2), " +
    "transactionType STRING, " +
    "counterparty STRING, " +
    "transactionTime TIMESTAMP(3), " +
    "currency STRING, " +
    "channel STRING, " +
    "location STRING, " +
    "description STRING, " +
    "riskScore INT, " +
    "tags STRING, " +
    "processingTime TIMESTAMP(3), " +
    "traceId STRING" +
    ">"))
public class TransactionTaggingUDF extends TableFunction<Row> {
    
    private static final Logger LOG = LoggerFactory.getLogger(TransactionTaggingUDF.class);
    
    // Transient to prevent serialization issues
    private transient org.kie.api.runtime.StatelessKieSession kieSession;
    
    /**
     * Evaluates a transaction using Drools rules.
     * Note: In production, this should use a shared RuleEngine
     * initialized in open() method of RichFunction.
     */
    public void eval(
            Long transactionId,
            String accountId,
            BigDecimal amount,
            String transactionType,
            String counterparty,
            Timestamp transactionTime,
            String currency,
            String channel,
            String location,
            String description) {
        
        String traceId = generateTraceId();
        Instant startTime = Instant.now();
        
        try {
            // Build Transaction object
            Transaction transaction = Transaction.builder()
                    .transactionId(transactionId)
                    .accountId(accountId)
                    .amount(amount)
                    .transactionType(transactionType)
                    .counterparty(counterparty)
                    .transactionTime(transactionTime != null ? transactionTime.toInstant() : null)
                    .currency(currency)
                    .channel(channel)
                    .location(location)
                    .description(description)
                    .build();
            
            // Execute rules - for embedded UDF, we use simplified rule evaluation
            // In production, this should delegate to a RuleEngine service
            executeSimpleRules(transaction);
            
            // Build output row
            Row outputRow = buildOutputRow(transaction, traceId);
            
            // Emit result
            collect(outputRow);
            
            LOG.debug("Transaction {} tagged successfully. Tags: {}, RiskScore: {}",
                    transactionId, transaction.getTags(), transaction.getRiskScore());
            
        } catch (Exception e) {
            LOG.error("Error tagging transaction {}: {}", transactionId, e.getMessage(), e);
            // Emit row with error indication
            Row errorRow = buildErrorRow(transactionId, accountId, amount, traceId, e.getMessage());
            collect(errorRow);
        }
    }
    
    /**
     * Simple rule evaluation without full Drools initialization.
     * For production use, this should use a proper RuleEngine with caching.
     */
    private void executeSimpleRules(Transaction t) {
        // High Value Transaction
        if (t.getAmount() != null && t.getAmount().compareTo(new BigDecimal("50000")) > 0) {
            t.addTag("HIGH_VALUE");
            t.increaseRiskScore(20);
        }
        
        // Very High Value
        if (t.getAmount() != null && t.getAmount().compareTo(new BigDecimal("100000")) > 0) {
            t.addTag("VERY_HIGH_VALUE");
            t.increaseRiskScore(30);
        }
        
        // Off Hours
        if (t.isOutsideBusinessHours()) {
            t.addTag("OFF_HOURS");
            t.increaseRiskScore(15);
        }
        
        // High Risk Location
        if (t.getLocation() != null && t.getLocation().matches(".*(XX|YY|ZZ).*")) {
            t.addTag("HIGH_RISK_LOCATION");
            t.increaseRiskScore(40);
        }
        
        // International
        if (t.getLocation() != null && !t.getLocation().equals("US") && !t.getLocation().equals("CN")) {
            t.addTag("INTERNATIONAL");
            t.increaseRiskScore(10);
        }
        
        // Unknown Counterparty
        if (t.getCounterparty() != null && t.getCounterparty().contains("Unknown")) {
            t.addTag("UNKNOWN_COUNTERPARTY");
            t.increaseRiskScore(25);
        }
        
        // Cash Related
        if ("WITHDRAWAL".equals(t.getTransactionType()) || "DEPOSIT".equals(t.getTransactionType())) {
            t.addTag("CASH_RELATED");
            t.increaseRiskScore(5);
        }
        
        // Automated
        if ("API".equals(t.getChannel())) {
            t.addTag("AUTOMATED");
        }
        
        // Suspicious Pattern
        if (t.getAmount() != null && t.getAmount().compareTo(new BigDecimal("25000")) > 0 
                && t.isOutsideBusinessHours()) {
            t.addTag("SUSPICIOUS_PATTERN");
            t.increaseRiskScore(35);
        }
        
        // Risk Level Assignment
        if (t.getRiskScore() >= 50) {
            t.addTag("HIGH_RISK");
        } else if (t.getRiskScore() >= 20) {
            t.addTag("MEDIUM_RISK");
        } else {
            t.addTag("LOW_RISK");
        }
    }
    
    private Row buildOutputRow(Transaction t, String traceId) {
        Row row = new Row(14);
        row.setField(0, t.getTransactionId());
        row.setField(1, t.getAccountId());
        row.setField(2, t.getAmount());
        row.setField(3, t.getTransactionType());
        row.setField(4, t.getCounterparty());
        row.setField(5, t.getTransactionTime() != null ? Timestamp.from(t.getTransactionTime()) : null);
        row.setField(6, t.getCurrency());
        row.setField(7, t.getChannel());
        row.setField(8, t.getLocation());
        row.setField(9, t.getDescription());
        row.setField(10, t.getRiskScore());
        row.setField(11, String.join(",", t.getTags()));
        row.setField(12, Timestamp.from(Instant.now()));
        row.setField(13, traceId);
        return row;
    }
    
    private Row buildErrorRow(Long transactionId, String accountId, BigDecimal amount, 
                             String traceId, String errorMessage) {
        Row row = new Row(14);
        row.setField(0, transactionId);
        row.setField(1, accountId);
        row.setField(2, amount);
        row.setField(3, null);
        row.setField(4, null);
        row.setField(5, null);
        row.setField(6, null);
        row.setField(7, null);
        row.setField(8, null);
        row.setField(9, "ERROR: " + errorMessage);
        row.setField(10, -1);
        row.setField(11, "ERROR");
        row.setField(12, Timestamp.from(Instant.now()));
        row.setField(13, traceId);
        return row;
    }
    
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
