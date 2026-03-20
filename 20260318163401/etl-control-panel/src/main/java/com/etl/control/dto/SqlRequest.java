package com.etl.control.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SQL Definition Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SQL definition request")
public class SqlRequest {

    @NotBlank(message = "SQL name is required")
    @Schema(description = "SQL name", example = "transaction-source-table")
    private String name;

    @NotBlank(message = "Description is required")
    @Schema(description = "SQL description")
    private String description;

    @NotBlank(message = "SQL content is required")
    @Schema(description = "Flink SQL content")
    private String sqlContent;

    @NotNull(message = "SQL type is required")
    @Schema(description = "SQL type", example = "SOURCE_TABLE")
    private String sqlType;

    @Schema(description = "Associated schema name")
    private String associatedSchema;

    @Schema(description = "Parameters in JSON format")
    private String parameters;
}
