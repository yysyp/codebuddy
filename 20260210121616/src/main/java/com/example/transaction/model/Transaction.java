package com.example.transaction.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Transaction {
    private String transactionId;
    private String accountNumber;
    private BigDecimal amount;
    private String currency;
    private String merchantName;
    private String merchantCategory;
    private Instant transactionTime;
    private String location;
    private String deviceId;
    private List<String> labels;
    private String fraudRisk;
    private String priority;
    
    public Transaction() {
        this.labels = new ArrayList<>();
    }
    
    public Transaction(String transactionId, String accountNumber, BigDecimal amount, String currency,
                    String merchantName, String merchantCategory, Instant transactionTime,
                    String location, String deviceId, List<String> labels,
                    String fraudRisk, String priority) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.currency = currency;
        this.merchantName = merchantName;
        this.merchantCategory = merchantCategory;
        this.transactionTime = transactionTime;
        this.location = location;
        this.deviceId = deviceId;
        this.labels = labels != null ? labels : new ArrayList<>();
        this.fraudRisk = fraudRisk;
        this.priority = priority;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    
    public String getMerchantCategory() { return merchantCategory; }
    public void setMerchantCategory(String merchantCategory) { this.merchantCategory = merchantCategory; }
    
    public Instant getTransactionTime() { return transactionTime; }
    public void setTransactionTime(Instant transactionTime) { this.transactionTime = transactionTime; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    
    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels != null ? labels : new ArrayList<>(); }
    
    public String getFraudRisk() { return fraudRisk; }
    public void setFraudRisk(String fraudRisk) { this.fraudRisk = fraudRisk; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
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
                "transactionId='" + transactionId + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", merchantName='" + merchantName + '\'' +
                ", labels=" + labels +
                ", fraudRisk='" + fraudRisk + '\'' +
                '}';
    }
    
    public static class Builder {
        private String transactionId;
        private String accountNumber;
        private BigDecimal amount;
        private String currency;
        private String merchantName;
        private String merchantCategory;
        private Instant transactionTime;
        private String location;
        private String deviceId;
        private List<String> labels;
        private String fraudRisk;
        private String priority;
        
        public Builder() {
            this.labels = new ArrayList<>();
        }
        
        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }
        
        public Builder accountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
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
        
        public Builder merchantName(String merchantName) {
            this.merchantName = merchantName;
            return this;
        }
        
        public Builder merchantCategory(String merchantCategory) {
            this.merchantCategory = merchantCategory;
            return this;
        }
        
        public Builder transactionTime(Instant transactionTime) {
            this.transactionTime = transactionTime;
            return this;
        }
        
        public Builder location(String location) {
            this.location = location;
            return this;
        }
        
        public Builder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }
        
        public Builder labels(List<String> labels) {
            this.labels = labels;
            return this;
        }
        
        public Builder fraudRisk(String fraudRisk) {
            this.fraudRisk = fraudRisk;
            return this;
        }
        
        public Builder priority(String priority) {
            this.priority = priority;
            return this;
        }
        
        public Transaction build() {
            return new Transaction(
                transactionId, accountNumber, amount, currency,
                merchantName, merchantCategory, transactionTime,
                location, deviceId, labels,
                fraudRisk, priority
            );
        }
    }
}
