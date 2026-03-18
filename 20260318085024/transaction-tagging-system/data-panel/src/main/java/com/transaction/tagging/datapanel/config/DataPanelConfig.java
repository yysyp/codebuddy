package com.transaction.tagging.datapanel.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.util.Preconditions;

import java.io.Serializable;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Configuration for Data Panel application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataPanelConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    // Job identification
    @Builder.Default
    private String jobId = "job-" + UUID.randomUUID().toString().substring(0, 8);

    // Control Panel configuration
    @Builder.Default
    private String controlPanelUrl = "http://localhost:8080/api";
    @Builder.Default
    private int ruleRefreshIntervalSeconds = 60;
    @Builder.Default
    private int ruleFetchTimeoutSeconds = 30;

    // Source configuration
    @Builder.Default
    private String sourceType = "generator"; // generator, kafka, file
    @Builder.Default
    private int generatorRatePerSecond = 100;
    @Builder.Default
    private int generatorTotalRecords = 10000;

    // Kafka configuration (if source type is kafka)
    @Builder.Default
    private String kafkaBootstrapServers = "localhost:9092";
    @Builder.Default
    private String kafkaTopic = "transactions";
    @Builder.Default
    private String kafkaGroupId = "transaction-tagging-group";

    // Sink configuration
    @Builder.Default
    private String sinkType = "console"; // console, kafka, file
    @Builder.Default
    private String sinkKafkaTopic = "tagged-transactions";
    @Builder.Default
    private String sinkFilePath = "output/tagged-transactions";

    // Flink configuration
    @Builder.Default
    private int parallelism = 1;
    @Builder.Default
    private boolean checkpointingEnabled = true;
    @Builder.Default
    private long checkpointIntervalMs = 60000; // 1 minute
    @Builder.Default
    private long checkpointTimeoutMs = 600000; // 10 minutes
    @Builder.Default
    private int restartAttempts = 3;
    @Builder.Default
    private int restartDelaySeconds = 10;

    // Drools configuration
    @Builder.Default
    private boolean droolsDebugEnabled = false;
    @Builder.Default
    private int maxRulesPerSession = 10000;

    /**
     * Create configuration from command line arguments.
     */
    public static DataPanelConfig fromArgs(String[] args) {
        DataPanelConfigBuilder builder = DataPanelConfig.builder();
        
        if (args != null) {
            for (String arg : args) {
                if (arg.startsWith("--")) {
                    String[] parts = arg.substring(2).split("=", 2);
                    if (parts.length == 2) {
                        setConfigValue(builder, parts[0], parts[1]);
                    }
                }
            }
        }
        
        return builder.build();
    }

    private static void setConfigValue(DataPanelConfigBuilder builder, String key, String value) {
        switch (key.toLowerCase()) {
            case "jobid" -> builder.jobId(value);
            case "controlpanelurl" -> builder.controlPanelUrl(value);
            case "rulerefreshintervalseconds" -> builder.ruleRefreshIntervalSeconds(Integer.parseInt(value));
            case "sourcetype" -> builder.sourceType(value);
            case "generatorratepersecond" -> builder.generatorRatePerSecond(Integer.parseInt(value));
            case "generatortotalrecords" -> builder.generatorTotalRecords(Integer.parseInt(value));
            case "kafkabootstrapservers" -> builder.kafkaBootstrapServers(value);
            case "kafkatopic" -> builder.kafkaTopic(value);
            case "kafkagroupid" -> builder.kafkaGroupId(value);
            case "sinktype" -> builder.sinkType(value);
            case "sinkkafkatopic" -> builder.sinkKafkaTopic(value);
            case "sinkfilepath" -> builder.sinkFilePath(value);
            case "parallelism" -> builder.parallelism(Integer.parseInt(value));
            case "checkpointingenabled" -> builder.checkpointingEnabled(Boolean.parseBoolean(value));
            case "checkpointintervalms" -> builder.checkpointIntervalMs(Long.parseLong(value));
            case "restartattempts" -> builder.restartAttempts(Integer.parseInt(value));
            case "restartdelayseconds" -> builder.restartDelaySeconds(Integer.parseInt(value));
        }
    }

    /**
     * Get Control Panel URL for fetching rules.
     */
    public String getRulesFetchUrl() {
        return controlPanelUrl + "/v1/internal/rules/published";
    }

    /**
     * Validate configuration.
     */
    public void validate() {
        Preconditions.checkNotNull(jobId, "Job ID cannot be null");
        Preconditions.checkNotNull(controlPanelUrl, "Control Panel URL cannot be null");
        Preconditions.checkNotNull(sourceType, "Source type cannot be null");
        Preconditions.checkNotNull(sinkType, "Sink type cannot be null");
        
        if (sourceType.equals("kafka")) {
            Preconditions.checkNotNull(kafkaBootstrapServers, "Kafka bootstrap servers required for Kafka source");
            Preconditions.checkNotNull(kafkaTopic, "Kafka topic required for Kafka source");
        }
    }
}
