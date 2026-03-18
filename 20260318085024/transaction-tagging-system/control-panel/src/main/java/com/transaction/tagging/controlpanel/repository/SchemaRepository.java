package com.transaction.tagging.controlpanel.repository;

import com.transaction.tagging.controlpanel.entity.SchemaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SchemaEntity.
 */
@Repository
public interface SchemaRepository extends JpaRepository<SchemaEntity, Long> {

    /**
     * Find schema by name
     */
    Optional<SchemaEntity> findBySchemaName(String schemaName);

    /**
     * Find schema by name and version
     */
    Optional<SchemaEntity> findBySchemaNameAndSchemaVersion(String schemaName, String schemaVersion);

    /**
     * Find all active schemas
     */
    List<SchemaEntity> findByActiveTrue();

    /**
     * Find schemas by type
     */
    List<SchemaEntity> findBySchemaType(String schemaType);

    /**
     * Find schemas by name containing
     */
    Page<SchemaEntity> findBySchemaNameContainingIgnoreCase(String schemaName, Pageable pageable);

    /**
     * Check if schema name exists
     */
    boolean existsBySchemaName(String schemaName);

    /**
     * Find all active schemas by type
     */
    List<SchemaEntity> findBySchemaTypeAndActiveTrue(String schemaType);
}
