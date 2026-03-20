package com.etl.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * SQL Definition DTO
 * Data transfer object for SQL definitions from Control Panel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SqlDefinitionDto {

    private Long id;
    private String name;
    private String description;
    private String sqlContent;
    private String sqlType;
    private String status;
    private String associatedSchema;
    private String parameters;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;
}
