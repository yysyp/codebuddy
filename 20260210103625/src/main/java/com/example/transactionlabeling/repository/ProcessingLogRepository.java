package com.example.transactionlabeling.repository;

import com.example.transactionlabeling.entity.ProcessingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ProcessingLog entity
 */
@Repository
public interface ProcessingLogRepository extends JpaRepository<ProcessingLog, Long> {

    List<ProcessingLog> findByOperationName(String operationName);

    List<ProcessingLog> findByStatus(String status);

    Optional<ProcessingLog> findTopByOperationNameOrderByStartTimeDesc(String operationName);
}
