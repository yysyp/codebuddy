package com.cmdwrapper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for synchronous command execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandExecuteResponse {

    /**
     * Task ID for async tracking
     */
    private String taskId;

    /**
     * Exit code of the command
     */
    private int exitCode;

    /**
     * Standard output of the command
     */
    private String stdout;

    /**
     * Standard error output of the command
     */
    private String stderr;

    /**
     * Execution start time in milliseconds
     */
    private long startTime;

    /**
     * Execution end time in milliseconds
     */
    private long endTime;

    /**
     * Total execution time in milliseconds
     */
    private long executionTimeMs;

    /**
     * Whether the command was executed successfully
     */
    private boolean success;
}
