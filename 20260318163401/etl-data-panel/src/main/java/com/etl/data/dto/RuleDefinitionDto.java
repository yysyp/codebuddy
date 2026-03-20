package com.etl.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Rule Definition DTO
 * Data transfer object for rule definitions from Control Panel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleDefinitionDto {

    private Long id;
    private String name;
    private String description;
    private String ruleContent;
    private Integer version;
    private String status;
    private String ruleType;
    private String targetType;
    private String priority;
    private String tags;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;
}
