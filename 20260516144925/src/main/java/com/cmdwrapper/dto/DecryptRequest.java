package com.cmdwrapper.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for password decryption.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecryptRequest {

    /**
     * The encrypted text to decrypt (can be with or without ENC() wrapper)
     */
    @NotBlank(message = "Ciphertext cannot be blank")
    private String ciphertext;
}
