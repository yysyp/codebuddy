package com.example.flink.udf;

import com.example.flink.model.Transaction;
import com.example.flink.rules.DroolsRuleEngine;
import com.example.flink.rules.TaggingResult;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.ScalarFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simplified Flink SQL Scalar UDF that returns tagging result as a delimited string.
 * Format: RISK_LEVEL|TAGS|APPLIED_RULES
 * Easier to use in SQL queries than structured types.
 */
public class TaggingStringUdf extends ScalarFunction {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(TaggingStringUdf.class);
    
    private static final String FIELD_SEPARATOR = "|";
    private static final String DEFAULT_RESULT = "LOW||";
    
    private transient DroolsRuleEngine ruleEngine;
    private transient AtomicLong processCounter;
    private transient AtomicLong errorCounter;
    
    @Override
    public void open(FunctionContext context) throws Exception {
        LOG.info("Opening TaggingStringUdf...");
        ruleEngine = new DroolsRuleEngine();
        processCounter = new AtomicLong(0);
        errorCounter = new AtomicLong(0);
        
        // Initialize Drools engine
        DroolsRuleEngine.initialize();
        LOG.info("TaggingStringUdf opened successfully");
    }
    
    /**
     * Evaluates tagging rules and returns result as delimited string.
     * Format: RISK_LEVEL|TAGS|APPLIED_RULES
     */
    public String eval(
            String transactionId,
            String accountId,
            BigDecimal amount,
            String currency,
            String transactionType) {
        
        return evalWithDetails(transactionId, accountId, null, amount, currency, 
                transactionType, null, null, null, null);
    }
    
    /**
     * Full evaluation with all transaction details.
     */
    public String evalWithDetails(
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
            // Validate required fields
            if (transactionId == null || transactionId.isBlank()) {
                LOG.warn("Invalid transactionId provided to UDF");
                return "MEDIUM|INVALID_DATA|VALIDATION_ERROR";
            }
            
            // Build transaction object with safe defaults
            Transaction transaction = Transaction.builder()
                    .transactionId(transactionId.trim())
                    .accountId(accountId != null ? accountId.trim() : "UNKNOWN")
                    .counterpartyAccount(counterpartyAccount != null ? counterpartyAccount.trim() : null)
                    .amount(amount != null ? amount : BigDecimal.ZERO)
                    .currency(currency != null ? currency.toUpperCase().trim() : "USD")
                    .transactionType(transactionType != null ? transactionType.toUpperCase().trim() : "UNKNOWN")
                    .channel(channel != null ? channel.toUpperCase().trim() : null)
                    .countryCode(countryCode != null ? countryCode.toUpperCase().trim() : null)
                    .transactionTime(transactionTime != null ? transactionTime : Instant.now())
                    .description(description != null ? description.trim() : null)
                    .build();
            
            // Apply rules
            TaggingResult result = ruleEngine.applyRules(transaction);
            
            long durationNanos = System.nanoTime() - startTime;
            long count = processCounter.incrementAndGet();
            
            // Log periodic progress
            if (count % 10000 == 0) {
                LOG.info("Processed {} transactions. Last latency: {} µs", 
                        count, durationNanos / 1000);
            }
            
            LOG.debug("Tagged transaction {} in {} ns: risk={}, tags={}", 
                    transactionId, durationNanos, result.getRiskLevel(), result.getTagsAsString());
            
            // Format result
            return formatResult(result);
            
        } catch (Exception e) {
            long errors = errorCounter.incrementAndGet();
            LOG.error("Error tagging transaction {} (total errors: {}): {}", 
                    transactionId, errors, e.getMessage());
            
            // Return safe default on error to allow processing to continue
            return "MEDIUM|RULE_ERROR|ERROR";
        }
    }
    
    /**
     * Alternative evaluation method with string amount (for SQL compatibility).
     */
    public String evalWithStringAmount(
            String transactionId,
            String accountId,
            String amountStr,
            String currency,
            String transactionType) {
        
        BigDecimal amount;
        try {
            amount = amountStr != null ? new BigDecimal(amountStr) : BigDecimal.ZERO;
        } catch (NumberFormatException e) {
            LOG.warn("Invalid amount format: {}", amountStr);
            amount = BigDecimal.ZERO;
        }
        
        return eval(transactionId, accountId, amount, currency, transactionType);
    }
    
    private String formatResult(TaggingResult result) {
        if (result == null) {
            return DEFAULT_RESULT;
        }
        
        String riskLevel = result.getRiskLevel() != null ? result.getRiskLevel() : "LOW";
        String tags = result.getTagsAsString() != null ? result.getTagsAsString() : "";
        String rules = result.getAppliedRulesAsString() != null ? result.getAppliedRulesAsString() : "";
        
        return riskLevel + FIELD_SEPARATOR + tags + FIELD_SEPARATOR + rules;
    }
    
    @Override
    public void close() throws Exception {
        long totalProcessed = processCounter != null ? processCounter.get() : 0;
        long totalErrors = errorCounter != null ? errorCounter.get() : 0;
        
        LOG.info("Closing TaggingStringUdf. Total processed: {}, Errors: {}, Success rate: {}%",
                totalProcessed, totalErrors,
                totalProcessed > 0 ? (100.0 * (totalProcessed - totalErrors) / totalProcessed) : 0);
        
        DroolsRuleEngine.cleanup();
    }
}
