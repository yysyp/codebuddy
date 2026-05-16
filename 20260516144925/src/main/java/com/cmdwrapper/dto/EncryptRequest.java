package com.cmdwrapper.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for password encryption.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncryptRequest {

    /**
     * The plaintext password to encrypt
     */
    @NotBlank(message = "Plaintext cannot be blank")
    private String plaintext;
}
