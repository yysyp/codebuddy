package com.transaction.tagging.controlpanel.dto;

import com.transaction.tagging.controlpanel.entity.RuleEntity.RuleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for rule information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Rule response with full details")
public class RuleResponse {

    @Schema(description = "Unique rule identifier")
    private String ruleId;

    @Schema(description = "Display name of the rule")
    private String name;

    @Schema(description = "Detailed description")
    private String description;

    @Schema(description = "Rule type")
    private String ruleType;

    @Schema(description = "Rule version")
    private String version;

    @Schema(description = "Rule status")
    private RuleStatus status;

    @Schema(description = "Drools rule content in DRL format")
    private String ruleContent;

    @Schema(description = "Drools package name")
    private String packageName;

    @Schema(description = "Rule group")
    private String ruleGroup;

    @Schema(description = "Priority")
    private int priority;

    @Schema(description = "Enabled flag")
    private boolean enabled;

    @Schema(description = "Tag code")
    private String tagCode;

    @Schema(description = "Tag name")
    private String tagName;

    @Schema(description = "Tag category")
    private String tagCategory;

    @Schema(description = "Tag severity")
    private String tagSeverity;

    @Schema(description = "Effective start time")
    private Instant effectiveFrom;

    @Schema(description = "Effective end time")
    private Instant effectiveTo;

    @Schema(description = "Creation timestamp")
    private Instant createdAt;

    @Schema(description = "User who created the rule")
    private String createdBy;

    @Schema(description = "Last update timestamp")
    private Instant updatedAt;

    @Schema(description = "User who last updated the rule")
    private String updatedBy;

    @Schema(description = "Publication timestamp")
    private Instant publishedAt;

    @Schema(description = "User who published the rule")
    private String publishedBy;
}
