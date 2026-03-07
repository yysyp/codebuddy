package com.example.flink.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * Configuration properties for Flink job.
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "flink.job")
public class FlinkJobConfig {

    /**
     * Job name
     */
    private String jobName = "transaction-tagging-job";

    /**
     * Parallelism for the job
     */
    private int parallelism = 2;

    /**
     * Input CSV file path
     */
    private String inputPath = "data/transactions.csv";

    /**
     * Output CSV file path
     */
    private String outputPath = "output/tagged_transactions.csv";

    /**
     * Checkpointing interval
     */
    private Duration checkpointInterval = Duration.ofSeconds(30);

    /**
     * Enable checkpointing
     */
    private boolean checkpointingEnabled = true;

    /**
     * Min pause between checkpoints
     */
    private Duration minPauseBetweenCheckpoints = Duration.ofSeconds(5);

    /**
     * Checkpoint timeout
     */
    private Duration checkpointTimeout = Duration.ofMinutes(10);

    /**
     * Maximum concurrent checkpoints
     */
    private int maxConcurrentCheckpoints = 1;

    /**
     * Enable unaligned checkpoints (for better performance under backpressure)
     */
    private boolean unalignedCheckpoints = false;

    /**
     * Restart strategy: fixed-delay, exponential-delay, no-restart
     */
    private String restartStrategy = "fixed-delay";

    /**
     * Number of restart attempts
     */
    private int restartAttempts = 3;

    /**
     * Delay between restart attempts
     */
    private Duration restartDelay = Duration.ofSeconds(10);

    /**
     * Batch size for processing
     */
    private int batchSize = 1000;

    /**
     * Enable metrics
     */
    private boolean metricsEnabled = true;

    /**
     * Metrics report interval
     */
    private Duration metricsInterval = Duration.ofSeconds(60);

    /**
     * Rule source: drl (default), table, or decision-table
     */
    private String ruleSource = "drl";

    /**
     * Table rules CSV file path
     */
    private String tableRulesPath = "rules/table-rules.csv";

    /**
     * Decision table CSV file path
     */
    private String decisionTablePath = "rules/decision-table.csv";

    @PostConstruct
    public void validate() {
        log.info("FlinkJobConfig loaded: {}", this);

        // Validate configuration
        if (parallelism < 1) {
            throw new IllegalArgumentException("Parallelism must be at least 1");
        }

        if (checkpointInterval.toMillis() < 1000) {
            log.warn("Checkpoint interval is very short: {}", checkpointInterval);
        }

        // Create directories if they don't exist
        try {
            Path outputDir = Paths.get(outputPath).getParent();
            if (outputDir != null) {
                java.nio.file.Files.createDirectories(outputDir);
            }
        } catch (Exception e) {
            log.warn("Could not create output directory: {}", e.getMessage());
        }
    }

    /**
     * Get input path as absolute path
     */
    public String getAbsoluteInputPath() {
        return Paths.get(inputPath).toAbsolutePath().toString();
    }

    /**
     * Get output path as absolute path
     */
    public String getAbsoluteOutputPath() {
        return Paths.get(outputPath).toAbsolutePath().toString();
    }
}
