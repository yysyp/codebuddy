package com.etl.control.controller;

import com.etl.control.dto.ApiResponse;
import com.etl.control.dto.SchemaRequest;
import com.etl.control.entity.SchemaDefinition;
import com.etl.control.service.SchemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Schema Controller
 * REST API endpoints for schema management
 */
@Tag(name = "Schema Management", description = "APIs for managing data schemas")
@RestController
@RequestMapping("/api/v1/schemas")
@RequiredArgsConstructor
public class SchemaController {

    private final SchemaService schemaService;

    @Operation(summary = "Create a new schema", description = "Creates a new schema definition")
    @PostMapping
    public ResponseEntity<ApiResponse<SchemaDefinition>> createSchema(
            @Valid @RequestBody SchemaRequest request,
            @RequestHeader(value = "X-User", defaultValue = "system") String username) {
        SchemaDefinition response = schemaService.createSchema(request, username);
        return ResponseEntity.ok(ApiResponse.success(response, "Schema created successfully"));
    }

    @Operation(summary = "Update a schema", description = "Updates an existing schema")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SchemaDefinition>> updateSchema(
            @Parameter(description = "Schema ID") @PathVariable Long id,
            @Valid @RequestBody SchemaRequest request,
            @RequestHeader(value = "X-User", defaultValue = "system") String username) {
        SchemaDefinition response = schemaService.updateSchema(id, request, username);
        return ResponseEntity.ok(ApiResponse.success(response, "Schema updated successfully"));
    }

    @Operation(summary = "Get schema by ID", description = "Retrieves a schema definition by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SchemaDefinition>> getSchemaById(
            @Parameter(description = "Schema ID") @PathVariable Long id) {
        SchemaDefinition response = schemaService.getSchemaById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get schema by name", description = "Retrieves a schema definition by name")
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<SchemaDefinition>> getSchemaByName(
            @Parameter(description = "Schema name") @PathVariable String name) {
        SchemaDefinition response = schemaService.getSchemaByName(name);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get all schemas", description = "Retrieves all schemas")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SchemaDefinition>>> getAllSchemas() {
        List<SchemaDefinition> response = schemaService.getAllSchemas();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get all active schemas", description = "Retrieves all active schemas")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<SchemaDefinition>>> getAllActiveSchemas() {
        List<SchemaDefinition> response = schemaService.getAllActiveSchemas();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Delete a schema", description = "Deactivates a schema")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSchema(
            @Parameter(description = "Schema ID") @PathVariable Long id,
            @RequestHeader(value = "X-User", defaultValue = "system") String username) {
        schemaService.deleteSchema(id, username);
        return ResponseEntity.ok(ApiResponse.success(null, "Schema deactivated successfully"));
    }
}
