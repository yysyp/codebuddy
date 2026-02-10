package com.example.transactionlabeling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.*;
import lombok.*;
import lombok.*;

/**
 * Request DTO for creating/updating rules
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleRequest {

    @NotBlank(message = "Rule name is required")
    @Size(max = 100, message = "Rule name must not exceed 100 characters")
    private String ruleName;

    @NotNull(message = "Rule content is required")
    private String ruleContent;

    @NotBlank(message = "Rule category is required")
    @Size(max = 50, message = "Rule category must not exceed 50 characters")
    private String ruleCategory;

    private Integer priority;

    private Boolean active;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
