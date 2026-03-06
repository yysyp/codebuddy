package com.example.flink.function;

import com.example.flink.model.TaggingResult;
import com.example.flink.model.Transaction;
import com.example.flink.service.RuleEngineService;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.functions.ScalarFunction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Flink Scalar Function for transaction tagging using Drools rule engine.
 * This UDF can be used in Flink SQL to apply Drools rules to transaction data.
 */
@Slf4j
public class DroolsTaggingUDF extends ScalarFunction {

    private static final long serialVersionUID = 1L;

    private transient RuleEngineService ruleEngineService;

    /**
     * Initialize the rule engine service when the UDF is opened
     */
    public void open() {
        if (ruleEngineService == null) {
            try {
                ruleEngineService = new RuleEngineService();
                ruleEngineService.initialize();
                log.info("DroolsTaggingUDF initialized. Rule engine ready: {}",
                        ruleEngineService.isReady());
            } catch (Exception e) {
                log.error("Failed to initialize DroolsTaggingUDF: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Close the rule engine service
     */
    public void close() {
        if (ruleEngineService != null) {
            try {
                ruleEngineService.cleanup();
                ruleEngineService = null;
            } catch (Exception e) {
                log.error("Error closing DroolsTaggingUDF: {}", e.getMessage());
            }
        }
    }

    /**
     * Apply Drools rules to transaction data and return tagging results
     *
     * @param transactionId   Transaction identifier
     * @param accountId       Account identifier
     * @param amount          Transaction amount
     * @param currency        Currency code
     * @param transactionType Transaction type
     * @param counterpartyId  Counterparty ID
     * @param counterpartyName Counterparty name
     * @param description     Transaction description
     * @param transactionTime Transaction timestamp
     * @param countryCode     Country code
     * @param ipAddress       IP address
     * @param deviceId        Device ID
     * @param riskScore       Risk score
     * @return Array containing [tags, primary_tag, tag_count]
     */
    public String[] eval(
            String transactionId,
            String accountId,
            @DataTypeHint("DECIMAL(19,4)") BigDecimal amount,
            String currency,
            String transactionType,
            String counterpartyId,
            String counterpartyName,
            String description,
            @DataTypeHint("TIMESTAMP_LTZ(3)") Instant transactionTime,
            String countryCode,
            String ipAddress,
            String deviceId,
            Integer riskScore
    ) {
        // Ensure rule engine is initialized
        if (ruleEngineService == null) {
            open();
        }

        // Create Transaction object from input parameters
        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .accountId(accountId)
                .amount(amount)
                .currency(currency)
                .transactionType(transactionType)
                .counterpartyId(counterpartyId)
                .counterpartyName(counterpartyName)
                .description(description)
                .transactionTime(transactionTime)
                .countryCode(countryCode)
                .ipAddress(ipAddress)
                .deviceId(deviceId)
                .riskScore(riskScore)
                .tags(new ArrayList<>())
                .traceId(UUID.randomUUID().toString())
                .build();

        try {
            // Execute Drools rules
            if (ruleEngineService != null && ruleEngineService.isReady()) {
                ruleEngineService.executeRules(transaction);
            } else {
                log.warn("[transactionId={}] Rule engine not ready, returning empty tags",
                        transactionId);
                transaction.addTag("NO_RULES");
            }

            // Extract tagging results
            String tags = transaction.getTagsAsString();
            String primaryTag = (transaction.getTags() != null && !transaction.getTags().isEmpty())
                    ? transaction.getTags().get(0)
                    : "UNTAGGED";
            int tagCount = transaction.getTags() != null ? transaction.getTags().size() : 0;

            return new String[]{tags, primaryTag, String.valueOf(tagCount)};

        } catch (Exception e) {
            log.error("[transactionId={}] Error in DroolsTaggingUDF: {}",
                    transactionId, e.getMessage(), e);

            // Return error tags
            return new String[]{"PROCESSING_ERROR", "PROCESSING_ERROR", "1"};
        }
    }
}
