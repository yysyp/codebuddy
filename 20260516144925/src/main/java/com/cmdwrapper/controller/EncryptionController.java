package com.cmdwrapper.controller;

import com.cmdwrapper.dto.DecryptRequest;
import com.cmdwrapper.dto.DecryptResponse;
import com.cmdwrapper.dto.EncryptRequest;
import com.cmdwrapper.dto.EncryptResponse;
import com.cmdwrapper.service.EncryptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for encryption and decryption operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/crypto")
@RequiredArgsConstructor
@Tag(name = "Encryption", description = "APIs for password encryption and decryption")
public class EncryptionController {

    private final EncryptionService encryptionService;

    @PostMapping("/encrypt")
    @Operation(summary = "Encrypt with custom password",
               description = "Encrypt a plaintext value using a custom encryption password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Encryption successful",
                     content = @Content(schema = @Schema(implementation = EncryptResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> encrypt(
            @Parameter(description = "Plaintext to encrypt") @RequestParam String plaintext,
            @Parameter(description = "Encryption password") @RequestParam String password) {
        
        log.info("Encrypt request received");

        EncryptResponse response = encryptionService.encryptWithPassword(plaintext, password);

        Map<String, Object> result = new HashMap<>();
        result.put("success", response.isSuccess());
        if (response.isSuccess()) {
            result.put("encrypted", response.getEncrypted());
        } else {
            result.put("error", response.getError());
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/encrypt/default")
    @Operation(summary = "Encrypt with default password",
               description = "Encrypt a plaintext value using the default encryption password from config")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Encryption successful",
                     content = @Content(schema = @Schema(implementation = EncryptResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> encryptWithDefault(
            @Valid @RequestBody EncryptRequest request) {
        
        log.info("Encrypt with default password request received");

        EncryptResponse response = encryptionService.encrypt(request.getPlaintext());

        Map<String, Object> result = new HashMap<>();
        result.put("success", response.isSuccess());
        if (response.isSuccess()) {
            result.put("encrypted", response.getEncrypted());
        } else {
            result.put("error", response.getError());
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/decrypt")
    @Operation(summary = "Decrypt with custom password",
               description = "Decrypt a ciphertext using a custom decryption password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Decryption successful",
                     content = @Content(schema = @Schema(implementation = DecryptResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> decrypt(
            @Parameter(description = "Ciphertext to decrypt (with or without ENC() wrapper)") @RequestParam String ciphertext,
            @Parameter(description = "Decryption password") @RequestParam String password) {
        
        log.info("Decrypt request received");

        DecryptResponse response = encryptionService.decryptWithPassword(ciphertext, password);

        Map<String, Object> result = new HashMap<>();
        result.put("success", response.isSuccess());
        if (response.isSuccess()) {
            result.put("decrypted", response.getDecrypted());
        } else {
            result.put("error", response.getError());
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/decrypt/default")
    @Operation(summary = "Decrypt with default password",
               description = "Decrypt a ciphertext using the default decryption password from config")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Decryption successful",
                     content = @Content(schema = @Schema(implementation = DecryptResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> decryptWithDefault(
            @Valid @RequestBody DecryptRequest request) {
        
        log.info("Decrypt with default password request received");

        DecryptResponse response = encryptionService.decrypt(request.getCiphertext());

        Map<String, Object> result = new HashMap<>();
        result.put("success", response.isSuccess());
        if (response.isSuccess()) {
            result.put("decrypted", response.getDecrypted());
        } else {
            result.put("error", response.getError());
        }

        return ResponseEntity.ok(result);
    }
}
