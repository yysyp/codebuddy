package com.example.drools.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entity for logging rule executions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleExecutionLog {

    private String id;
    private String ruleName;
    private String factType;
    private String inputData;
    private String outputData;
    private Instant executionTime;
    private long durationMs;
    private boolean matched;
    private String errorMessage;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;
}
