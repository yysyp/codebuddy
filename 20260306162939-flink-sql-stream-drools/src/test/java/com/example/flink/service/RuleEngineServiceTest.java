package com.example.flink.service;

import com.example.flink.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RuleEngineService.
 */
class RuleEngineServiceTest {

    private RuleEngineService ruleEngineService;

    @BeforeEach
    void setUp() {
        ruleEngineService = new RuleEngineService();
        ruleEngineService.initialize();
    }

    @Test
    void testInitialize() {
        assertTrue(ruleEngineService.isReady());
    }

    @Test
    void testExecuteRulesHighAmount() {
        Transaction transaction = Transaction.builder()
                .transactionId("TXN-001")
                .accountId("ACC001")
                .amount(new BigDecimal("15000.00"))
                .transactionType("DEBIT")
                .transactionTime(Instant.now())
                .tags(new java.util.ArrayList<>())
                .build();

        Transaction result = ruleEngineService.executeRules(transaction);

        assertNotNull(result);
        assertTrue(result.hasTag("HIGH_AMOUNT"));
    }

    @Test
    void testExecuteRulesHighRisk() {
        Transaction transaction = Transaction.builder()
                .transactionId("TXN-002")
                .accountId("ACC001")
                .amount(new BigDecimal("100.00"))
                .riskScore(75)
                .transactionType("DEBIT")
                .transactionTime(Instant.now())
                .tags(new java.util.ArrayList<>())
                .build();

        Transaction result = ruleEngineService.executeRules(transaction);

        assertNotNull(result);
        assertTrue(result.hasTag("HIGH_RISK"));
    }

    @Test
    void testExecuteRulesTransfer() {
        Transaction transaction = Transaction.builder()
                .transactionId("TXN-003")
                .accountId("ACC001")
                .amount(new BigDecimal("1000.00"))
                .transactionType("TRANSFER")
                .transactionTime(Instant.now())
                .tags(new java.util.ArrayList<>())
                .build();

        Transaction result = ruleEngineService.executeRules(transaction);

        assertNotNull(result);
        assertTrue(result.hasTag("TRANSFER"));
    }

    @Test
    void testExecuteRulesInternational() {
        Transaction transaction = Transaction.builder()
                .transactionId("TXN-004")
                .accountId("ACC001")
                .amount(new BigDecimal("100.00"))
                .countryCode("CN")
                .transactionType("DEBIT")
                .transactionTime(Instant.now())
                .tags(new java.util.ArrayList<>())
                .build();

        Transaction result = ruleEngineService.executeRules(transaction);

        assertNotNull(result);
        assertTrue(result.hasTag("INTERNATIONAL"));
    }

    @Test
    void testExecuteRulesNoCountry() {
        Transaction transaction = Transaction.builder()
                .transactionId("TXN-005")
                .accountId("ACC001")
                .amount(new BigDecimal("100.00"))
                .countryCode("")
                .transactionType("DEBIT")
                .transactionTime(Instant.now())
                .tags(new java.util.ArrayList<>())
                .build();

        Transaction result = ruleEngineService.executeRules(transaction);

        assertNotNull(result);
        assertTrue(result.hasTag("NO_COUNTRY"));
    }

    @Test
    void testExecuteRulesCriticalRisk() {
        Transaction transaction = Transaction.builder()
                .transactionId("TXN-006")
                .accountId("ACC001")
                .amount(new BigDecimal("5000.00"))
                .riskScore(95)
                .transactionType("TRANSFER")
                .transactionTime(Instant.now())
                .tags(new java.util.ArrayList<>())
                .build();

        Transaction result = ruleEngineService.executeRules(transaction);

        assertNotNull(result);
        assertTrue(result.hasTag("CRITICAL_RISK"));
        assertTrue(result.hasTag("HIGH_RISK"));
        assertTrue(result.hasTag("TRANSFER"));
    }

    @Test
    void testExecuteRulesNullTransaction() {
        assertThrows(NullPointerException.class, () -> {
            ruleEngineService.executeRules(null);
        });
    }

    @Test
    void testGetStatus() {
        String status = ruleEngineService.getStatus();
        assertNotNull(status);
        assertTrue(status.contains("RuleEngineService"));
    }

    @Test
    void testReloadRules() {
        assertDoesNotThrow(() -> ruleEngineService.reloadRules());
        assertTrue(ruleEngineService.isReady());
    }

    @Test
    void testCleanup() {
        ruleEngineService.cleanup();
        assertFalse(ruleEngineService.isReady());
    }
}
