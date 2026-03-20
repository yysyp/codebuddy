package com.etl.control.service;

import com.etl.control.dto.SchemaRequest;
import com.etl.control.entity.SchemaDefinition;
import com.etl.control.entity.SchemaDefinition.SchemaStatus;
import com.etl.control.exception.BusinessException;
import com.etl.control.repository.SchemaDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Schema Service
 * Handles schema definition operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchemaService {

    private final SchemaDefinitionRepository schemaRepository;

    /**
     * Create a new schema
     */
    @Transactional
    public SchemaDefinition createSchema(SchemaRequest request, String username) {
        log.info("Creating new schema: {}", request.getName());

        // Check if schema name already exists
        if (schemaRepository.existsByName(request.getName())) {
            throw new BusinessException("SCHEMA_EXISTS", 
                    "Schema with name '" + request.getName() + "' already exists");
        }

        // Create schema entity
        SchemaDefinition schema = SchemaDefinition.builder()
                .name(request.getName())
                .description(request.getDescription())
                .schemaContent(request.getSchemaContent())
                .status(SchemaStatus.ACTIVE)
                .schemaType(request.getSchemaType())
                .metadata(request.getMetadata())
                .createdBy(username)
                .updatedBy(username)
                .build();

        schema = schemaRepository.save(schema);
        log.info("Schema created successfully with ID: {}", schema.getId());

        return schema;
    }

    /**
     * Update an existing schema
     */
    @Transactional
    public SchemaDefinition updateSchema(Long id, SchemaRequest request, String username) {
        log.info("Updating schema with ID: {}", id);

        SchemaDefinition schema = schemaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("SCHEMA_NOT_FOUND", 
                        "Schema not found with ID: " + id));

        // Update schema
        schema.setDescription(request.getDescription());
        schema.setSchemaContent(request.getSchemaContent());
        schema.setSchemaType(request.getSchemaType());
        schema.setMetadata(request.getMetadata());
        schema.setUpdatedBy(username);

        schema = schemaRepository.save(schema);
        log.info("Schema updated successfully: {}", schema.getId());

        return schema;
    }

    /**
     * Get schema by ID
     */
    public SchemaDefinition getSchemaById(Long id) {
        return schemaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("SCHEMA_NOT_FOUND", 
                        "Schema not found with ID: " + id));
    }

    /**
     * Get schema by name
     */
    public SchemaDefinition getSchemaByName(String name) {
        return schemaRepository.findByName(name)
                .orElseThrow(() -> new BusinessException("SCHEMA_NOT_FOUND", 
                        "Schema not found with name: " + name));
    }

    /**
     * Get all active schemas
     */
    public List<SchemaDefinition> getAllActiveSchemas() {
        return schemaRepository.findByStatus(SchemaStatus.ACTIVE);
    }

    /**
     * Get all schemas
     */
    public List<SchemaDefinition> getAllSchemas() {
        return schemaRepository.findAll();
    }

    /**
     * Delete a schema (deactivate)
     */
    @Transactional
    public void deleteSchema(Long id, String username) {
        log.info("Deactivating schema with ID: {}", id);

        SchemaDefinition schema = schemaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("SCHEMA_NOT_FOUND", 
                        "Schema not found with ID: " + id));

        schema.setStatus(SchemaStatus.INACTIVE);
        schema.setUpdatedBy(username);
        schemaRepository.save(schema);

        log.info("Schema deactivated successfully: {}", id);
    }
}
