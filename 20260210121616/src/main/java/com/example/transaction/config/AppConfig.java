package com.example.transaction.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Logger;

public class AppConfig {
    private static final Logger logger = Logger.getLogger(AppConfig.class.getName());
    
    private String dbUrl;
    private String dbDriver;
    private String dbUsername;
    private String dbPassword;
    private int parallelism;
    private String rulesDirectory;
    private String outputDirectory;
    
    public static AppConfig load() throws IOException {
        AppConfig config = new AppConfig();
        Properties props = new Properties();
        
        try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
            }
        }
        
        config.dbUrl = props.getProperty("db.url", "jdbc:h2:./data/transactions;MODE=PostgreSQL;AUTO_SERVER=TRUE");
        config.dbDriver = props.getProperty("db.driver", "org.h2.Driver");
        config.dbUsername = props.getProperty("db.username", "sa");
        config.dbPassword = props.getProperty("db.password", "xxxxxxxx");
        config.parallelism = Integer.parseInt(props.getProperty("parallelism", "4"));
        config.rulesDirectory = props.getProperty("rules.directory", "rules");
        config.outputDirectory = props.getProperty("output.directory", "output");
        
        Path outputDir = Paths.get(config.outputDirectory);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        
        logger.info("Config loaded: dbUrl=" + config.dbUrl + ", parallelism=" + config.parallelism);
        return config;
    }
    
    public String getDbUrl() { return dbUrl; }
    public void setDbUrl(String dbUrl) { this.dbUrl = dbUrl; }
    
    public String getDbDriver() { return dbDriver; }
    public void setDbDriver(String dbDriver) { this.dbDriver = dbDriver; }
    
    public String getDbUsername() { return dbUsername; }
    public void setDbUsername(String dbUsername) { this.dbUsername = dbUsername; }
    
    public String getDbPassword() { return dbPassword; }
    public void setDbPassword(String dbPassword) { this.dbPassword = dbPassword; }
    
    public int getParallelism() { return parallelism; }
    public void setParallelism(int parallelism) { this.parallelism = parallelism; }
    
    public String getRulesDirectory() { return rulesDirectory; }
    public void setRulesDirectory(String rulesDirectory) { this.rulesDirectory = rulesDirectory; }
    
    public String getOutputDirectory() { return outputDirectory; }
    public void setOutputDirectory(String outputDirectory) { this.outputDirectory = outputDirectory; }
}
