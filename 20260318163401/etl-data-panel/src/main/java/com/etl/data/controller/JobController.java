package com.etl.data.controller;

import com.etl.data.service.TransactionTaggingJob;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Job Controller
 * REST API endpoints for managing Flink jobs
 */
@Slf4j
@Tag(name = "Job Management", description = "APIs for managing data processing jobs")
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final TransactionTaggingJob taggingJob;

    @Operation(summary = "Execute transaction tagging job", description = "Executes the Flink job to tag transactions")
    @PostMapping("/tagging/execute")
    public ResponseEntity<Map<String, Object>> executeTaggingJob(
            @Parameter(description = "Input file path") @RequestParam(required = false) String inputPath,
            @Parameter(description = "Output file path") @RequestParam(required = false) String outputPath) {
        
        log.info("Executing transaction tagging job");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (inputPath != null && outputPath != null) {
                taggingJob.execute(inputPath, outputPath);
            } else {
                taggingJob.executeWithDefaults();
            }
            
            response.put("success", true);
            response.put("message", "Job executed successfully");
            response.put("timestamp", java.time.Instant.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error executing tagging job", e);
            
            response.put("success", false);
            response.put("message", "Job execution failed: " + e.getMessage());
            response.put("timestamp", java.time.Instant.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @Operation(summary = "Health check", description = "Checks if the data panel service is healthy")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "etl-data-panel");
        health.put("timestamp", java.time.Instant.now());
        return ResponseEntity.ok(health);
    }
}
