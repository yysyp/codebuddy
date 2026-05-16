package com.cmdwrapper.service;

import com.cmdwrapper.config.CommandWrapperProperties;
import com.cmdwrapper.config.CommandWrapperProperties.PlaceholderConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for password placeholder replacement.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordService {

    private final CommandWrapperProperties properties;

    /**
     * Replace placeholders in the command with actual password values.
     *
     * @param command the original command with placeholders
     * @return the command with placeholders replaced by actual passwords
     */
    public String replacePlaceholders(String command) {
        if (command == null || command.isEmpty()) {
            return command;
        }

        PlaceholderConfig config = properties.getPlaceholder();
        String prefix = config.getPrefix();
        String suffix = config.getSuffix();
        String separator = config.getSeparator();

        // Build regex pattern for placeholders
        // Format: ${password:key} or ${secret:key}
        String regex = Pattern.quote(prefix) + "([^:]+)" + Pattern.quote(separator) + "([^" + Pattern.quote(suffix) + "]+)" + Pattern.quote(suffix);
        Pattern pattern = Pattern.compile(regex);

        StringBuffer result = new StringBuffer();
        Matcher matcher = pattern.matcher(command);

        while (matcher.find()) {
            String type = matcher.group(1);
            String key = matcher.group(2);
            String replacement = getPassword(type, key);
            
            if (replacement != null) {
                // Escape special regex characters in replacement
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            } else {
                log.warn("Password placeholder not found: {}:{}", type, key);
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Get password from configuration.
     *
     * @param type the password type (e.g., "password", "secret")
     * @param key the password key
     * @return the decrypted password value, or null if not found
     */
    private String getPassword(String type, String key) {
        Map<String, String> passwords = properties.getPasswords();
        String fullKey = type + ":" + key;
        String encryptedPassword = passwords.get(fullKey);
        
        if (encryptedPassword != null) {
            // The password is already decrypted by Jasypt during configuration binding
            return encryptedPassword;
        }
        
        // Try just the key
        String directKey = passwords.get(key);
        if (directKey != null) {
            return directKey;
        }
        
        return null;
    }

    /**
     * Validate that all placeholders in the command have corresponding passwords.
     *
     * @param command the command to validate
     * @return true if all placeholders are valid
     */
    public boolean validatePlaceholders(String command) {
        if (command == null || command.isEmpty()) {
            return true;
        }

        PlaceholderConfig config = properties.getPlaceholder();
        String prefix = config.getPrefix();
        String suffix = config.getSuffix();
        String separator = config.getSeparator();

        String regex = Pattern.quote(prefix) + "([^:]+)" + Pattern.quote(separator) + "([^" + Pattern.quote(suffix) + "]+)" + Pattern.quote(suffix);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(command);

        while (matcher.find()) {
            String type = matcher.group(1);
            String key = matcher.group(2);
            if (getPassword(type, key) == null) {
                return false;
            }
        }

        return true;
    }
}
