package com.example.flink.function;

import com.example.flink.model.TaggedTransaction;
import com.example.flink.model.Transaction;
import com.example.flink.service.RuleEngineService;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.annotation.FunctionHint;
import org.apache.flink.table.functions.ScalarFunction;

import java.util.Objects;
import java.util.UUID;

/**
 * Flink function for tagging transactions using Drools rule engine.
 * Implements RichMapFunction for stateful processing with initialization.
 */
@Slf4j
public class TransactionTaggingFunction extends RichMapFunction<Transaction, TaggedTransaction> {

    private static final long serialVersionUID = 1L;

    private transient RuleEngineService ruleEngineService;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        // Initialize rule engine service
        ruleEngineService = new RuleEngineService();
        ruleEngineService.initialize();

        log.info("TransactionTaggingFunction initialized. Rule engine ready: {}",
                ruleEngineService.isReady());
    }

    @Override
    public void close() throws Exception {
        if (ruleEngineService != null) {
            ruleEngineService.cleanup();
        }
        super.close();
    }

    @Override
    public TaggedTransaction map(Transaction transaction) throws Exception {
        // Validate input - prevent null pointer exceptions
        if (transaction == null) {
            log.warn("Received null transaction, skipping");
            return null;
        }

        // Generate traceId if not present
        String traceId = transaction.getTraceId();
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
            transaction.setTraceId(traceId);
        }

        int subtaskIndex = getRuntimeContext().getIndexOfThisSubtask();
        String subtaskTraceId = traceId + "-subtask-" + subtaskIndex;

        try {
            log.debug("[traceId={}] Processing transaction: {}", subtaskTraceId, transaction.getTransactionId());

            // Validate transaction data
            if (!transaction.isValid()) {
                log.warn("[traceId={}] Invalid transaction data: {}", subtaskTraceId, transaction);
                return createErrorTaggedTransaction(transaction, "INVALID_DATA");
            }

            // Initialize default values
            transaction.initialize();

            // Execute rules
            Transaction taggedTransaction;
            if (ruleEngineService != null && ruleEngineService.isReady()) {
                taggedTransaction = ruleEngineService.executeRules(transaction);
            } else {
                log.warn("[traceId={}] Rule engine not ready, processing without rules", subtaskTraceId);
                taggedTransaction = transaction;
                taggedTransaction.addTag("NO_RULES");
            }

            // Convert to output format
            TaggedTransaction result = TaggedTransaction.fromTransaction(taggedTransaction);

            log.debug("[traceId={}] Transaction tagged successfully: {}",
                    subtaskTraceId, result.getTags());

            return result;

        } catch (Exception e) {
            log.error("[traceId={}] Error tagging transaction: {}", subtaskTraceId, e.getMessage(), e);
            // Return transaction with error tag - graceful error handling
            return createErrorTaggedTransaction(transaction, "PROCESSING_ERROR");
        }
    }

    /**
     * Create a tagged transaction for error cases
     */
    private TaggedTransaction createErrorTaggedTransaction(Transaction transaction, String errorTag) {
        if (transaction == null) {
            return null;
        }

        transaction.addTag(errorTag);
        TaggedTransaction result = TaggedTransaction.fromTransaction(transaction);
        result.setPrimaryTag(errorTag);
        return result;
    }

    /**
     * Get the type information for the output
     */
    public static TypeInformation<TaggedTransaction> getResultTypeInfo() {
        return TypeInformation.of(TaggedTransaction.class);
    }
}
