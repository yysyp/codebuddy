package com.transaction.tagging.controlpanel.repository;

import com.transaction.tagging.controlpanel.entity.RuleEntity;
import com.transaction.tagging.controlpanel.entity.RuleEntity.RuleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RuleEntity.
 */
@Repository
public interface RuleRepository extends JpaRepository<RuleEntity, Long> {

    /**
     * Find rule by rule ID
     */
    Optional<RuleEntity> findByRuleId(String ruleId);

    /**
     * Find all rules by status
     */
    List<RuleEntity> findByStatus(RuleStatus status);

    /**
     * Find all enabled published rules
     */
    @Query("SELECT r FROM RuleEntity r WHERE r.status = 'PUBLISHED' AND r.enabled = true")
    List<RuleEntity> findAllActiveRules();

    /**
     * Find rules by rule group
     */
    List<RuleEntity> findByRuleGroup(String ruleGroup);

    /**
     * Find rules by status with pagination
     */
    Page<RuleEntity> findByStatus(RuleStatus status, Pageable pageable);

    /**
     * Find rules containing name
     */
    Page<RuleEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Check if rule ID exists
     */
    boolean existsByRuleId(String ruleId);

    /**
     * Find effective rules at a given time
     */
    @Query("SELECT r FROM RuleEntity r WHERE r.status = 'PUBLISHED' AND r.enabled = true " +
           "AND (r.effectiveFrom IS NULL OR r.effectiveFrom <= :atTime) " +
           "AND (r.effectiveTo IS NULL OR r.effectiveTo >= :atTime)")
    List<RuleEntity> findEffectiveRules(@Param("atTime") Instant atTime);

    /**
     * Find all published rules ordered by priority
     */
    @Query("SELECT r FROM RuleEntity r WHERE r.status = 'PUBLISHED' AND r.enabled = true ORDER BY r.priority DESC")
    List<RuleEntity> findAllPublishedOrderByPriority();

    /**
     * Count rules by status
     */
    long countByStatus(RuleStatus status);

    /**
     * Delete rule by rule ID
     */
    void deleteByRuleId(String ruleId);
}
