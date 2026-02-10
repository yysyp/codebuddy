package com.example.transactionlabeling.dto;

import lombok.*;
import lombok.*;
import lombok.*;
import lombok.*;

import java.time.Instant;

/**
 * Response DTO for rule data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleResponse {

    private Long id;
    private String ruleName;
    private String ruleContent;
    private String ruleCategory;
    private Integer priority;
    private Boolean active;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
