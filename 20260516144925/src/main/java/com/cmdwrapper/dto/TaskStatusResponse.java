package com.cmdwrapper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for task status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusResponse {

    /**
     * Task ID
     */
    private String taskId;

    /**
     * Current status: PENDING, RUNNING, COMPLETED, FAILED
     */
    private String status;

    /**
     * Original command
     */
    private String command;

    /**
     * Execution start time
     */
    private long startTime;

    /**
     * Current progress description
     */
    private String message;
}
