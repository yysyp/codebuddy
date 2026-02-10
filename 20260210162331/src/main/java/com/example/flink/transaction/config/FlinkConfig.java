package com.example.flink.transaction.config;

import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.configuration.Configuration;

import java.io.Serializable;
import java.time.Duration;

/**
 * Flink application configuration
 * Centralized configuration management for all components
 */
public class FlinkConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    // Database configuration options
    public static final ConfigOption<String> DB_URL = ConfigOptions
            .key("database.url")
            .stringType()
            .defaultValue("jdbc:h2:mem:transactions;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");

    public static final ConfigOption<String> DB_USERNAME = ConfigOptions
            .key("database.username")
            .stringType()
            .defaultValue("sa");

    public static final ConfigOption<String> DB_PASSWORD = ConfigOptions
            .key("database.password")
            .stringType()
            .defaultValue("xxxxxxxx");

    // Rule engine configuration
    public static final ConfigOption<String> RULE_FILE_PATH = ConfigOptions
            .key("rules.file.path")
            .stringType()
            .defaultValue("rules/TransactionRule.drl");

    public static final ConfigOption<Long> RULE_RELOAD_INTERVAL_MS = ConfigOptions
            .key("rules.reload.interval.ms")
            .longType()
            .defaultValue(60000L); // Reload rules every 60 seconds

    // Output configuration
    public static final ConfigOption<String> OUTPUT_PATH = ConfigOptions
            .key("output.path")
            .stringType()
            .defaultValue("output/transactions.parquet");

    // Parquet configuration
    public static final ConfigOption<Integer> PARQUET_ROW_GROUP_SIZE = ConfigOptions
            .key("parquet.row.group.size")
            .intType()
            .defaultValue(128 * 1024 * 1024); // 128MB

    public static final ConfigOption<Integer> PARQUET_PAGE_SIZE = ConfigOptions
            .key("parquet.page.size")
            .intType()
            .defaultValue(1024 * 1024); // 1MB

    // Checkpointing configuration
    public static final ConfigOption<Long> CHECKPOINT_INTERVAL_MS = ConfigOptions
            .key("checkpoint.interval.ms")
            .longType()
            .defaultValue(10000L); // 10 seconds

    public static final ConfigOption<Integer> CHECKPOINT_TIMEOUT_MS = ConfigOptions
            .key("checkpoint.timeout.ms")
            .intType()
            .defaultValue(60000); // 60 seconds

    // Rate limiting configuration
    public static final ConfigOption<Long> RATE_LIMIT_PER_SECOND = ConfigOptions
            .key("rate.limit.per.second")
            .longType()
            .defaultValue(1000L);

    private final Configuration configuration;

    public FlinkConfig(Configuration configuration) {
        this.configuration = configuration;
    }

    public FlinkConfig() {
        this.configuration = new Configuration();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    // Getters for configuration values
    public String getDatabaseUrl() {
        return configuration.getString(DB_URL);
    }

    public String getDatabaseUsername() {
        return configuration.getString(DB_USERNAME);
    }

    public String getDatabasePassword() {
        return configuration.getString(DB_PASSWORD);
    }

    public String getRuleFilePath() {
        return configuration.getString(RULE_FILE_PATH);
    }

    public long getRuleReloadIntervalMs() {
        return configuration.getLong(RULE_RELOAD_INTERVAL_MS);
    }

    public String getOutputPath() {
        return configuration.getString(OUTPUT_PATH);
    }

    public int getParquetRowGroupSize() {
        return configuration.getInteger(PARQUET_ROW_GROUP_SIZE);
    }

    public int getParquetPageSize() {
        return configuration.getInteger(PARQUET_PAGE_SIZE);
    }

    public Duration getCheckpointInterval() {
        return Duration.ofMillis(configuration.getLong(CHECKPOINT_INTERVAL_MS));
    }

    public int getCheckpointTimeout() {
        return configuration.getInteger(CHECKPOINT_TIMEOUT_MS);
    }

    public long getRateLimitPerSecond() {
        return configuration.getLong(RATE_LIMIT_PER_SECOND);
    }

    /**
     * Builder pattern for fluent configuration
     */
    public static class Builder {
        private final Configuration config = new Configuration();

        public Builder setDatabaseUrl(String url) {
            config.setString(DB_URL, url);
            return this;
        }

        public Builder setDatabaseUsername(String username) {
            config.setString(DB_USERNAME, username);
            return this;
        }

        public Builder setDatabasePassword(String password) {
            config.setString(DB_PASSWORD, password);
            return this;
        }

        public Builder setRuleFilePath(String path) {
            config.setString(RULE_FILE_PATH, path);
            return this;
        }

        public Builder setOutputPath(String path) {
            config.setString(OUTPUT_PATH, path);
            return this;
        }

        public Builder setCheckpointInterval(Duration interval) {
            config.setLong(CHECKPOINT_INTERVAL_MS, interval.toMillis());
            return this;
        }

        public FlinkConfig build() {
            return new FlinkConfig(config);
        }
    }
}
