package com.cmdwrapper.service;

import com.cmdwrapper.config.CommandWrapperProperties;
import com.cmdwrapper.config.CommandWrapperProperties.PlaceholderConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PasswordServiceTest {

    @Mock
    private CommandWrapperProperties properties;

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        PlaceholderConfig placeholderConfig = new PlaceholderConfig();
        placeholderConfig.setPrefix("${");
        placeholderConfig.setSuffix("}");
        placeholderConfig.setSeparator(":");

        when(properties.getPlaceholder()).thenReturn(placeholderConfig);
        
        Map<String, String> passwords = new HashMap<>();
        passwords.put("password:db", "myDbPassword123");
        passwords.put("password:ssh", "mySshPassword456");
        passwords.put("secret:api", "myApiKey789");
        when(properties.getPasswords()).thenReturn(passwords);

        passwordService = new PasswordService(properties);
    }

    @Test
    void testReplacePlaceholders_SinglePlaceholder() {
        String command = "mysql -u root -p${password:db}";
        String result = passwordService.replacePlaceholders(command);
        
        assertEquals("mysql -u root -pmyDbPassword123", result);
    }

    @Test
    void testReplacePlaceholders_MultiplePlaceholders() {
        String command = "ssh user@host -p ${password:ssh} && curl -H 'X-API-Key: ${secret:api}' http://api.example.com";
        String result = passwordService.replacePlaceholders(command);
        
        assertEquals("ssh user@host -p mySshPassword456 && curl -H 'X-API-Key: myApiKey789' http://api.example.com", result);
    }

    @Test
    void testReplacePlaceholders_NoPlaceholders() {
        String command = "echo 'Hello World'";
        String result = passwordService.replacePlaceholders(command);
        
        assertEquals("echo 'Hello World'", result);
    }

    @Test
    void testReplacePlaceholders_EmptyCommand() {
        String command = "";
        String result = passwordService.replacePlaceholders(command);
        
        assertEquals("", result);
    }

    @Test
    void testReplacePlaceholders_NullCommand() {
        String result = passwordService.replacePlaceholders(null);
        
        assertNull(result);
    }

    @Test
    void testReplacePlaceholders_PartialMatch() {
        String command = "echo ${notfound:key} and ${password:db}";
        String result = passwordService.replacePlaceholders(command);
        
        assertTrue(result.contains("myDbPassword123"));
    }

    @Test
    void testValidatePlaceholders_AllValid() {
        String command = "mysql -u root -p${password:db} -h ${secret:api}";
        boolean result = passwordService.validatePlaceholders(command);
        
        assertTrue(result);
    }

    @Test
    void testValidatePlaceholders_MissingPlaceholder() {
        String command = "mysql -u root -p${password:nonexistent}";
        boolean result = passwordService.validatePlaceholders(command);
        
        assertFalse(result);
    }

    @Test
    void testValidatePlaceholders_NoPlaceholders() {
        String command = "echo 'Hello World'";
        boolean result = passwordService.validatePlaceholders(command);
        
        assertTrue(result);
    }
}
