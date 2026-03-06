package com.example.flink;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration test for the TransactionTaggingApplication.
 */
@SpringBootTest(args = {"generate", "target/test-data/test-transactions.csv"})
class TransactionTaggingApplicationTest {

    @Test
    void contextLoads() {
        // Verifies that the Spring context loads successfully
    }
}
