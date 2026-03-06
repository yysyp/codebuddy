package com.example.flink;

import com.example.flink.config.FlinkJobConfig;
import com.example.flink.job.TransactionTaggingJob;
import com.example.flink.util.CsvUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Flink Transaction Tagging Application
 * 
 * This application processes transaction data using Apache Flink and Drools rule engine.
 * It supports multiple execution modes:
 * - SQL Mode: Uses Flink SQL for declarative processing
 * - DataStream Mode: Uses Flink DataStream API with Drools integration
 * - Hybrid Mode: Combines SQL and DataStream APIs
 * 
 * Usage:
 *   java -jar flink-transaction-tagging.jar [mode] [input-file] [output-file]
 * 
 * Modes:
 *   sql        - Use Flink SQL mode (default)
 *   stream     - Use DataStream API mode
 *   hybrid     - Use hybrid mode
 *   generate   - Generate sample data file
 */
@Slf4j
@SpringBootApplication
public class TransactionTaggingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionTaggingApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(FlinkJobConfig config) {
        return args -> {
            String traceId = java.util.UUID.randomUUID().toString();
            log.info("[traceId={}] Starting Flink Transaction Tagging Application", traceId);

            try {
                // Parse command line arguments
                ExecutionMode mode = parseExecutionMode(args);
                String inputPath = args.length > 1 ? args[1] : config.getInputPath();
                String outputPath = args.length > 2 ? args[2] : config.getOutputPath();

                // Update config with command line arguments
                config.setInputPath(inputPath);
                config.setOutputPath(outputPath);

                log.info("[traceId={}] Execution mode: {}, Input: {}, Output: {}", 
                        traceId, mode, inputPath, outputPath);

                // Generate sample data if requested
                if (mode == ExecutionMode.GENERATE) {
                    generateSampleData(inputPath, traceId);
                    return;
                }

                // Validate input file exists
                java.nio.file.Path inputFile = java.nio.file.Paths.get(config.getAbsoluteInputPath());
                if (!java.nio.file.Files.exists(inputFile)) {
                    log.warn("[traceId={}] Input file not found: {}. Generating sample data...", 
                            traceId, config.getAbsoluteInputPath());
                    generateSampleData(config.getAbsoluteInputPath(), traceId);
                }

                // Create and run the job
                TransactionTaggingJob job = new TransactionTaggingJob(config);
                
                switch (mode) {
                    case SQL:
                        log.info("[traceId={}] Executing Flink SQL Job", traceId);
                        job.executeSqlJob();
                        break;
                    case STREAM:
                        log.info("[traceId={}] Executing Flink DataStream Job", traceId);
                        job.executeDataStreamJob();
                        break;
                    case HYBRID:
                        log.info("[traceId={}] Executing Hybrid Flink Job", traceId);
                        job.executeHybridJob();
                        break;
                    default:
                        log.info("[traceId={}] Executing default Flink SQL Job", traceId);
                        job.executeSqlJob();
                }

                log.info("[traceId={}] Job completed successfully. Metrics: {}", traceId, job.getMetrics());
                log.info("[traceId={}] Output written to: {}", traceId, config.getAbsoluteOutputPath());

            } catch (Exception e) {
                log.error("[traceId={}] Application failed: {}", traceId, e.getMessage(), e);
                System.exit(1);
            }
        };
    }

    /**
     * Parse execution mode from command line arguments
     */
    private ExecutionMode parseExecutionMode(String[] args) {
        if (args.length == 0) {
            return ExecutionMode.SQL;
        }

        String modeArg = args[0].toLowerCase();
        switch (modeArg) {
            case "sql":
                return ExecutionMode.SQL;
            case "stream":
            case "datastream":
                return ExecutionMode.STREAM;
            case "hybrid":
                return ExecutionMode.HYBRID;
            case "generate":
            case "gen":
                return ExecutionMode.GENERATE;
            default:
                log.warn("Unknown mode '{}', using default SQL mode", modeArg);
                return ExecutionMode.SQL;
        }
    }

    /**
     * Generate sample transaction data
     */
    private void generateSampleData(String filePath, String traceId) throws Exception {
        log.info("[traceId={}] Generating sample transaction data to: {}", traceId, filePath);
        CsvUtils.writeSampleCsv(filePath, 100);
        log.info("[traceId={}] Sample data generated successfully", traceId);
    }

    /**
     * Execution modes for the application
     */
    private enum ExecutionMode {
        SQL,
        STREAM,
        HYBRID,
        GENERATE
    }
}
