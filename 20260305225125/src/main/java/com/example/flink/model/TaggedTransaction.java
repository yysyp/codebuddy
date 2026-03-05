package com.example.flink.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Output model representing a transaction with applied rule-based tags.
 * This is the result of applying Drools rules to Transaction data.
 */
public final class TaggedTransaction implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final String transactionId;
    private final String accountId;
    private final String counterpartyAccount;
    private final BigDecimal amount;
    private final String currency;
    private final String transactionType;
    private final String channel;
    private final String countryCode;
    private final Instant transactionTime;
    private final String description;
    private final String riskLevel;
    private final String tags;
    private final String appliedRules;
    private final Instant processingTime;
    
    // Default constructor for serialization
    public TaggedTransaction() {
        this.transactionId = null;
        this.accountId = null;
        this.counterpartyAccount = null;
        this.amount = null;
        this.currency = null;
        this.transactionType = null;
        this.channel = null;
        this.countryCode = null;
        this.transactionTime = null;
        this.description = null;
        this.riskLevel = null;
        this.tags = null;
        this.appliedRules = null;
        this.processingTime = null;
    }
    
    public TaggedTransaction(Transaction transaction, String appliedRules) {
        this.transactionId = transaction.getTransactionId();
        this.accountId = transaction.getAccountId();
        this.counterpartyAccount = transaction.getCounterpartyAccount();
        this.amount = transaction.getAmount();
        this.currency = transaction.getCurrency();
        this.transactionType = transaction.getTransactionType();
        this.channel = transaction.getChannel();
        this.countryCode = transaction.getCountryCode();
        this.transactionTime = transaction.getTransactionTime();
        this.description = transaction.getDescription();
        this.riskLevel = transaction.getRiskLevel();
        this.tags = transaction.getTags();
        this.appliedRules = appliedRules;
        this.processingTime = Instant.now();
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public String getAccountId() {
        return accountId;
    }
    
    public String getCounterpartyAccount() {
        return counterpartyAccount;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public String getChannel() {
        return channel;
    }
    
    public String getCountryCode() {
        return countryCode;
    }
    
    public Instant getTransactionTime() {
        return transactionTime;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getRiskLevel() {
        return riskLevel;
    }
    
    public String getTags() {
        return tags;
    }
    
    public String getAppliedRules() {
        return appliedRules;
    }
    
    public Instant getProcessingTime() {
        return processingTime;
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
                ", accountId='" + accountId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", riskLevel='" + riskLevel + '\'' +
                ", tags='" + tags + '\'' +
                ", appliedRules='" + appliedRules + '\'' +
                ", processingTime=" + processingTime +
                '}';
    }
    
    /**
     * Returns a CSV header row for the output file.
     */
    public static String getCsvHeader() {
        return "transaction_id,account_id,counterparty_account,amount,currency," +
               "transaction_type,channel,country_code,transaction_time,description," +
               "risk_level,tags,applied_rules,processing_time";
    }
    
    /**
     * Converts this transaction to a CSV row.
     */
    public String toCsvRow() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                escapeCsv(transactionId),
                escapeCsv(accountId),
                escapeCsv(counterpartyAccount),
                amount != null ? amount.toPlainString() : "",
                escapeCsv(currency),
                escapeCsv(transactionType),
                escapeCsv(channel),
                escapeCsv(countryCode),
                transactionTime != null ? transactionTime.toString() : "",
                escapeCsv(description),
                escapeCsv(riskLevel),
                escapeCsv(tags),
                escapeCsv(appliedRules),
                processingTime != null ? processingTime.toString() : ""
        );
    }
    
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
