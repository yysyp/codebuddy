package com.cmdwrapper.controller;

import com.cmdwrapper.config.CommandWrapperProperties;
import com.cmdwrapper.dto.CommandExecuteRequest;
import com.cmdwrapper.dto.CommandExecuteResponse;
import com.cmdwrapper.dto.TaskStatusResponse;
import com.cmdwrapper.service.CommandExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for command execution API.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/commands")
@RequiredArgsConstructor
@Tag(name = "Command Execution", description = "APIs for executing commands with password placeholder replacement")
public class CommandController {

    private final CommandExecutionService commandExecutionService;
    private final CommandWrapperProperties properties;

    @PostMapping("/execute")
    @Operation(summary = "Execute command synchronously", 
               description = "Execute a command with password placeholder replacement and wait for result")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Command executed successfully",
                     content = @Content(schema = @Schema(implementation = CommandExecuteResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> executeCommand(
            @Valid @RequestBody CommandExecuteRequest request) {
        
        log.info("Received sync command execution request");

        int timeout = request.getTimeoutSeconds() != null 
            ? request.getTimeoutSeconds() 
            : properties.getDefaultTimeoutSeconds();

        CommandExecuteResponse response = commandExecutionService.executeCommand(
            request.getCommand(),
            request.getWorkingDirectory(),
            timeout
        );

        return buildResponse(response);
    }

    @PostMapping("/execute-async")
    @Operation(summary = "Execute command asynchronously",
               description = "Execute a command with password placeholder replacement and return immediately with task ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Task created and running",
                     content = @Content(schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> executeCommandAsync(
            @Valid @RequestBody CommandExecuteRequest request) {
        
        log.info("Received async command execution request");

        int timeout = request.getTimeoutSeconds() != null 
            ? request.getTimeoutSeconds() 
            : properties.getDefaultTimeoutSeconds();

        String taskId = commandExecutionService.executeCommandAsync(
            request.getCommand(),
            request.getWorkingDirectory(),
            timeout
        );

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        result.put("status", "PENDING");
        result.put("message", "Command execution started. Use GET /api/v1/commands/" + taskId + "/status to check status");

        return ResponseEntity.accepted().body(result);
    }

    @GetMapping("/{taskId}/status")
    @Operation(summary = "Get task status",
               description = "Get the current status of an async command execution task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status retrieved successfully",
                     content = @Content(schema = @Schema(implementation = TaskStatusResponse.class))),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<Map<String, Object>> getTaskStatus(
            @Parameter(description = "Task ID") @PathVariable String taskId) {
        
        String status = commandExecutionService.getTaskStatus(taskId);
        
        if ("NOT_FOUND".equals(status)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Task not found");
            error.put("taskId", taskId);
            return ResponseEntity.notFound().build();
        }

        CommandExecuteResponse result = commandExecutionService.getTaskResult(taskId);

        Map<String, Object> response = new HashMap<>();
        response.put("taskId", taskId);
        response.put("status", status);
        if (result != null) {
            response.put("startTime", result.getStartTime());
            response.put("executionTimeMs", result.getExecutionTimeMs());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{taskId}/result")
    @Operation(summary = "Get task result",
               description = "Get the execution result of an async command execution task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Result retrieved successfully",
                     content = @Content(schema = @Schema(implementation = CommandExecuteResponse.class))),
        @ApiResponse(responseCode = "202", description = "Task still running"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<Map<String, Object>> getTaskResult(
            @Parameter(description = "Task ID") @PathVariable String taskId) {
        
        String status = commandExecutionService.getTaskStatus(taskId);
        
        if ("NOT_FOUND".equals(status)) {
            return ResponseEntity.notFound().build();
        }

        if ("RUNNING".equals(status)) {
            Map<String, Object> running = new HashMap<>();
            running.put("taskId", taskId);
            running.put("status", "RUNNING");
            running.put("message", "Task is still running");
            return ResponseEntity.status(202).body(running);
        }

        CommandExecuteResponse result = commandExecutionService.getTaskResult(taskId);
        return buildResponse(result);
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Cancel task",
               description = "Cancel an async command execution task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cancel request processed"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<Map<String, Object>> cancelTask(
            @Parameter(description = "Task ID") @PathVariable String taskId) {
        
        boolean cancelled = commandExecutionService.cancelTask(taskId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("taskId", taskId);
        response.put("cancelled", cancelled);
        
        if (cancelled) {
            response.put("message", "Task cancellation requested");
        } else {
            response.put("message", "Task could not be cancelled (may be already completed or not found)");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check",
               description = "Check if the service is running")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "command-wrapper-service");
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }

    /**
     * Build unified response format.
     */
    private ResponseEntity<Map<String, Object>> buildResponse(CommandExecuteResponse response) {
        Map<String, Object> result = new HashMap<>();
        result.put("taskId", response.getTaskId());
        result.put("exitCode", response.getExitCode());
        result.put("stdout", response.getStdout());
        result.put("stderr", response.getStderr());
        result.put("success", response.isSuccess());
        result.put("startTime", response.getStartTime());
        result.put("endTime", response.getEndTime());
        result.put("executionTimeMs", response.getExecutionTimeMs());

        if (response.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(500).body(result);
        }
    }
}
