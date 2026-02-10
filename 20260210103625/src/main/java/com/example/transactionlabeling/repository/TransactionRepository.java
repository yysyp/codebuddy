package com.example.transactionlabeling.repository;

import com.example.transactionlabeling.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Transaction entity
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);

    List<Transaction> findByAccountNumber(String accountNumber);

    List<Transaction> findByStatus(String status);

    @Query("SELECT t FROM Transaction t WHERE t.processedAt IS NULL ORDER BY t.createdAt ASC")
    List<Transaction> findUnprocessedTransactions();

    @Query("SELECT t FROM Transaction t WHERE t.processedAt IS NULL ORDER BY t.createdAt ASC")
    List<Transaction> findUnprocessedTransactionsWithLimit(@Param("limit") int limit);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.processedAt IS NULL")
    Long countUnprocessedTransactions();

    List<Transaction> findByAccountNumberAndStatus(String accountNumber, String status);
}
