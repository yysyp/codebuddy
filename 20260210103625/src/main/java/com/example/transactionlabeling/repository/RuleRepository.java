package com.example.transactionlabeling.repository;

import com.example.transactionlabeling.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Rule entity
 */
@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {

    Optional<Rule> findByRuleName(String ruleName);

    List<Rule> findByRuleCategory(String ruleCategory);

    List<Rule> findByActive(Boolean active);

    @Query("SELECT r FROM Rule r WHERE r.active = true ORDER BY r.priority DESC, r.id ASC")
    List<Rule> findActiveRulesOrderedByPriority();
}
