package com.example.transaction.service;

import com.example.transaction.model.Transaction;
import java.math.BigDecimal;
import java.util.List;

public class RuleProcessor {
    
    public void applyRules(Transaction transaction) {
        // Rule 1: High Value Transaction
        if (transaction.getAmount().compareTo(new BigDecimal("10000")) >= 0) {
            transaction.getLabels().add("HIGH_VALUE");
            transaction.setPriority("HIGH");
        }
        // Rule 2: Medium Value Transaction
        else if (transaction.getAmount().compareTo(new BigDecimal("1000")) >= 0) {
            transaction.getLabels().add("MEDIUM_VALUE");
            transaction.setPriority("MEDIUM");
        }
        // Rule 3: Low Value Transaction
        else {
            transaction.getLabels().add("LOW_VALUE");
            transaction.setPriority("LOW");
        }
        
        // Rule 4: Category-based labels
        if (transaction.getMerchantCategory().equals("Luxury") || 
            transaction.getMerchantName().contains("Jewelry")) {
            transaction.getLabels().add("LUXURY");
        }
        
        if (transaction.getMerchantCategory().equals("Electronics")) {
            transaction.getLabels().add("ELECTRONICS");
        }
        
        if (transaction.getMerchantCategory().equals("Food") || 
            transaction.getMerchantCategory().equals("Grocery")) {
            transaction.getLabels().add("FOOD");
        }
        
        // Rule 5: Online Transaction
        if (transaction.getLocation().equals("Unknown")) {
            transaction.getLabels().add("ONLINE");
        }
        
        // Rule 6: Suspicious Transaction
        if (transaction.getAmount().compareTo(new BigDecimal("5000")) >= 0 && 
            transaction.getLocation().equals("Unknown")) {
            transaction.getLabels().add("SUSPICIOUS");
            transaction.setFraudRisk("HIGH");
        }
        
        // Rule 7: Travel Category
        if (transaction.getMerchantCategory().equals("Travel")) {
            transaction.getLabels().add("TRAVEL");
        }
        
        // Rule 8: Banking Transaction
        if (transaction.getMerchantCategory().equals("Banking")) {
            transaction.getLabels().add("BANKING");
        }
        
        // Rule 9: Fraud Risk Assessment
        if (transaction.getFraudRisk() == null) {
            if (transaction.getAmount().compareTo(new BigDecimal("20000")) >= 0) {
                transaction.getLabels().add("VERY_HIGH_RISK");
                transaction.setFraudRisk("VERY_HIGH");
            } else if (transaction.getAmount().compareTo(new BigDecimal("100")) < 0) {
                transaction.getLabels().add("NORMAL_RISK");
                transaction.setFraudRisk("LOW");
            }
        }
    }
    
    public void applyRulesToAll(List<Transaction> transactions) {
        for (Transaction tx : transactions) {
            applyRules(tx);
        }
    }
}
