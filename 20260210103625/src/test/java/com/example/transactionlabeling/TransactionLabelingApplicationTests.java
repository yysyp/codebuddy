package com.example.transactionlabeling;

import com.example.transactionlabeling.dto.TransactionRequest;
import com.example.transactionlabeling.entity.Transaction;
import com.example.transactionlabeling.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Main test class for the application
 */
@SpringBootTest
@ActiveProfiles("test")
class TransactionLabelingApplicationTests {

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void contextLoads() {
        assertThat(transactionRepository).isNotNull();
    }

    @Test
    void testCreateAndRetrieveTransaction() {
        TransactionRequest request = TransactionRequest.builder()
                .transactionId("TEST_TXN_001")
                .accountNumber("ACC_TEST")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .transactionType("PURCHASE")
                .merchantCategory("RETAIL")
                .location("New York")
                .countryCode("USA")
                .status("PENDING")
                .description("Test transaction")
                .build();

        Transaction transaction = Transaction.builder()
                .transactionId(request.getTransactionId())
                .accountNumber(request.getAccountNumber())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .transactionType(request.getTransactionType())
                .merchantCategory(request.getMerchantCategory())
                .location(request.getLocation())
                .countryCode(request.getCountryCode())
                .status(request.getStatus())
                .description(request.getDescription())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTransactionId()).isEqualTo("TEST_TXN_001");

        Transaction retrieved = transactionRepository.findByTransactionId("TEST_TXN_001").orElse(null);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getTransactionId()).isEqualTo("TEST_TXN_001");

        // Cleanup
        transactionRepository.delete(retrieved);
    }
}
