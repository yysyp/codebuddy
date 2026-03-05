package com.example.flink.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Transaction data model representing a financial transaction.
 * Immutable class designed for thread-safe operations in Flink.
 */
public final class Transaction implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);
    
    private final Long id;
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
    
    // Default constructor for serialization
    public Transaction() {
        this.id = null;
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
    }
    
    private Transaction(Builder builder) {
        this.id = builder.id != null ? builder.id : ID_GENERATOR.incrementAndGet();
        this.transactionId = Objects.requireNonNull(builder.transactionId, "transactionId cannot be null");
        this.accountId = Objects.requireNonNull(builder.accountId, "accountId cannot be null");
        this.counterpartyAccount = builder.counterpartyAccount;
        this.amount = Objects.requireNonNull(builder.amount, "amount cannot be null");
        this.currency = Objects.requireNonNull(builder.currency, "currency cannot be null");
        this.transactionType = Objects.requireNonNull(builder.transactionType, "transactionType cannot be null");
        this.channel = builder.channel;
        this.countryCode = builder.countryCode;
        this.transactionTime = builder.transactionTime != null ? builder.transactionTime : Instant.now();
        this.description = builder.description;
        this.riskLevel = builder.riskLevel;
        this.tags = builder.tags;
    }
    
    public Long getId() {
        return id;
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
    
    public Transaction withTags(String newTags) {
        return new Builder(this).tags(newTags).build();
    }
    
    public Transaction withRiskLevel(String newRiskLevel) {
        return new Builder(this).riskLevel(newRiskLevel).build();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", transactionId='" + transactionId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", channel='" + channel + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", transactionTime=" + transactionTime +
                ", riskLevel='" + riskLevel + '\'' +
                ", tags='" + tags + '\'' +
                '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long id;
        private String transactionId;
        private String accountId;
        private String counterpartyAccount;
        private BigDecimal amount;
        private String currency;
        private String transactionType;
        private String channel;
        private String countryCode;
        private Instant transactionTime;
        private String description;
        private String riskLevel;
        private String tags;
        
        public Builder() {}
        
        public Builder(Transaction transaction) {
            this.id = transaction.id;
            this.transactionId = transaction.transactionId;
            this.accountId = transaction.accountId;
            this.counterpartyAccount = transaction.counterpartyAccount;
            this.amount = transaction.amount;
            this.currency = transaction.currency;
            this.transactionType = transaction.transactionType;
            this.channel = transaction.channel;
            this.countryCode = transaction.countryCode;
            this.transactionTime = transaction.transactionTime;
            this.description = transaction.description;
            this.riskLevel = transaction.riskLevel;
            this.tags = transaction.tags;
        }
        
        public Builder id(Long id) {
            this.id = id;
            return this;
        }
        
        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }
        
        public Builder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }
        
        public Builder counterpartyAccount(String counterpartyAccount) {
            this.counterpartyAccount = counterpartyAccount;
            return this;
        }
        
        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }
        
        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }
        
        public Builder transactionType(String transactionType) {
            this.transactionType = transactionType;
            return this;
        }
        
        public Builder channel(String channel) {
            this.channel = channel;
            return this;
        }
        
        public Builder countryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }
        
        public Builder transactionTime(Instant transactionTime) {
            this.transactionTime = transactionTime;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder riskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
            return this;
        }
        
        public Builder tags(String tags) {
            this.tags = tags;
            return this;
        }
        
        public Transaction build() {
            return new Transaction(this);
        }
    }
}
