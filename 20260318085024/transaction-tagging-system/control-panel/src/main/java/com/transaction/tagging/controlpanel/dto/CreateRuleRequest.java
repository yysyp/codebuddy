package com.transaction.tagging.controlpanel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Request DTO for creating a new rule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for creating a new rule")
public class CreateRuleRequest {

    @NotBlank(message = "Rule ID is required")
    @Size(max = 64, message = "Rule ID must not exceed 64 characters")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_-]*$", message = "Rule ID must start with a letter and contain only letters, numbers, underscores, or hyphens")
    @Schema(description = "Unique rule identifier", example = "HIGH_AMOUNT_TRANSACTION_RULE")
    private String ruleId;

    @NotBlank(message = "Rule name is required")
    @Size(max = 128, message = "Rule name must not exceed 128 characters")
    @Schema(description = "Display name of the rule", example = "High Amount Transaction Alert")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(description = "Detailed description of the rule")
    private String description;

    @Schema(description = "Rule type", defaultValue = "DROOLS", example = "DROOLS")
    @Builder.Default
    private String ruleType = "DROOLS";

    @Size(max = 32, message = "Version must not exceed 32 characters")
    @Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$", message = "Version must be in semantic versioning format (e.g., 1.0.0)")
    @Schema(description = "Rule version in semantic versioning", defaultValue = "1.0.0", example = "1.0.0")
    @Builder.Default
    private String version = "1.0.0";

    @Schema(description = "Drools rule content in DRL format")
    private String ruleContent;

    @Size(max = 256, message = "Package name must not exceed 256 characters")
    @Schema(description = "Drools package name", defaultValue = "com.transaction.tagging.rules")
    @Builder.Default
    private String packageName = "com.transaction.tagging.rules";

    @Size(max = 64, message = "Rule group must not exceed 64 characters")
    @Schema(description = "Rule group for organization", example = "FRAUD_DETECTION")
    private String ruleGroup;

    @Schema(description = "Priority of the rule (higher number = higher priority)", defaultValue = "0")
    @Builder.Default
    private int priority = 0;

    @Schema(description = "Whether the rule is enabled", defaultValue = "true")
    @Builder.Default
    private boolean enabled = true;

    @Size(max = 64, message = "Tag code must not exceed 64 characters")
    @Schema(description = "Tag code to apply when rule matches", example = "HIGH_RISK")
    private String tagCode;

    @Size(max = 128, message = "Tag name must not exceed 128 characters")
    @Schema(description = "Tag name to apply when rule matches", example = "High Risk Transaction")
    private String tagName;

    @Size(max = 64, message = "Tag category must not exceed 64 characters")
    @Schema(description = "Tag category", example = "RISK")
    private String tagCategory;

    @Size(max = 32, message = "Tag severity must not exceed 32 characters")
    @Schema(description = "Tag severity level", example = "HIGH")
    private String tagSeverity;

    @Schema(description = "Effective start time")
    private Instant effectiveFrom;

    @Schema(description = "Effective end time")
    private Instant effectiveTo;

    @Size(max = 64, message = "Created by must not exceed 64 characters")
    @Schema(description = "User who creates the rule", example = "admin")
    private String createdBy;
}
