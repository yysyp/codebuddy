package com.example.flink.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Tagged transaction output model for CSV sink.
 * Contains original transaction data plus assigned tags.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaggedTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    // Original transaction fields
    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private String transactionType;
    private String counterpartyId;
    private String counterpartyName;
    private String description;
    private Instant transactionTime;
    private String countryCode;
    private String ipAddress;
    private String deviceId;
    private Integer riskScore;

    // Tagging result fields
    private String tags;
    private String primaryTag;
    private Integer tagCount;
    private Instant processingTime;
    private String traceId;

    /**
     * Create TaggedTransaction from Transaction
     *
     * @param transaction the source transaction
     * @return new TaggedTransaction
     */
    public static TaggedTransaction fromTransaction(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        String tagsString = transaction.getTagsAsString();
        String primaryTag = (transaction.getTags() != null && !transaction.getTags().isEmpty())
                ? transaction.getTags().get(0)
                : "UNTAGGED";

        return TaggedTransaction.builder()
                .transactionId(transaction.getTransactionId())
                .accountId(transaction.getAccountId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .transactionType(transaction.getTransactionType())
                .counterpartyId(transaction.getCounterpartyId())
                .counterpartyName(transaction.getCounterpartyName())
                .description(transaction.getDescription())
                .transactionTime(transaction.getTransactionTime())
                .countryCode(transaction.getCountryCode())
                .ipAddress(transaction.getIpAddress())
                .deviceId(transaction.getDeviceId())
                .riskScore(transaction.getRiskScore())
                .tags(tagsString)
                .primaryTag(primaryTag)
                .tagCount(transaction.getTags() != null ? transaction.getTags().size() : 0)
                .processingTime(Instant.now())
                .traceId(transaction.getTraceId())
                .build();
    }

    /**
     * Convert to CSV row array
     *
     * @return array of values for CSV
     */
    public Object[] toCsvRow() {
        return new Object[]{
                transactionId != null ? transactionId : "",
                accountId != null ? accountId : "",
                amount != null ? amount.toPlainString() : "0",
                currency != null ? currency : "",
                transactionType != null ? transactionType : "",
                counterpartyId != null ? counterpartyId : "",
                counterpartyName != null ? counterpartyName : "",
                description != null ? description : "",
                transactionTime != null ? transactionTime.toString() : "",
                countryCode != null ? countryCode : "",
                ipAddress != null ? ipAddress : "",
                deviceId != null ? deviceId : "",
                riskScore != null ? riskScore : 0,
                tags != null ? tags : "",
                primaryTag != null ? primaryTag : "",
                tagCount != null ? tagCount : 0,
                processingTime != null ? processingTime.toString() : "",
                traceId != null ? traceId : ""
        };
    }

    /**
     * Get CSV header row
     *
     * @return array of header names
     */
    public static String[] getCsvHeader() {
        return new String[]{
                "transaction_id",
                "account_id",
                "amount",
                "currency",
                "transaction_type",
                "counterparty_id",
                "counterparty_name",
                "description",
                "transaction_time",
                "country_code",
                "ip_address",
                "device_id",
                "risk_score",
                "tags",
                "primary_tag",
                "tag_count",
                "processing_time",
                "trace_id"
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaggedTransaction that = (TaggedTransaction) o;
        return Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        return "TaggedTransaction{" +
                "transactionId='" + transactionId + '\'' +
                ", amount=" + amount +
                ", transactionType='" + transactionType + '\'' +
                ", tags='" + tags + '\'' +
                ", primaryTag='" + primaryTag + '\'' +
                ", traceId='" + traceId + '\'' +
                '}';
    }
}
