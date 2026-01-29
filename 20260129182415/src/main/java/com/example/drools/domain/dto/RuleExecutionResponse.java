package com.example.drools.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for rule execution
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleExecutionResponse {

    private String ruleName;
    private boolean matched;
    private long executionTimeMs;
    private Map<String, Object> result;
    private String message;
}
