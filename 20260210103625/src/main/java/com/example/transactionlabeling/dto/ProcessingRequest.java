package com.example.transactionlabeling.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.*;
import lombok.*;
import lombok.*;

/**
 * Request DTO for processing operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingRequest {

    @NotNull(message = "Batch size is required")
    @Min(value = 1, message = "Batch size must be at least 1")
    private Integer batchSize;

    @Min(value = 1, message = "Max records must be at least 1")
    private Integer maxRecords;

    private Boolean useParquetOutput;
    private String outputDirectory;
}
