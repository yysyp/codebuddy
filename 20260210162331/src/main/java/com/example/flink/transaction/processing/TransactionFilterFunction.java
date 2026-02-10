package com.example.flink.transaction.processing;

import com.example.flink.transaction.model.Transaction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Transaction Filter Function
 * Filters transactions based on tags or other criteria
 * Can be used to focus on specific transaction types
 */
public class TransactionFilterFunction extends RichFlatMapFunction<Transaction, Transaction> {

    private static final long serialVersionUID = 1L;

    private final Set<String> requiredTags;
    private final Set<String> excludedTags;
    private final Integer minRiskScore;
    private final Integer maxRiskScore;

    /**
     * Constructor for tag-based filtering
     *
     * @param requiredTags transactions must have at least one of these tags (empty means no requirement)
     * @param excludedTags transactions must NOT have any of these tags
     */
    public TransactionFilterFunction(Set<String> requiredTags, Set<String> excludedTags) {
        this.requiredTags = requiredTags != null ? requiredTags : new HashSet<>();
        this.excludedTags = excludedTags != null ? excludedTags : new HashSet<>();
        this.minRiskScore = null;
        this.maxRiskScore = null;
    }

    /**
     * Constructor for risk score filtering
     *
     * @param minRiskScore minimum risk score (inclusive)
     * @param maxRiskScore maximum risk score (inclusive)
     */
    public TransactionFilterFunction(Integer minRiskScore, Integer maxRiskScore) {
        this.requiredTags = new HashSet<>();
        this.excludedTags = new HashSet<>();
        this.minRiskScore = minRiskScore;
        this.maxRiskScore = maxRiskScore;
    }

    /**
     * Constructor for combined filtering
     */
    public TransactionFilterFunction(Set<String> requiredTags, Set<String> excludedTags,
                                     Integer minRiskScore, Integer maxRiskScore) {
        this.requiredTags = requiredTags != null ? requiredTags : new HashSet<>();
        this.excludedTags = excludedTags != null ? excludedTags : new HashSet<>();
        this.minRiskScore = minRiskScore;
        this.maxRiskScore = maxRiskScore;
    }

    @Override
    public void flatMap(Transaction transaction, Collector<Transaction> out) throws Exception {
        if (transaction == null) {
            return;
        }

        // Check required tags
        if (!requiredTags.isEmpty()) {
            boolean hasRequiredTag = false;
            for (String requiredTag : requiredTags) {
                if (transaction.hasTag(requiredTag)) {
                    hasRequiredTag = true;
                    break;
                }
            }
            if (!hasRequiredTag) {
                return;
            }
        }

        // Check excluded tags
        for (String excludedTag : excludedTags) {
            if (transaction.hasTag(excludedTag)) {
                return;
            }
        }

        // Check risk score range
        if (transaction.getRiskScore() != null) {
            if (minRiskScore != null && transaction.getRiskScore() < minRiskScore) {
                return;
            }
            if (maxRiskScore != null && transaction.getRiskScore() > maxRiskScore) {
                return;
            }
        }

        // All criteria passed, collect the transaction
        out.collect(transaction);
    }
}
