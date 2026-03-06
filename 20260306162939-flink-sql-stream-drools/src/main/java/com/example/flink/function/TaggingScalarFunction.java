package com.example.flink.function;

import com.example.flink.model.Transaction;
import com.example.flink.service.RuleEngineService;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.functions.FunctionContext;
import org.apache.flink.table.functions.ScalarFunction;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Scalar function for Flink SQL that tags a transaction using Drools rules.
 * Can be used directly in SQL queries.
 */
@Slf4j
@FunctionHint(
        input = {
                @DataTypeHint("STRING"),    // transaction_id
                @DataTypeHint("STRING"),    // account_id
                @DataTypeHint("DECIMAL(19,4)"), // amount
                @DataTypeHint("STRING"),    // currency
                @DataTypeHint("STRING"),    // transaction_type
                @DataTypeHint("STRING"),    // counterparty_id
                @DataTypeHint("STRING"),    // counterparty_name
                @DataTypeHint("STRING"),    // description
                @DataTypeHint("TIMESTAMP_LTZ(3)"), // transaction_time
                @DataTypeHint("STRING"),    // country_code
                @DataTypeHint("STRING"),    // ip_address
                @DataTypeHint("STRING"),    // device_id
                @DataTypeHint("INT")        // risk_score
        },
        output = @DataTypeHint("STRING")  // comma-separated tags
)
public class TaggingScalarFunction extends ScalarFunction implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient RuleEngineService ruleEngineService;
    private volatile boolean initialized = false;

    @Override
    public void open(FunctionContext context) throws Exception {
        super.open(context);

        // Thread-safe initialization
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    try {
                        ruleEngineService = new RuleEngineService();
                        ruleEngineService.initialize();
                        initialized = true;
                        log.info("TaggingScalarFunction initialized successfully");
                    } catch (Exception e) {
                        log.error("Failed to initialize TaggingScalarFunction: {}", e.getMessage(), e);
                        throw e;
                    }
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (ruleEngineService != null) {
            ruleEngineService.cleanup();
        }
        initialized = false;
        super.close();
    }

    /**
     * Evaluate method called by Flink SQL for each row
     */
    public String eval(
            String transactionId,
            String accountId,
            BigDecimal amount,
            String currency,
            String transactionType,
            String counterpartyId,
            String counterpartyName,
            String description,
            Instant transactionTime,
            String countryCode,
            String ipAddress,
            String deviceId,
            Integer riskScore) {

        String traceId = UUID.randomUUID().toString();

        try {
            // Build transaction object
            Transaction transaction = Transaction.builder()
                    .transactionId(transactionId != null ? transactionId : UUID.randomUUID().toString())
                    .accountId(accountId != null ? accountId : "UNKNOWN")
                    .amount(amount != null ? amount : BigDecimal.ZERO)
                    .currency(currency != null ? currency : "USD")
                    .transactionType(transactionType != null ? transactionType : "UNKNOWN")
                    .counterpartyId(counterpartyId)
                    .counterpartyName(counterpartyName)
                    .description(description)
                    .transactionTime(transactionTime != null ? transactionTime : Instant.now())
                    .countryCode(countryCode)
                    .ipAddress(ipAddress)
                    .deviceId(deviceId)
                    .riskScore(riskScore != null ? riskScore : 0)
                    .tags(new ArrayList<>())
                    .traceId(traceId)
                    .build();

            // Execute rules
            if (ruleEngineService != null && ruleEngineService.isReady()) {
                transaction = ruleEngineService.executeRules(transaction);
            } else {
                log.warn("[traceId={}] Rule engine not ready", traceId);
                transaction.addTag("NO_RULES");
            }

            return transaction.getTagsAsString();

        } catch (Exception e) {
            log.error("[traceId={}] Error in tagging function: {}", traceId, e.getMessage(), e);
            return "ERROR";
        }
    }

    /**
     * Simplified eval for testing
     */
    public String eval(String transactionId, BigDecimal amount, String transactionType) {
        return eval(
                transactionId,
                null,
                amount,
                "USD",
                transactionType,
                null,
                null,
                null,
                Instant.now(),
                null,
                null,
                null,
                0
        );
    }
}
