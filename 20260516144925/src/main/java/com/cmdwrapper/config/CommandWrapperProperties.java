package com.cmdwrapper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for the Command Wrapper Service.
 */
@Data
@Component
@ConfigurationProperties(prefix = "command-wrapper")
public class CommandWrapperProperties {

    /**
     * Placeholder configuration
     */
    private PlaceholderConfig placeholder = new PlaceholderConfig();

    /**
     * Encrypted passwords map (key -> encrypted value)
     */
    private Map<String, String> passwords = new HashMap<>();

    /**
     * Default command execution timeout in seconds
     */
    private int defaultTimeoutSeconds = 300;

    /**
     * Maximum concurrent commands
     */
    private int maxConcurrentCommands = 10;

    /**
     * Working directory for command execution
     */
    private String workingDirectory = System.getProperty("user.dir");

    @Data
    public static class PlaceholderConfig {
        /**
         * Opening delimiter for placeholders
         */
        private String prefix = "${";

        /**
         * Closing delimiter for placeholders
         */
        private String suffix = "}";

        /**
         * Separator between type and key (e.g., ${password:db_password})
         */
        private String separator = ":";
    }
}
