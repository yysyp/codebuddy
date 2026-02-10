package com.transaction.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Base entity class with audit fields
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Base entity with audit fields")
public abstract class BaseEntity {

    @Schema(description = "Creation timestamp in UTC")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    @Schema(description = "User who created the record")
    private String createdBy;

    @Schema(description = "Last update timestamp in UTC")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;

    @Schema(description = "User who last updated the record")
    private String updatedBy;

    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
