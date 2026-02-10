package com.transaction.domain.repository;

import com.transaction.domain.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Transaction entity
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Find transaction by ID
     */
    Optional<Transaction> findById(UUID id);

    /**
     * Find transactions by account ID
     */
    Page<Transaction> findByAccountId(String accountId, Pageable pageable);

    /**
     * Find transactions by account ID and date range
     */
    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId AND t.transactionTime BETWEEN :startTime AND :endTime")
    Page<Transaction> findByAccountIdAndDateRange(
            @Param("accountId") String accountId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            Pageable pageable
    );

    /**
     * Find transactions by transaction type
     */
    Page<Transaction> findByTransactionType(String transactionType, Pageable pageable);

    /**
     * Find transactions by status
     */
    Page<Transaction> findByStatus(String status, Pageable pageable);

    /**
     * Find transactions with risk score above threshold
     */
    @Query("SELECT t FROM Transaction t WHERE t.riskScore >= :threshold")
    List<Transaction> findHighRiskTransactions(@Param("threshold") BigDecimal threshold);

    /**
     * Find transactions by tags containing specific tag
     */
    @Query("SELECT t FROM Transaction t WHERE t.tags LIKE %:tag%")
    List<Transaction> findByTag(@Param("tag") String tag);

    /**
     * Count transactions by account ID
     */
    long countByAccountId(String accountId);

    /**
     * Sum transaction amounts by account ID and date range
     */
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.accountId = :accountId AND t.transactionTime BETWEEN :startTime AND :endTime")
    Optional<BigDecimal> sumAmountsByAccountIdAndDateRange(
            @Param("accountId") String accountId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime
    );

    /**
     * Find transactions by merchant name
     */
    Page<Transaction> findByMerchantName(String merchantName, Pageable pageable);
}
