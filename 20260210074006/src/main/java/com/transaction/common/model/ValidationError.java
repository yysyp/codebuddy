package com.transaction.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Validation error details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Field validation error")
public class ValidationError {

    @Schema(description = "Field name that failed validation")
    private String field;

    @Schema(description = "Error code")
    private String code;

    @Schema(description = "Error message")
    private String message;

    public static ValidationError of(String field, String code, String message) {
        return ValidationError.builder()
                .field(field)
                .code(code)
                .message(message)
                .build();
    }
}
