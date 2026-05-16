package com.cmdwrapper.service;

import com.cmdwrapper.config.CommandWrapperProperties;
import com.cmdwrapper.dto.CommandExecuteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Service for executing system commands.
 * Supports both Windows and Linux platforms.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommandExecutionService {

    private final PasswordService passwordService;
    private final CommandWrapperProperties properties;

    // Store async task futures
    private final ConcurrentHashMap<String, Future<CommandExecuteResponse>> taskFutures = new ConcurrentHashMap<>();

    /**
     * Execute a command synchronously.
     *
     * @param command the original command with placeholders
     * @param workingDirectory the working directory
     * @param timeoutSeconds execution timeout in seconds
     * @return the command execution response
     */
    public CommandExecuteResponse executeCommand(String command, String workingDirectory, int timeoutSeconds) {
        String taskId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        log.info("Executing command [taskId={}]: {}", taskId, maskSensitiveCommand(command));

        // Replace placeholders with actual passwords
        String processedCommand = passwordService.replacePlaceholders(command);
        log.debug("Processed command: {}", maskSensitiveCommand(processedCommand));

        try {
            // Determine the shell to use based on OS
            ProcessBuilder processBuilder = createProcessBuilder(processedCommand, workingDirectory);
            
            Process process = processBuilder.start();

            // Read output streams
            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();
            Charset charset = getCharset();

            Thread stdoutReader = startStreamReader(process.getInputStream(), stdout);
            Thread stderrReader = startStreamReader(process.getErrorStream(), stderr);

            // Wait for process to complete with timeout
            boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            // Wait for stream readers to finish
            stdoutReader.join(1000);
            stderrReader.join(1000);

            long endTime = System.currentTimeMillis();

            if (!completed) {
                process.destroyForcibly();
                log.warn("Command execution timed out [taskId={}]", taskId);
                return CommandExecuteResponse.builder()
                    .taskId(taskId)
                    .exitCode(-1)
                    .stdout(stdout.toString())
                    .stderr("Command execution timed out after " + timeoutSeconds + " seconds")
                    .startTime(startTime)
                    .endTime(endTime)
                    .executionTimeMs(endTime - startTime)
                    .success(false)
                    .build();
            }

            int exitCode = process.exitValue();
            boolean success = exitCode == 0;

            log.info("Command completed [taskId={}, exitCode={}, duration={}ms]", 
                taskId, exitCode, endTime - startTime);

            return CommandExecuteResponse.builder()
                .taskId(taskId)
                .exitCode(exitCode)
                .stdout(stdout.toString())
                .stderr(stderr.toString())
                .startTime(startTime)
                .endTime(endTime)
                .executionTimeMs(endTime - startTime)
                .success(success)
                .build();

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("Command execution failed [taskId={}]: {}", taskId, e.getMessage(), e);

            return CommandExecuteResponse.builder()
                .taskId(taskId)
                .exitCode(-1)
                .stdout("")
                .stderr("Execution error: " + e.getMessage())
                .startTime(startTime)
                .endTime(endTime)
                .executionTimeMs(endTime - startTime)
                .success(false)
                .build();
        }
    }

    /**
     * Execute a command asynchronously.
     *
     * @param command the original command with placeholders
     * @param workingDirectory the working directory
     * @param timeoutSeconds execution timeout in seconds
     * @return the task ID for tracking
     */
    public String executeCommandAsync(String command, String workingDirectory, int timeoutSeconds) {
        String taskId = UUID.randomUUID().toString();
        
        log.info("Starting async command execution [taskId={}]", taskId);

        Future<CommandExecuteResponse> future = java.util.concurrent.CompletableFuture.supplyAsync(() ->
            executeCommand(command, workingDirectory, timeoutSeconds)
        );

        taskFutures.put(taskId, future);
        
        return taskId;
    }

    /**
     * Get the status of an async task.
     *
     * @param taskId the task ID
     * @return the task status
     */
    public String getTaskStatus(String taskId) {
        Future<CommandExecuteResponse> future = taskFutures.get(taskId);
        if (future == null) {
            return "NOT_FOUND";
        }
        if (future.isDone()) {
            return "COMPLETED";
        }
        if (future.isCancelled()) {
            return "CANCELLED";
        }
        return "RUNNING";
    }

    /**
     * Get the result of an async task.
     *
     * @param taskId the task ID
     * @return the command execution response, or null if not found
     */
    public CommandExecuteResponse getTaskResult(String taskId) {
        Future<CommandExecuteResponse> future = taskFutures.get(taskId);
        if (future == null) {
            return null;
        }
        
        try {
            if (future.isDone()) {
                return future.get();
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting task result [taskId={}]: {}", taskId, e.getMessage());
            return CommandExecuteResponse.builder()
                .taskId(taskId)
                .exitCode(-1)
                .stderr("Task retrieval error: " + e.getMessage())
                .success(false)
                .build();
        }
    }

    /**
     * Cancel an async task.
     *
     * @param taskId the task ID
     * @return true if cancelled successfully
     */
    public boolean cancelTask(String taskId) {
        Future<CommandExecuteResponse> future = taskFutures.get(taskId);
        if (future != null && !future.isDone()) {
            return future.cancel(true);
        }
        return false;
    }

    /**
     * Create a ProcessBuilder based on the operating system.
     */
    private ProcessBuilder createProcessBuilder(String command, String workingDirectory) {
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder builder;

        if (os.contains("win")) {
            // Windows
            builder = new ProcessBuilder("cmd.exe", "/c", command);
        } else {
            // Linux/Unix/Mac
            builder = new ProcessBuilder("bash", "-c", command);
        }

        // Set working directory
        String workDir = workingDirectory != null && !workingDirectory.isEmpty() 
            ? workingDirectory 
            : properties.getWorkingDirectory();
        builder.directory(new File(workDir));

        // Merge error stream with output
        builder.redirectErrorStream(false);

        // Set environment
        builder.environment().put("CMD_WRAPPER_TASK_ID", UUID.randomUUID().toString());

        return builder;
    }

    /**
     * Get the appropriate charset for the system.
     */
    private Charset getCharset() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return Charset.forName("GBK"); // Windows default Chinese encoding
        }
        return StandardCharsets.UTF_8;
    }

    /**
     * Start a thread to read from an input stream.
     */
    private Thread startStreamReader(InputStream inputStream, StringBuilder output) {
        Thread thread = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, getCharset()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            } catch (Exception e) {
                log.warn("Error reading stream: {}", e.getMessage());
            }
        });
        thread.start();
        return thread;
    }

    /**
     * Mask sensitive information in command for logging.
     */
    private String maskSensitiveCommand(String command) {
        if (command == null) return null;
        // Replace potential password patterns
        return command.replaceAll("(-p\\s*|--password\\s*|=)\\S+", "$1******");
    }
}
