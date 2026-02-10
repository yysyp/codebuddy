package com.transaction.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pagination metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Pagination metadata")
public class PaginationMeta {

    @Schema(description = "Current page number (1-based)")
    private int pageNumber;

    @Schema(description = "Number of items per page")
    private int pageSize;

    @Schema(description = "Total number of pages")
    private int totalPages;

    @Schema(description = "Total number of elements")
    private long totalElements;
}
