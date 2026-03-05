package com.example.flink.udf;

import com.example.flink.model.Transaction;
import com.example.flink.rules.DroolsRuleEngine;
import com.example.flink.rules.TaggingResult;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.ScalarFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Flink SQL Scalar UDF for applying Drools-based tagging rules to transactions.
 * Returns a structured result containing risk level, tags, and applied rules.
 */
@FunctionHint(
    output = @DataTypeHint("ROW<risk_level STRING, tags STRING, applied_rules STRING>")
)
public class TaggingUdf extends ScalarFunction {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(TaggingUdf.class);
    
    private transient DroolsRuleEngine ruleEngine;
    private transient AtomicLong processCounter;
    private transient AtomicLong errorCounter;
    
    @Override
    public void open(FunctionContext context) throws Exception {
        LOG.info("Opening TaggingUdf...");
        ruleEngine = new DroolsRuleEngine();
        processCounter = new AtomicLong(0);
        errorCounter = new AtomicLong(0);
        
        // Initialize Drools engine
        DroolsRuleEngine.initialize();
        LOG.info("TaggingUdf opened successfully");
    }
    
    /**
     * Evaluates tagging rules for a transaction.
     * Returns a ROW with: risk_level, tags, applied_rules
     */
    public RowData eval(
            String transactionId,
            String accountId,
            String counterpartyAccount,
            BigDecimal amount,
            String currency,
            String transactionType,
            String channel,
            String countryCode,
            Instant transactionTime,
            String description) {
        
        long startTime = System.nanoTime();
        
        try {
            // Build transaction object
            Transaction transaction = Transaction.builder()
                    .transactionId(transactionId)
                    .accountId(accountId)
                    .counterpartyAccount(counterpartyAccount)
                    .amount(amount != null ? amount : BigDecimal.ZERO)
                    .currency(currency != null ? currency : "USD")
                    .transactionType(transactionType != null ? transactionType : "UNKNOWN")
                    .channel(channel)
                    .countryCode(countryCode)
                    .transactionTime(transactionTime != null ? transactionTime : Instant.now())
                    .description(description)
                    .build();
            
            // Apply rules
            TaggingResult result = ruleEngine.applyRules(transaction);
            
            long durationNanos = System.nanoTime() - startTime;
            long count = processCounter.incrementAndGet();
            
            if (count % 1000 == 0) {
                LOG.info("Processed {} transactions. Last processing time: {} ns", count, durationNanos);
            }
            
            LOG.debug("Tagged transaction {}: risk={}, tags={}", 
                    transactionId, result.getRiskLevel(), result.getTagsAsString());
            
            // Return result as RowData
            return new RowData(
                result.getRiskLevel(),
                result.getTagsAsString(),
                result.getAppliedRulesAsString()
            );
            
        } catch (Exception e) {
            errorCounter.incrementAndGet();
            LOG.error("Error tagging transaction {}: {}", transactionId, e.getMessage(), e);
            
            // Return safe default on error
            return new RowData("MEDIUM", "RULE_ERROR", "ERROR:" + e.getMessage());
        }
    }
    
    @Override
    public void close() throws Exception {
        LOG.info("Closing TaggingUdf. Total processed: {}, Errors: {}", 
                processCounter != null ? processCounter.get() : 0,
                errorCounter != null ? errorCounter.get() : 0);
        DroolsRuleEngine.cleanup();
    }
    
    /**
     * Simple row data holder for UDF output.
     */
    public static class RowData {
        public final String riskLevel;
        public final String tags;
        public final String appliedRules;
        
        public RowData(String riskLevel, String tags, String appliedRules) {
            this.riskLevel = riskLevel != null ? riskLevel : "LOW";
            this.tags = tags != null ? tags : "";
            this.appliedRules = appliedRules != null ? appliedRules : "";
        }
        
        @Override
        public String toString() {
            return String.format("RowData{riskLevel='%s', tags='%s', appliedRules='%s'}",
                    riskLevel, tags, appliedRules);
        }
    }
}
