package com.example.transactionlabeling.dto;

import lombok.*;
import lombok.*;
import lombok.*;
import lombok.*;

import java.time.Instant;

/**
 * Response DTO for processing operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingResponse {

    private Long id;
    private String operationName;
    private String status;
    private Long recordsProcessed;
    private Long executionTimeMs;
    private String errorMessage;
    private String outputFilePath;
    private Instant startTime;
    private Instant endTime;
}
