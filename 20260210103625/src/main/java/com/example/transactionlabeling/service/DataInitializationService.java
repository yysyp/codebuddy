package com.example.transactionlabeling.service;

import com.example.transactionlabeling.entity.Rule;
import com.example.transactionlabeling.entity.Transaction;
import com.example.transactionlabeling.repository.RuleRepository;
import com.example.transactionlabeling.repository.TransactionRepository;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Random;

/**
 * Service for initializing sample data
 */

@Component
@RequiredArgsConstructor
public class DataInitializationService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializationService.class);

    private final TransactionRepository transactionRepository;
    private final RuleRepository ruleRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");

        // Initialize sample rules
        initializeSampleRules();

        // Initialize sample transactions
        initializeSampleTransactions();

        log.info("Data initialization completed.");
    }

    private void initializeSampleRules() {
        if (ruleRepository.count() > 0) {
            log.info("Rules already exist, skipping initialization");
            return;
        }

        log.info("Initializing sample rules...");

        Rule rule1 = Rule.builder()
                .ruleName("High Amount Transaction")
                .ruleCategory("RISK")
                .priority(100)
                .active(true)
                .description("Flag transactions with amount greater than 10000")
                .ruleContent("""
                        package com.example.transactionlabeling.rules

                        import com.example.transactionlabeling.entity.Transaction
                        import java.math.BigDecimal

                        rule "High Amount Transaction"
                            when
                                $transaction : Transaction(amount > 10000)
                            then
                                $transaction.getLabels().add("HIGH_AMOUNT");
                                $transaction.setRiskScore(new BigDecimal("0.8"));
                        end
                        """)
                .build();

        Rule rule2 = Rule.builder()
                .ruleName("International Transaction")
                .ruleCategory("GEOGRAPHIC")
                .priority(90)
                .active(true)
                .description("Flag international transactions (non-US country code)")
                .ruleContent("""
                        package com.example.transactionlabeling.rules

                        import com.example.transactionlabeling.entity.Transaction

                        rule "International Transaction"
                            when
                                $transaction : Transaction(countryCode != null && countryCode != "USA")
                            then
                                $transaction.getLabels().add("INTERNATIONAL");
                        end
                        """)
                .build();

        Rule rule3 = Rule.builder()
                .ruleName("Merchant Category Risk")
                .ruleCategory("RISK")
                .priority(80)
                .active(true)
                .description("Flag transactions from high-risk merchant categories")
                .ruleContent("""
                        package com.example.transactionlabeling.rules

                        import com.example.transactionlabeling.entity.Transaction

                        rule "Merchant Category Risk"
                            when
                                $transaction : Transaction(merchantCategory in ("GAMBLING", "CASH_ADVANCE", "CRYPTO"))
                            then
                                $transaction.getLabels().add("HIGH_RISK_MERCHANT");
                                if ($transaction.getRiskScore() == null) {
                                    $transaction.setRiskScore(new BigDecimal("0.7"));
                                } else {
                                    BigDecimal current = $transaction.getRiskScore();
                                    $transaction.setRiskScore(current.add(new BigDecimal("0.1")));
                                }
                        end
                        """)
                .build();

        Rule rule4 = Rule.builder()
                .ruleName("Suspicious Pattern")
                .ruleCategory("FRAUD")
                .priority(95)
                .active(true)
                .description("Flag transactions with suspicious patterns")
                .ruleContent("""
                        package com.example.transactionlabeling.rules

                        import com.example.transactionlabeling.entity.Transaction

                        rule "Suspicious Pattern"
                            when
                                $transaction : Transaction(
                                    (amount >= 9999.99 && amount <= 10000.01) ||
                                    (amount >= 99999.99 && amount <= 100000.01)
                                )
                            then
                                $transaction.getLabels().add("SUSPICIOUS_PATTERN");
                                if ($transaction.getRiskScore() == null) {
                                    $transaction.setRiskScore(new BigDecimal("0.6"));
                                } else {
                                    BigDecimal current = $transaction.getRiskScore();
                                    $transaction.setRiskScore(current.add(new BigDecimal("0.15")));
                                }
                        end
                        """)
                .build();

        Rule rule5 = Rule.builder()
                .ruleName("Online Purchase")
                .ruleCategory("NORMAL")
                .priority(50)
                .active(true)
                .description("Flag online purchases")
                .ruleContent("""
                        package com.example.transactionlabeling.rules

                        import com.example.transactionlabeling.entity.Transaction

                        rule "Online Purchase"
                            when
                                $transaction : Transaction(transactionType == "ONLINE_PURCHASE")
                            then
                                $transaction.getLabels().add("ONLINE");
                        end
                        """)
                .build();

        ruleRepository.saveAll(List.of(rule1, rule2, rule3, rule4, rule5));
        log.info("Initialized {} sample rules", 5);
    }

    private void initializeSampleTransactions() {
        if (transactionRepository.count() > 0) {
            log.info("Transactions already exist, skipping initialization");
            return;
        }

        log.info("Initializing sample transactions...");

        Random random = new Random();
        String[] accountNumbers = {"ACC001", "ACC002", "ACC003", "ACC004", "ACC005"};
        String[] transactionTypes = {"PURCHASE", "WITHDRAWAL", "DEPOSIT", "TRANSFER", "ONLINE_PURCHASE"};
        String[] merchantCategories = {"RETAIL", "GROCERY", "ENTERTAINMENT", "GAMBLING", "CASH_ADVANCE", "CRYPTO", "TRAVEL"};
        String[] locations = {"New York", "London", "Tokyo", "Paris", "Sydney", "Singapore"};
        String[] countryCodes = {"USA", "GBR", "JPN", "FRA", "AUS", "SGP"};
        String[] statuses = {"PENDING", "COMPLETED", "FAILED"};

        for (int i = 1; i <= 100; i++) {
            String transactionId = "TXN" + String.format("%06d", i);
            String accountNumber = accountNumbers[random.nextInt(accountNumbers.length)];
            BigDecimal amount = new BigDecimal(random.nextDouble() * 20000 + 100)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            String currency = "USD";
            String transactionType = transactionTypes[random.nextInt(transactionTypes.length)];
            String merchantCategory = merchantCategories[random.nextInt(merchantCategories.length)];
            String location = locations[random.nextInt(locations.length)];
            String countryCode = countryCodes[random.nextInt(countryCodes.length)];
            String status = statuses[random.nextInt(statuses.length)];

            // Create some specific test cases
            if (i % 20 == 0) {
                amount = new BigDecimal("15000.00"); // High amount
            } else if (i % 15 == 0) {
                countryCode = "USA"; // Domestic
                amount = new BigDecimal("500.00");
            } else if (i % 10 == 0) {
                merchantCategory = "GAMBLING"; // High risk merchant
            } else if (i % 7 == 0) {
                amount = new BigDecimal("10000.00"); // Suspicious pattern
            }

            Transaction transaction = Transaction.builder()
                    .transactionId(transactionId)
                    .accountNumber(accountNumber)
                    .amount(amount)
                    .currency(currency)
                    .transactionType(transactionType)
                    .merchantCategory(merchantCategory)
                    .location(location)
                    .countryCode(countryCode)
                    .riskScore(null)
                    .status(status)
                    .description("Sample transaction " + i)
                    .labels(new HashSet<>())
                    .processedAt(null)
                    .build();

            transactionRepository.save(transaction);
        }

        log.info("Initialized {} sample transactions", 100);
    }
}
