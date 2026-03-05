package com.example.flink.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Tagged transaction output model.
 * Represents a transaction after being processed by the rules engine.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaggedTransaction implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long transactionId;
    private String accountId;
    private BigDecimal amount;
    private String transactionType;
    private String counterparty;
    private Instant transactionTime;
    private String currency;
    private String channel;
    private String location;
    private String description;
    
    /** Risk score (0-100) */
    private Integer riskScore;
    
    /** Comma-separated tags */
    private String tags;
    
    /** Processing timestamp */
    private Instant processingTime;
    
    /** Trace ID for distributed tracing */
    private String traceId;
    
    /**
     * Converts from Transaction to TaggedTransaction.
     *
     * @param transaction the source transaction
     * @param traceId the trace ID
     * @return tagged transaction
     */
    public static TaggedTransaction fromTransaction(Transaction transaction, String traceId) {
        if (transaction == null) {
            return null;
        }
        
        String tagsString = "";
        try {
            java.util.List<String> tagList = transaction.getTags();
            if (tagList != null && !tagList.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < tagList.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(tagList.get(i));
                }
                tagsString = sb.toString();
            }
        } catch (Exception e) {
            tagsString = "";
        }
        
        return TaggedTransaction.builder()
                .transactionId(transaction.getTransactionId())
                .accountId(transaction.getAccountId())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .counterparty(transaction.getCounterparty())
                .transactionTime(transaction.getTransactionTime())
                .currency(transaction.getCurrency())
                .channel(transaction.getChannel())
                .location(transaction.getLocation())
                .description(transaction.getDescription())
                .riskScore(transaction.getRiskScore() != null ? transaction.getRiskScore() : 0)
                .tags(tagsString)
                .processingTime(Instant.now())
                .traceId(traceId)
                .build();
    }
}
