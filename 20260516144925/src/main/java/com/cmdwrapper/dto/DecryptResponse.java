package com.cmdwrapper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for decryption result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecryptResponse {

    /**
     * The decrypted plaintext value
     */
    private String decrypted;

    /**
     * Whether the decryption was successful
     */
    private boolean success;

    /**
     * Error message if decryption failed
     */
    private String error;
}
