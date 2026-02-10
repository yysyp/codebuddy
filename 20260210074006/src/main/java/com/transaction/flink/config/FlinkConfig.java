package com.transaction.flink.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.configuration.FlinkConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for Apache Flink stream processing
 */
@Slf4j
@Configuration
public class FlinkConfig {

    @Value("${app.flink.parallelism:2}")
    private int parallelism;

    @Value("${app.flink.checkpoint-interval:5000}")
    private long checkpointInterval;

    @Value("${app.flink.checkpoint-dir:./flink-checkpoints}")
    private String checkpointDir;

    @Bean
    public StreamExecutionEnvironment streamExecutionEnvironment() {
        log.info("Initializing Flink StreamExecutionEnvironment with parallelism: {}", parallelism);
        
        // Create Flink execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        // Set parallelism
        env.setParallelism(parallelism);
        
        // Configure checkpointing
        env.enableCheckpointing(checkpointInterval);
        
        // Configure checkpoint mode
        CheckpointConfig checkpointConfig = env.getCheckpointConfig();
        checkpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        checkpointConfig.setCheckpointTimeout(60000); // 1 minute
        checkpointConfig.setMaxConcurrentCheckpoints(1);
        checkpointConfig.setMinPauseBetweenCheckpoints(500);
        checkpointConfig.setTolerableCheckpointFailureNumber(3);
        
        // Configure restart strategy
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
                3, // max number of restarts
                Time.of(10, TimeUnit.SECONDS) // delay between restarts
        ));
        
        // Configure state backend (using filesystem state backend)
        FlinkConfiguration config = new FlinkConfiguration();
        config.setString("state.backend", "filesystem");
        config.setString("state.checkpoints.dir", "file://" + checkpointDir);
        env.getConfig().setGlobalJobParameters(config);
        
        log.info("Flink StreamExecutionEnvironment initialized successfully");
        return env;
    }
}
