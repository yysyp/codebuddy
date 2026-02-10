package com.example.transactionlabeling.service;

import com.example.transactionlabeling.dto.TransactionRequest;
import com.example.transactionlabeling.dto.TransactionResponse;
import com.example.transactionlabeling.entity.Transaction;
import com.example.transactionlabeling.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for TransactionService
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = Transaction.builder()
                .id(1L)
                .transactionId("TXN001")
                .accountNumber("ACC001")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .transactionType("PURCHASE")
                .merchantCategory("RETAIL")
                .location("New York")
                .countryCode("USA")
                .status("PENDING")
                .description("Test transaction")
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void testCreateTransaction() {
        TransactionRequest request = TransactionRequest.builder()
                .transactionId("TXN001")
                .accountNumber("ACC001")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .transactionType("PURCHASE")
                .merchantCategory("RETAIL")
                .location("New York")
                .countryCode("USA")
                .status("PENDING")
                .description("Test transaction")
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = transactionService.createTransaction(request);

        assertThat(response).isNotNull();
        assertThat(response.getTransactionId()).isEqualTo("TXN001");
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testGetTransactionById() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        TransactionResponse response = transactionService.getTransactionById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getTransactionId()).isEqualTo("TXN001");
        verify(transactionRepository, times(1)).findById(1L);
    }

    @Test
    void testGetTransactionByIdNotFound() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        try {
            transactionService.getTransactionById(1L);
            assertThat(true).isFalse(); // Should not reach here
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Transaction not found");
        }

        verify(transactionRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAllTransactions() {
        Pageable pageable = PageRequest.of(0, 20);
        List<Transaction> transactions = Arrays.asList(transaction);
        Page<Transaction> page = new PageImpl<>(transactions, pageable, 1);

        when(transactionRepository.findAll(pageable)).thenReturn(page);

        var response = transactionService.getAllTransactions(pageable);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(transactionRepository, times(1)).findAll(pageable);
    }
}
