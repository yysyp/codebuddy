package com.etl.control.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Schema Definition Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Schema definition request")
public class SchemaRequest {

    @NotBlank(message = "Schema name is required")
    @Schema(description = "Schema name", example = "transaction-schema")
    private String name;

    @NotBlank(message = "Description is required")
    @Schema(description = "Schema description", example = "Transaction data schema")
    private String description;

    @NotBlank(message = "Schema content is required")
    @Schema(description = "Schema content in JSON format")
    private String schemaContent;

    @Schema(description = "Schema type", example = "CSV")
    private String schemaType;

    @Schema(description = "Additional metadata")
    private String metadata;
}
