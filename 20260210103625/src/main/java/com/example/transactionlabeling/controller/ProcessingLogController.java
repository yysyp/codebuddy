package com.example.transactionlabeling.controller;

import com.example.transactionlabeling.dto.ApiResponse;
import com.example.transactionlabeling.entity.ProcessingLog;
import com.example.transactionlabeling.repository.ProcessingLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing processing logs
 */

@RestController
@RequestMapping("/api/processing")
@RequiredArgsConstructor
@Tag(name = "Processing Log Management", description = "APIs for managing processing logs")
public class ProcessingLogController {

    private static final Logger log = LoggerFactory.getLogger(ProcessingLogController.class);

    private final ProcessingLogRepository processingLogRepository;

    /**
     * Get all processing logs
     */
    @GetMapping("/logs")
    @Operation(summary = "Get all processing logs", description = "Retrieve all processing operation logs")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logs retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<ProcessingLog>>> getAllProcessingLogs() {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Fetching all processing logs, traceId: {}", traceId);
        List<ProcessingLog> logs = processingLogRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(logs).withTraceId(traceId));
    }

    /**
     * Get processing log by ID
     */
    @GetMapping("/logs/{id}")
    @Operation(summary = "Get processing log by ID", description = "Retrieve a processing log by its database ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Log found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Log not found")
    })
    public ResponseEntity<ApiResponse<ProcessingLog>> getProcessingLogById(
            @Parameter(description = "Log ID") @PathVariable Long id) {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Fetching processing log by ID: {}, traceId: {}", id, traceId);
        ProcessingLog processingLog = processingLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processing log not found with id: " + id));
        return ResponseEntity.ok(ApiResponse.success(processingLog).withTraceId(traceId));
    }

    /**
     * Get processing logs by operation name
     */
    @GetMapping("/logs/operation/{operationName}")
    @Operation(summary = "Get logs by operation", description = "Retrieve all logs for a specific operation")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logs retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<ProcessingLog>>> getLogsByOperationName(
            @Parameter(description = "Operation name") @PathVariable String operationName) {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Fetching processing logs for operation: {}, traceId: {}", operationName, traceId);
        List<ProcessingLog> logs = processingLogRepository.findByOperationName(operationName);
        return ResponseEntity.ok(ApiResponse.success(logs).withTraceId(traceId));
    }

    /**
     * Get processing logs by status
     */
    @GetMapping("/logs/status/{status}")
    @Operation(summary = "Get logs by status", description = "Retrieve all logs with a specific status")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logs retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<ProcessingLog>>> getLogsByStatus(
            @Parameter(description = "Status") @PathVariable String status) {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Fetching processing logs with status: {}, traceId: {}", status, traceId);
        List<ProcessingLog> logs = processingLogRepository.findByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(logs).withTraceId(traceId));
    }

    /**
     * Get latest processing log
     */
    @GetMapping("/logs/latest")
    @Operation(summary = "Get latest log", description = "Retrieve the most recent processing log")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Log found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<ProcessingLog>> getLatestProcessingLog() {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Fetching latest processing log, traceId: {}", traceId);
        ProcessingLog processingLog = processingLogRepository.findTopByOperationNameOrderByStartTimeDesc("PROCESS_TRANSACTIONS")
                .orElse(null);
        return ResponseEntity.ok(ApiResponse.success(processingLog).withTraceId(traceId));
    }
}
