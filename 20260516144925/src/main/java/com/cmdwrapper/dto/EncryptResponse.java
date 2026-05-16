package com.cmdwrapper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for encryption result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncryptResponse {

    /**
     * The encrypted value wrapped in ENC()
     */
    private String encrypted;

    /**
     * Whether the encryption was successful
     */
    private boolean success;

    /**
     * Error message if encryption failed
     */
    private String error;
}
