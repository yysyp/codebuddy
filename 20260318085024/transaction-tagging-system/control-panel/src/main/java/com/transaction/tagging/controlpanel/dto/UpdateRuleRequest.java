package com.transaction.tagging.controlpanel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Request DTO for updating an existing rule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for updating an existing rule")
public class UpdateRuleRequest {

    @Size(max = 128, message = "Rule name must not exceed 128 characters")
    @Schema(description = "Display name of the rule")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(description = "Detailed description of the rule")
    private String description;

    @Schema(description = "Drools rule content in DRL format")
    private String ruleContent;

    @Size(max = 256, message = "Package name must not exceed 256 characters")
    @Schema(description = "Drools package name")
    private String packageName;

    @Size(max = 64, message = "Rule group must not exceed 64 characters")
    @Schema(description = "Rule group for organization")
    private String ruleGroup;

    @Schema(description = "Priority of the rule")
    private Integer priority;

    @Schema(description = "Whether the rule is enabled")
    private Boolean enabled;

    @Size(max = 64, message = "Tag code must not exceed 64 characters")
    @Schema(description = "Tag code to apply when rule matches")
    private String tagCode;

    @Size(max = 128, message = "Tag name must not exceed 128 characters")
    @Schema(description = "Tag name to apply when rule matches")
    private String tagName;

    @Size(max = 64, message = "Tag category must not exceed 64 characters")
    @Schema(description = "Tag category")
    private String tagCategory;

    @Size(max = 32, message = "Tag severity must not exceed 32 characters")
    @Schema(description = "Tag severity level")
    private String tagSeverity;

    @Schema(description = "Effective start time")
    private Instant effectiveFrom;

    @Schema(description = "Effective end time")
    private Instant effectiveTo;

    @Size(max = 64, message = "Updated by must not exceed 64 characters")
    @Schema(description = "User who updates the rule")
    private String updatedBy;
}
