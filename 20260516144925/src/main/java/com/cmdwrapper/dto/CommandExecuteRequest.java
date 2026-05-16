package com.cmdwrapper.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for command execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandExecuteRequest {

    /**
     * The original command with placeholders to be replaced
     */
    @NotBlank(message = "Command cannot be blank")
    private String command;

    /**
     * Working directory for command execution (optional)
     */
    private String workingDirectory;

    /**
     * Execution timeout in seconds (optional, uses default if not specified)
     */
    private Integer timeoutSeconds;
}
