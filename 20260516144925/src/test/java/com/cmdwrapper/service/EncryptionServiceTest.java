package com.cmdwrapper.service;

import com.cmdwrapper.dto.DecryptResponse;
import com.cmdwrapper.dto.EncryptResponse;
import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EncryptionServiceTest {

    @Mock
    private StringEncryptor mockEncryptor;

    private EncryptionService encryptionService;

    private static final String TEST_PASSWORD = "testPassword123";
    private static final String TEST_PLAINTEXT = "mySecretPassword";
    private static final String TEST_ENCRYPTED = "encryptedValue123";

    @BeforeEach
    void setUp() {
        when(mockEncryptor.encrypt(TEST_PLAINTEXT)).thenReturn(TEST_ENCRYPTED);
        when(mockEncryptor.decrypt(TEST_ENCRYPTED)).thenReturn(TEST_PLAINTEXT);

        encryptionService = new EncryptionService(mockEncryptor);
        ReflectionTestUtils.setField(encryptionService, "algorithm", "PBEWITHHMACSHA512ANDAES_256");
    }

    @Test
    void testEncrypt_Success() {
        EncryptResponse response = encryptionService.encrypt(TEST_PLAINTEXT);

        assertTrue(response.isSuccess());
        assertEquals("ENC(" + TEST_ENCRYPTED + ")", response.getEncrypted());
        assertNull(response.getError());
        verify(mockEncryptor).encrypt(TEST_PLAINTEXT);
    }

    @Test
    void testEncrypt_Error() {
        when(mockEncryptor.encrypt(TEST_PLAINTEXT)).thenThrow(new RuntimeException("Encryption error"));

        EncryptResponse response = encryptionService.encrypt(TEST_PLAINTEXT);

        assertFalse(response.isSuccess());
        assertNull(response.getEncrypted());
        assertNotNull(response.getError());
        assertTrue(response.getError().contains("Encryption failed"));
    }

    @Test
    void testEncryptWithPassword_Success() {
        // The encryptWithPassword method creates its own encryptor,
        // so we just verify it returns successfully with an encrypted value
        EncryptResponse response = encryptionService.encryptWithPassword(TEST_PLAINTEXT, TEST_PASSWORD);

        assertTrue(response.isSuccess());
        assertNotNull(response.getEncrypted());
        assertTrue(response.getEncrypted().startsWith("ENC("));
        assertTrue(response.getEncrypted().endsWith(")"));
        assertNull(response.getError());
    }

    @Test
    void testDecrypt_Success() {
        DecryptResponse response = encryptionService.decrypt(TEST_ENCRYPTED);

        assertTrue(response.isSuccess());
        assertEquals(TEST_PLAINTEXT, response.getDecrypted());
        assertNull(response.getError());
        verify(mockEncryptor).decrypt(TEST_ENCRYPTED);
    }

    @Test
    void testDecrypt_WithENCWrapper() {
        DecryptResponse response = encryptionService.decrypt("ENC(" + TEST_ENCRYPTED + ")");

        assertTrue(response.isSuccess());
        assertEquals(TEST_PLAINTEXT, response.getDecrypted());
        verify(mockEncryptor).decrypt(TEST_ENCRYPTED);
    }

    @Test
    void testDecrypt_Error() {
        when(mockEncryptor.decrypt(TEST_ENCRYPTED)).thenThrow(new RuntimeException("Decryption error"));

        DecryptResponse response = encryptionService.decrypt(TEST_ENCRYPTED);

        assertFalse(response.isSuccess());
        assertNull(response.getDecrypted());
        assertNotNull(response.getError());
        assertTrue(response.getError().contains("Decryption failed"));
    }

    @Test
    void testDecryptWithPassword_Success() {
        // The decryptWithPassword method creates its own encryptor,
        // so we verify it handles the decryption correctly
        // First encrypt to get a value that can be decrypted
        EncryptResponse encryptResponse = encryptionService.encryptWithPassword(TEST_PLAINTEXT, TEST_PASSWORD);
        assertTrue(encryptResponse.isSuccess());

        // Then decrypt with the same password
        DecryptResponse response = encryptionService.decryptWithPassword(encryptResponse.getEncrypted(), TEST_PASSWORD);

        assertTrue(response.isSuccess());
        assertEquals(TEST_PLAINTEXT, response.getDecrypted());
        assertNull(response.getError());
    }
}
