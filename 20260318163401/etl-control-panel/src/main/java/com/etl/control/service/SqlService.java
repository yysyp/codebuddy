package com.etl.control.service;

import com.etl.control.dto.SqlRequest;
import com.etl.control.entity.SqlDefinition;
import com.etl.control.entity.SqlDefinition.SqlStatus;
import com.etl.control.entity.SqlDefinition.SqlType;
import com.etl.control.exception.BusinessException;
import com.etl.control.repository.SqlDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SQL Service
 * Handles SQL definition operations for Flink
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SqlService {

    private final SqlDefinitionRepository sqlRepository;

    /**
     * Create a new SQL definition
     */
    @Transactional
    public SqlDefinition createSql(SqlRequest request, String username) {
        log.info("Creating new SQL definition: {}", request.getName());

        // Check if SQL name already exists
        if (sqlRepository.existsByName(request.getName())) {
            throw new BusinessException("SQL_EXISTS", 
                    "SQL definition with name '" + request.getName() + "' already exists");
        }

        // Create SQL entity
        SqlDefinition sql = SqlDefinition.builder()
                .name(request.getName())
                .description(request.getDescription())
                .sqlContent(request.getSqlContent())
                .sqlType(SqlType.valueOf(request.getSqlType()))
                .status(SqlStatus.ACTIVE)
                .associatedSchema(request.getAssociatedSchema())
                .parameters(request.getParameters())
                .createdBy(username)
                .updatedBy(username)
                .build();

        sql = sqlRepository.save(sql);
        log.info("SQL definition created successfully with ID: {}", sql.getId());

        return sql;
    }

    /**
     * Update an existing SQL definition
     */
    @Transactional
    public SqlDefinition updateSql(Long id, SqlRequest request, String username) {
        log.info("Updating SQL definition with ID: {}", id);

        SqlDefinition sql = sqlRepository.findById(id)
                .orElseThrow(() -> new BusinessException("SQL_NOT_FOUND", 
                        "SQL definition not found with ID: " + id));

        // Update SQL
        sql.setDescription(request.getDescription());
        sql.setSqlContent(request.getSqlContent());
        sql.setSqlType(SqlType.valueOf(request.getSqlType()));
        sql.setAssociatedSchema(request.getAssociatedSchema());
        sql.setParameters(request.getParameters());
        sql.setUpdatedBy(username);

        sql = sqlRepository.save(sql);
        log.info("SQL definition updated successfully: {}", sql.getId());

        return sql;
    }

    /**
     * Get SQL by ID
     */
    public SqlDefinition getSqlById(Long id) {
        return sqlRepository.findById(id)
                .orElseThrow(() -> new BusinessException("SQL_NOT_FOUND", 
                        "SQL definition not found with ID: " + id));
    }

    /**
     * Get SQL by name
     */
    public SqlDefinition getSqlByName(String name) {
        return sqlRepository.findByName(name)
                .orElseThrow(() -> new BusinessException("SQL_NOT_FOUND", 
                        "SQL definition not found with name: " + name));
    }

    /**
     * Get all SQL definitions by type
     */
    public List<SqlDefinition> getSqlByType(SqlType sqlType) {
        return sqlRepository.findBySqlType(sqlType);
    }

    /**
     * Get active SQL definitions by type
     */
    public List<SqlDefinition> getActiveSqlByType(SqlType sqlType) {
        return sqlRepository.findBySqlTypeAndStatus(sqlType, SqlStatus.ACTIVE);
    }

    /**
     * Get all active SQL definitions
     */
    public List<SqlDefinition> getAllActiveSql() {
        return sqlRepository.findByStatus(SqlStatus.ACTIVE);
    }

    /**
     * Get SQL definitions by associated schema
     */
    public List<SqlDefinition> getSqlBySchema(String schemaName) {
        return sqlRepository.findByAssociatedSchema(schemaName);
    }

    /**
     * Get all SQL definitions
     */
    public List<SqlDefinition> getAllSql() {
        return sqlRepository.findAll();
    }

    /**
     * Delete a SQL definition (deactivate)
     */
    @Transactional
    public void deleteSql(Long id, String username) {
        log.info("Deactivating SQL definition with ID: {}", id);

        SqlDefinition sql = sqlRepository.findById(id)
                .orElseThrow(() -> new BusinessException("SQL_NOT_FOUND", 
                        "SQL definition not found with ID: " + id));

        sql.setStatus(SqlStatus.INACTIVE);
        sql.setUpdatedBy(username);
        sqlRepository.save(sql);

        log.info("SQL definition deactivated successfully: {}", id);
    }
}
