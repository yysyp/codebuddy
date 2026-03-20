package com.etl.control.controller;

import com.etl.control.dto.ApiResponse;
import com.etl.control.dto.SqlRequest;
import com.etl.control.entity.SqlDefinition;
import com.etl.control.entity.SqlDefinition.SqlType;
import com.etl.control.service.SqlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SQL Controller
 * REST API endpoints for SQL definition management
 */
@Tag(name = "SQL Management", description = "APIs for managing Flink SQL definitions")
@RestController
@RequestMapping("/api/v1/sql")
@RequiredArgsConstructor
public class SqlController {

    private final SqlService sqlService;

    @Operation(summary = "Create a new SQL definition", description = "Creates a new SQL definition for Flink")
    @PostMapping
    public ResponseEntity<ApiResponse<SqlDefinition>> createSql(
            @Valid @RequestBody SqlRequest request,
            @RequestHeader(value = "X-User", defaultValue = "system") String username) {
        SqlDefinition response = sqlService.createSql(request, username);
        return ResponseEntity.ok(ApiResponse.success(response, "SQL definition created successfully"));
    }

    @Operation(summary = "Update a SQL definition", description = "Updates an existing SQL definition")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SqlDefinition>> updateSql(
            @Parameter(description = "SQL ID") @PathVariable Long id,
            @Valid @RequestBody SqlRequest request,
            @RequestHeader(value = "X-User", defaultValue = "system") String username) {
        SqlDefinition response = sqlService.updateSql(id, request, username);
        return ResponseEntity.ok(ApiResponse.success(response, "SQL definition updated successfully"));
    }

    @Operation(summary = "Get SQL by ID", description = "Retrieves a SQL definition by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SqlDefinition>> getSqlById(
            @Parameter(description = "SQL ID") @PathVariable Long id) {
        SqlDefinition response = sqlService.getSqlById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get SQL by name", description = "Retrieves a SQL definition by name")
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<SqlDefinition>> getSqlByName(
            @Parameter(description = "SQL name") @PathVariable String name) {
        SqlDefinition response = sqlService.getSqlByName(name);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get all SQL definitions", description = "Retrieves all SQL definitions")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SqlDefinition>>> getAllSql() {
        List<SqlDefinition> response = sqlService.getAllSql();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get all active SQL definitions", description = "Retrieves all active SQL definitions")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<SqlDefinition>>> getAllActiveSql() {
        List<SqlDefinition> response = sqlService.getAllActiveSql();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get SQL by type", description = "Retrieves SQL definitions by type")
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<SqlDefinition>>> getSqlByType(
            @Parameter(description = "SQL type") @PathVariable SqlType type) {
        List<SqlDefinition> response = sqlService.getSqlByType(type);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get active SQL by type", description = "Retrieves active SQL definitions by type")
    @GetMapping("/type/{type}/active")
    public ResponseEntity<ApiResponse<List<SqlDefinition>>> getActiveSqlByType(
            @Parameter(description = "SQL type") @PathVariable SqlType type) {
        List<SqlDefinition> response = sqlService.getActiveSqlByType(type);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get SQL by schema", description = "Retrieves SQL definitions by associated schema")
    @GetMapping("/schema/{schemaName}")
    public ResponseEntity<ApiResponse<List<SqlDefinition>>> getSqlBySchema(
            @Parameter(description = "Schema name") @PathVariable String schemaName) {
        List<SqlDefinition> response = sqlService.getSqlBySchema(schemaName);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Delete a SQL definition", description = "Deactivates a SQL definition")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSql(
            @Parameter(description = "SQL ID") @PathVariable Long id,
            @RequestHeader(value = "X-User", defaultValue = "system") String username) {
        sqlService.deleteSql(id, username);
        return ResponseEntity.ok(ApiResponse.success(null, "SQL definition deactivated successfully"));
    }
}
