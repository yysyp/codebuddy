package com.cmdwrapper.service;

import com.cmdwrapper.dto.DecryptResponse;
import com.cmdwrapper.dto.EncryptResponse;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for password encryption and decryption operations.
 * Provides separate encryptors for different encryption passwords.
 */
@Slf4j
@Service
public class EncryptionService {

    private final StringEncryptor defaultEncryptor;

    @Value("${jasypt.encryptor.algorithm:PBEWITHHMACSHA512ANDAES_256}")
    private String algorithm;

    /**
     * Constructor injection of the default StringEncryptor.
     *
     * @param stringEncryptor the default encryptor configured in JasyptConfig
     */
    public EncryptionService(@Qualifier("stringEncryptor") StringEncryptor stringEncryptor) {
        this.defaultEncryptor = stringEncryptor;
    }

    /**
     * Encrypt a plaintext value using the default encryptor (from application config).
     *
     * @param plaintext the value to encrypt
     * @return encryption response with ENC() wrapped result
     */
    public EncryptResponse encrypt(String plaintext) {
        try {
            String encrypted = defaultEncryptor.encrypt(plaintext);
            return EncryptResponse.builder()
                .encrypted("ENC(" + encrypted + ")")
                .success(true)
                .build();
        } catch (Exception e) {
            log.error("Encryption failed: {}", e.getMessage(), e);
            return EncryptResponse.builder()
                .success(false)
                .error("Encryption failed: " + e.getMessage())
                .build();
        }
    }

    /**
     * Encrypt a plaintext value using a custom password.
     *
     * @param plaintext the value to encrypt
     * @param password the encryption password
     * @return encryption response with ENC() wrapped result
     */
    public EncryptResponse encryptWithPassword(String plaintext, String password) {
        try {
            StringEncryptor encryptor = createEncryptor(password);
            String encrypted = encryptor.encrypt(plaintext);
            return EncryptResponse.builder()
                .encrypted("ENC(" + encrypted + ")")
                .success(true)
                .build();
        } catch (Exception e) {
            log.error("Encryption failed: {}", e.getMessage(), e);
            return EncryptResponse.builder()
                .success(false)
                .error("Encryption failed: " + e.getMessage())
                .build();
        }
    }

    /**
     * Decrypt a ciphertext using the default encryptor (from application config).
     *
     * @param ciphertext the encrypted value (can be with or without ENC() wrapper)
     * @return decryption response with plaintext result
     */
    public DecryptResponse decrypt(String ciphertext) {
        try {
            String decrypted = decryptValue(defaultEncryptor, ciphertext);
            return DecryptResponse.builder()
                .decrypted(decrypted)
                .success(true)
                .build();
        } catch (Exception e) {
            log.error("Decryption failed: {}", e.getMessage(), e);
            return DecryptResponse.builder()
                .success(false)
                .error("Decryption failed: " + e.getMessage())
                .build();
        }
    }

    /**
     * Decrypt a ciphertext using a custom password.
     *
     * @param ciphertext the encrypted value (can be with or without ENC() wrapper)
     * @param password the decryption password
     * @return decryption response with plaintext result
     */
    public DecryptResponse decryptWithPassword(String ciphertext, String password) {
        try {
            StringEncryptor encryptor = createEncryptor(password);
            String decrypted = decryptValue(encryptor, ciphertext);
            return DecryptResponse.builder()
                .decrypted(decrypted)
                .success(true)
                .build();
        } catch (Exception e) {
            log.error("Decryption failed: {}", e.getMessage(), e);
            return DecryptResponse.builder()
                .success(false)
                .error("Decryption failed: " + e.getMessage())
                .build();
        }
    }

    /**
     * Create a new encryptor with the specified password.
     */
    private StringEncryptor createEncryptor(String password) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(password);
        config.setAlgorithm(algorithm);
        config.setKeyObtentionIterations("1000");
        config.setPoolSize(1);
        config.setProviderName("SunJCE");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        encryptor.setConfig(config);
        return encryptor;
    }

    /**
     * Strip ENC() wrapper if present and decrypt.
     */
    private String decryptValue(StringEncryptor encryptor, String ciphertext) {
        String value = ciphertext.trim();
        // Remove ENC() wrapper if present
        if (value.startsWith("ENC(") && value.endsWith(")")) {
            value = value.substring(4, value.length() - 1);
        }
        return encryptor.decrypt(value);
    }
}
