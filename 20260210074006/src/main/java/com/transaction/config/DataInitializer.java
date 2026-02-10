package com.transaction.config;

import com.transaction.application.dto.CreateTransactionRequest;
import com.transaction.application.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Initialize test data on application startup
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private TransactionService transactionService;

    private static final String[] ACCOUNTS = {
            "ACC001", "ACC002", "ACC003", "ACC004", "ACC005"
    };

    private static final String[] TRANSACTION_TYPES = {
            "CREDIT", "DEBIT", "TRANSFER"
    };

    private static final String[] CURRENCIES = {
            "USD", "EUR", "GBP", "JPY"
    };

    private static final String[] MERCHANT_NAMES = {
            "Amazon", "Walmart", "Best Buy", "Target", "Costco"
    };

    private static final String[] MERCHANT_CATEGORIES = {
            "5411", "5331", "5732", "5921", "5412"
    };

    private static final String[] LOCATIONS = {
            "New York, USA", "London, UK", "Tokyo, Japan", "Paris, France", "Sydney, Australia"
    };

    private static final String[] IP_ADDRESSES = {
            "192.168.1.100", "10.0.0.50", "172.16.0.10", "203.45.67.89", "185.200.123.45"
    };

    private final Random random = new Random();

    @Override
    public void run(String... args) {
        log.info("Initializing test data...");
        
        try {
            // Create sample transactions
            List<CreateTransactionRequest> requests = generateSampleTransactions(100);
            
            log.info("Creating {} sample transactions...", requests.size());
            transactionService.createTransactionsBatch(requests);
            
            log.info("Test data initialization completed successfully");
        } catch (Exception e) {
            log.error("Error initializing test data", e);
        }
    }

    /**
     * Generate sample transaction requests
     */
    private List<CreateTransactionRequest> generateSampleTransactions(int count) {
        List<CreateTransactionRequest> requests = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            requests.add(generateRandomTransaction(i));
        }
        
        return requests;
    }

    /**
     * Generate a random transaction request
     */
    private CreateTransactionRequest generateRandomTransaction(int index) {
        // Generate a random amount between $10 and $100,000
        double min = 10.0;
        double max = 100000.0;
        double amount = min + (max - min) * random.nextDouble();
        
        // Generate a random transaction time within the last 30 days
        int daysAgo = random.nextInt(30);
        Instant transactionTime = Instant.now().minus(daysAgo, ChronoUnit.DAYS)
                .minus(random.nextInt(24), ChronoUnit.HOURS)
                .minus(random.nextInt(60), ChronoUnit.MINUTES);
        
        return CreateTransactionRequest.builder()
                .accountId(ACCOUNTS[random.nextInt(ACCOUNTS.length)])
                .transactionType(TRANSACTION_TYPES[random.nextInt(TRANSACTION_TYPES.length)])
                .amount(new BigDecimal(String.format("%.2f", amount)))
                .currency(CURRENCIES[random.nextInt(CURRENCIES.length)])
                .merchantName(MERCHANT_NAMES[random.nextInt(MERCHANT_NAMES.length)])
                .merchantCategory(MERCHANT_CATEGORIES[random.nextInt(MERCHANT_CATEGORIES.length)])
                .location(LOCATIONS[random.nextInt(LOCATIONS.length)])
                .ipAddress(IP_ADDRESSES[random.nextInt(IP_ADDRESSES.length)])
                .deviceId("DEVICE" + String.format("%03d", random.nextInt(1000)))
                .referenceNumber("REF" + System.currentTimeMillis() + random.nextInt(10000))
                .status("PENDING")
                .transactionTime(transactionTime)
                .description("Test transaction #" + index)
                .build();
    }
}
