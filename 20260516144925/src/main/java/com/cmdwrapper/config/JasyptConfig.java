package com.cmdwrapper.config;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jasypt encryption configuration.
 * Uses PBEWITHHMACSHA512ANDAES_256 algorithm with RandomIvGenerator for secure encryption.
 */
@Configuration
public class JasyptConfig {

    @Value("${jasypt.encryptor.password:}")
    private String encryptorPassword;

    @Value("${jasypt.encryptor.algorithm:PBEWITHHMACSHA512ANDAES_256}")
    private String algorithm;

    /**
     * StringEncryptor bean for password encryption/decryption.
     * Uses PBEWITHHMACSHA512ANDAES_256 algorithm with RandomIvGenerator.
     */
    @Bean(name = "stringEncryptor")
    public StringEncryptor stringEncryptor() {
        if (encryptorPassword == null || encryptorPassword.isEmpty()) {
            throw new IllegalStateException(
                "Jasypt encryption password must be provided via --jasypt.encryptor.password argument");
        }

        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(encryptorPassword);
        config.setAlgorithm(algorithm);
        config.setKeyObtentionIterations("1000");
        config.setPoolSize(1);
        config.setProviderName("SunJCE");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        encryptor.setConfig(config);

        return encryptor;
    }

    /**
     * Validate that encryption password is provided.
     */
    @Bean
    public String jasyptPasswordValidator() {
        if (encryptorPassword == null || encryptorPassword.isEmpty()) {
            throw new IllegalStateException(
                "Jasypt encryption password must be provided via --jasypt.encryptor.password argument");
        }
        return encryptorPassword;
    }
}
