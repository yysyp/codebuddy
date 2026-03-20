package com.etl.control.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Rule Definition Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Rule definition request")
public class RuleRequest {

    @NotBlank(message = "Rule name is required")
    @Schema(description = "Rule name", example = "high-amount-transaction-rule")
    private String name;

    @NotBlank(message = "Description is required")
    @Schema(description = "Rule description", example = "Tag high amount transactions")
    private String description;

    @NotBlank(message = "Rule content is required")
    @Schema(description = "Drools rule content in DRL format")
    private String ruleContent;

    @Schema(description = "Rule type", example = "TAGGING")
    private String ruleType;

    @Schema(description = "Target type", example = "TRANSACTION")
    private String targetType;

    @Schema(description = "Rule priority", example = "HIGH")
    private String priority;

    @Schema(description = "Tags for categorization", example = "fraud,amount")
    private String tags;
}
