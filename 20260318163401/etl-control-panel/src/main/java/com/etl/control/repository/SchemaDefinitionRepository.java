package com.etl.control.repository;

import com.etl.control.entity.SchemaDefinition;
import com.etl.control.entity.SchemaDefinition.SchemaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Schema Definition Repository
 */
@Repository
public interface SchemaDefinitionRepository extends JpaRepository<SchemaDefinition, Long> {

    /**
     * Find by name
     */
    Optional<SchemaDefinition> findByName(String name);

    /**
     * Find by status
     */
    List<SchemaDefinition> findByStatus(SchemaStatus status);

    /**
     * Find by schema type
     */
    List<SchemaDefinition> findBySchemaType(String schemaType);

    /**
     * Check if name exists
     */
    boolean existsByName(String name);
}
