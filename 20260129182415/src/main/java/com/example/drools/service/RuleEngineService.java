package com.example.drools.service;

import com.example.drools.common.exception.RuleEngineException;
import com.example.drools.domain.dto.RuleExecutionRequest;
import com.example.drools.domain.dto.RuleExecutionResponse;
import com.example.drools.domain.fact.OrderFact;
import com.example.drools.domain.fact.PricingFact;
import com.example.drools.domain.fact.RiskAssessmentFact;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core service for executing Drools rules
 */
@Slf4j
@Service
public class RuleEngineService {

    private final KieBase kieBase;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, KieSession> sessionCache = new ConcurrentHashMap<>();

    public RuleEngineService(KieBase kieBase, ObjectMapper objectMapper) {
        this.kieBase = kieBase;
        this.objectMapper = objectMapper;
    }

    /**
     * Execute rules based on rule name and fact data
     */
    public RuleExecutionResponse executeRule(RuleExecutionRequest request) {
        long startTime = System.currentTimeMillis();
        String sessionId = "session-" + Thread.currentThread().getId();

        try {
            log.info("Executing rule: {} with data: {}", request.getRuleName(), request.getFactData());

            KieSession kieSession = kieBase.newKieSession();

            Map<String, Object> result = switch (request.getRuleName()) {
                case "pricing" -> executePricingRules(request.getFactData(), kieSession);
                case "order-processing" -> executeOrderRules(request.getFactData(), kieSession);
                case "risk-assessment" -> executeRiskAssessmentRules(request.getFactData(), kieSession);
                default -> throw new RuleEngineException("Unknown rule name: " + request.getRuleName());
            };

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Rule execution completed in {}ms", executionTime);

            return RuleExecutionResponse.builder()
                    .ruleName(request.getRuleName())
                    .matched(true)
                    .executionTimeMs(executionTime)
                    .result(result)
                    .message("Rule executed successfully")
                    .build();

        } catch (Exception e) {
            log.error("Error executing rule: {}", request.getRuleName(), e);
            throw new RuleEngineException("Failed to execute rule: " + e.getMessage(), e);
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            log.debug("Total rule execution time: {}ms", executionTime);
        }
    }

    /**
     * Execute pricing rules
     */
    private Map<String, Object> executePricingRules(Map<String, Object> factData, KieSession kieSession) {
        try {
            PricingFact fact = objectMapper.convertValue(factData, PricingFact.class);
            fact.setDiscountedPrice(fact.getOriginalPrice());

            kieSession.insert(fact);
            int rulesFired = kieSession.fireAllRules();
            kieSession.dispose();

            log.info("Pricing rules fired: {}", rulesFired);

            Map<String, Object> result = new HashMap<>();
            result.put("originalPrice", fact.getOriginalPrice());
            result.put("discountedPrice", fact.getDiscountedPrice());
            result.put("discountRate", fact.getDiscountRate());
            result.put("discountReason", fact.getDiscountReason());
            result.put("customerType", fact.getCustomerType());
            result.put("rulesFired", rulesFired);

            return result;

        } catch (Exception e) {
            throw new RuleEngineException("Failed to execute pricing rules: " + e.getMessage(), e);
        }
    }

    /**
     * Execute order processing rules
     */
    private Map<String, Object> executeOrderRules(Map<String, Object> factData, KieSession kieSession) {
        try {
            OrderFact fact = objectMapper.convertValue(factData, OrderFact.class);
            fact.setStatus("PENDING");

            kieSession.insert(fact);
            int rulesFired = kieSession.fireAllRules();
            kieSession.dispose();

            log.info("Order processing rules fired: {}", rulesFired);

            Map<String, Object> result = new HashMap<>();
            result.put("orderId", fact.getOrderId());
            result.put("status", fact.getStatus());
            result.put("requiresApproval", fact.isRequiresApproval());
            result.put("approvalReason", fact.getApprovalReason());
            result.put("highValue", fact.isHighValue());
            result.put("suspicious", fact.isSuspicious());
            result.put("warnings", fact.getWarnings());
            result.put("expressShipping", fact.isExpressShipping());
            result.put("rulesFired", rulesFired);

            return result;

        } catch (Exception e) {
            throw new RuleEngineException("Failed to execute order rules: " + e.getMessage(), e);
        }
    }

    /**
     * Execute risk assessment rules
     */
    private Map<String, Object> executeRiskAssessmentRules(Map<String, Object> factData, KieSession kieSession) {
        try {
            RiskAssessmentFact fact = objectMapper.convertValue(factData, RiskAssessmentFact.class);
            fact.setRiskScore(0);
            fact.setRiskLevel("LOW");

            kieSession.insert(fact);
            int rulesFired = kieSession.fireAllRules();
            kieSession.dispose();

            log.info("Risk assessment rules fired: {}", rulesFired);

            Map<String, Object> result = new HashMap<>();
            result.put("requestId", fact.getRequestId());
            result.put("riskLevel", fact.getRiskLevel());
            result.put("riskScore", fact.getRiskScore());
            result.put("requiresVerification", fact.isRequiresVerification());
            result.put("flagged", fact.isFlagged());
            result.put("flagReason", fact.getFlagReason());
            result.put("recommendedAction", fact.getRecommendedAction());
            result.put("rulesFired", rulesFired);

            return result;

        } catch (Exception e) {
            throw new RuleEngineException("Failed to execute risk assessment rules: " + e.getMessage(), e);
        }
    }

    /**
     * Get available rule sets
     */
    public Map<String, String> getAvailableRules() {
        Map<String, String> rules = new HashMap<>();
        rules.put("pricing", "Calculate discounts based on customer type and purchase history");
        rules.put("order-processing", "Validate and process orders with business rules");
        rules.put("risk-assessment", "Assess transaction risk and determine appropriate actions");
        return rules;
    }
}
