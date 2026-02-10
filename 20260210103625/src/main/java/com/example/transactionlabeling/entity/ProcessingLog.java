package com.example.transactionlabeling.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.*;
import lombok.*;
import lombok.*;

import java.time.Instant;

/**
 * ProcessingLog entity for tracking Flink processing operations
 */
@Entity
@Table(name = "processing_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String operationName;

    @Column(nullable = false, length = 50)
    private String status;

    @Column
    private Long recordsProcessed;

    @Column
    private Long executionTimeMs;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false)
    private Instant startTime;

    @Column
    private Instant endTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
