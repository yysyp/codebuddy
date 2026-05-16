package com.cmdwrapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "jasypt.encryptor.password=test-password",
    "command-wrapper.passwords.password:db=test-password-123"
})
class CommandWrapperApplicationTests {

    @Test
    void contextLoads() {
        // Verify application context loads successfully
    }
}
