package com.example.flink.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * Result of the tagging operation containing metadata about the tagging process.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaggingResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Whether tagging was successful
     */
    private boolean success;

    /**
     * List of assigned tags
     */
    private List<String> tags;

    /**
     * Primary/most important tag
     */
    private String primaryTag;

    /**
     * Number of rules triggered
     */
    private int rulesTriggered;

    /**
     * Processing timestamp in UTC
     */
    private Instant processedAt;

    /**
     * Trace ID for distributed tracing
     */
    private String traceId;

    /**
     * Error message if tagging failed
     */
    private String errorMessage;

    /**
     * Processing time in milliseconds
     */
    private long processingTimeMs;

    /**
     * Create a successful result
     */
    public static TaggingResult success(List<String> tags, String traceId) {
        return TaggingResult.builder()
                .success(true)
                .tags(tags)
                .primaryTag(tags != null && !tags.isEmpty() ? tags.get(0) : "UNTAGGED")
                .rulesTriggered(tags != null ? tags.size() : 0)
                .processedAt(Instant.now())
                .traceId(traceId)
                .processingTimeMs(0)
                .build();
    }

    /**
     * Create a failed result
     */
    public static TaggingResult failure(String errorMessage, String traceId) {
        return TaggingResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .processedAt(Instant.now())
                .traceId(traceId)
                .build();
    }
}
