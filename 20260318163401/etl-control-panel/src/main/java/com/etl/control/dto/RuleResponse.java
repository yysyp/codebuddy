package com.etl.control.dto;

import com.etl.control.entity.RuleDefinition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Rule Definition Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Rule definition response")
public class RuleResponse {

    @Schema(description = "Rule ID")
    private Long id;

    @Schema(description = "Rule name")
    private String name;

    @Schema(description = "Rule description")
    private String description;

    @Schema(description = "Drools rule content")
    private String ruleContent;

    @Schema(description = "Rule version")
    private Integer version;

    @Schema(description = "Rule status")
    private String status;

    @Schema(description = "Rule type")
    private String ruleType;

    @Schema(description = "Target type")
    private String targetType;

    @Schema(description = "Rule priority")
    private String priority;

    @Schema(description = "Tags")
    private String tags;

    @Schema(description = "Created timestamp")
    private Instant createdAt;

    @Schema(description = "Created by")
    private String createdBy;

    @Schema(description = "Updated timestamp")
    private Instant updatedAt;

    @Schema(description = "Updated by")
    private String updatedBy;

    /**
     * Convert entity to response DTO
     */
    public static RuleResponse fromEntity(RuleDefinition entity) {
        return RuleResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .ruleContent(entity.getRuleContent())
                .version(entity.getVersion())
                .status(entity.getStatus().name())
                .ruleType(entity.getRuleType())
                .targetType(entity.getTargetType())
                .priority(entity.getPriority())
                .tags(entity.getTags())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
