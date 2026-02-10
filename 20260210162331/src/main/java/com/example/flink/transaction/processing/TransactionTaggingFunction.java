package com.example.flink.transaction.processing;

import com.example.flink.transaction.config.FlinkConfig;
import com.example.flink.transaction.model.Transaction;
import com.example.flink.transaction.rules.RuleEngine;
import com.example.flink.transaction.util.TraceIdUtil;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transaction Tagging Function using Drools Rule Engine
 * Applies dynamic rules to tag transactions in Flink streaming pipeline
 * Thread-safe implementation with rule reloading support
 */
public class TransactionTaggingFunction extends RichMapFunction<Transaction, Transaction> {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionTaggingFunction.class);

    private static final long serialVersionUID = 1L;

    private final FlinkConfig config;
    private transient RuleEngine ruleEngine;

    public TransactionTaggingFunction(FlinkConfig config) {
        this.config = config;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        LOG.info("Initializing TransactionTaggingFunction");

        try {
            // Initialize rule engine
            ruleEngine = new RuleEngine(config.getRuleFilePath());
            LOG.info("Rule engine initialized with rules from: {}", config.getRuleFilePath());
        } catch (Exception e) {
            LOG.error("Failed to initialize rule engine", e);
            throw e;
        }
    }

    @Override
    public Transaction map(Transaction transaction) throws Exception {
        if (transaction == null) {
            LOG.warn("Received null transaction");
            return null;
        }

        // Set trace ID for observability
        TraceIdUtil.setTraceId(transaction.getTraceId());

        try {
            LOG.debug("Processing transaction: {}", transaction.getTransactionId());

            // Apply rules with automatic reload check
            long reloadIntervalMs = config.getRuleReloadIntervalMs();
            Transaction taggedTransaction = ruleEngine.applyRulesWithReloadCheck(transaction, reloadIntervalMs);

            LOG.info("Transaction {} tagged with: {}",
                    taggedTransaction.getTransactionId(),
                    taggedTransaction.getTagsAsString());

            return taggedTransaction;

        } catch (Exception e) {
            LOG.error("Error processing transaction: " + transaction.getTransactionId(), e);
            // Return original transaction on error (fault tolerance)
            return transaction;
        } finally {
            TraceIdUtil.clearTraceId();
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        if (ruleEngine != null) {
            ruleEngine.close();
        }
        LOG.info("TransactionTaggingFunction closed");
    }
}
