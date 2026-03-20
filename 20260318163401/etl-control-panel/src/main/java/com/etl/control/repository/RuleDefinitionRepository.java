package com.etl.control.repository;

import com.etl.control.entity.RuleDefinition;
import com.etl.control.entity.RuleDefinition.RuleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Rule Definition Repository
 */
@Repository
public interface RuleDefinitionRepository extends JpaRepository<RuleDefinition, Long> {

    /**
     * Find by name
     */
    Optional<RuleDefinition> findByName(String name);

    /**
     * Find by status
     */
    Page<RuleDefinition> findByStatus(RuleStatus status, Pageable pageable);

    /**
     * Find by name and status
     */
    Optional<RuleDefinition> findByNameAndStatus(String name, RuleStatus status);

    /**
     * Find latest version by name
     */
    @Query("SELECT r FROM RuleDefinition r WHERE r.name = :name ORDER BY r.version DESC LIMIT 1")
    Optional<RuleDefinition> findLatestVersionByName(@Param("name") String name);

    /**
     * Find all published rules
     */
    @Query("SELECT r FROM RuleDefinition r WHERE r.status = 'PUBLISHED' ORDER BY r.priority DESC, r.updatedAt DESC")
    List<RuleDefinition> findAllPublishedRules();

    /**
     * Find by rule type
     */
    List<RuleDefinition> findByRuleType(String ruleType);

    /**
     * Find by target type
     */
    List<RuleDefinition> findByTargetType(String targetType);

    /**
     * Find by tags containing
     */
    @Query("SELECT r FROM RuleDefinition r WHERE r.tags LIKE %:tag%")
    List<RuleDefinition> findByTag(@Param("tag") String tag);

    /**
     * Check if name exists
     */
    boolean existsByName(String name);
}
