package com.etl.control.controller;

import com.etl.control.dto.ApiResponse;
import com.etl.control.dto.RuleRequest;
import com.etl.control.dto.RuleResponse;
import com.etl.control.entity.RuleDefinition.RuleStatus;
import com.etl.control.service.RuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Rule Controller
 * REST API endpoints for rule management
 */
@Tag(name = "Rule Management", description = "APIs for managing Drools rules")
@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    @Operation(summary = "Create a new rule", description = "Creates a new rule definition in DRAFT status")
    @PostMapping
    public ResponseEntity<ApiResponse<RuleResponse>> createRule(
            @Valid @RequestBody RuleRequest request,
            @RequestHeader(value = "X-User", defaultValue = "system") String username) {
        RuleResponse response = ruleService.createRule(request, username);
        return ResponseEntity.ok(ApiResponse.success(response, "Rule created successfully"));
    }

    @Operation(summary = "Update a rule", description = "Updates an existing rule and increments version")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RuleResponse>> updateRule(
            @Parameter(description = "Rule ID") @PathVariable Long id,
            @Valid @RequestBody RuleRequest request,
            @RequestHeader(value = "X-User", defaultValue = "system") String username) {
        RuleResponse response = ruleService.updateRule(id, request, username);
        return ResponseEntity.ok(ApiResponse.success(response, "Rule updated successfully"));
    }

    @Operation(summary = "Publish a rule", description = "Changes rule status to PUBLISHED")
    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<RuleResponse>> publishRule(
            @Parameter(description = "Rule ID") @PathVariable Long id,
            @RequestHeader(value = "X-User", defaultValue = "system") String username) {
        RuleResponse response = ruleService.publishRule(id, username);
        return ResponseEntity.ok(ApiResponse.success(response, "Rule published successfully"));
    }

    @Operation(summary = "Deprecate a rule", description = "Changes rule status to DEPRECATED")
    @PostMapping("/{id}/deprecate")
    public ResponseEntity<ApiResponse<RuleResponse>> deprecateRule(
            @Parameter(description = "Rule ID") @PathVariable Long id,
            @RequestHeader(value = "X-User", defaultValue = "system") String username) {
        RuleResponse response = ruleService.deprecateRule(id, username);
        return ResponseEntity.ok(ApiResponse.success(response, "Rule deprecated successfully"));
    }

    @Operation(summary = "Get rule by ID", description = "Retrieves a rule definition by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RuleResponse>> getRuleById(
            @Parameter(description = "Rule ID") @PathVariable Long id) {
        RuleResponse response = ruleService.getRuleById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get rule by name", description = "Retrieves a rule definition by name")
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<RuleResponse>> getRuleByName(
            @Parameter(description = "Rule name") @PathVariable String name) {
        RuleResponse response = ruleService.getRuleByName(name);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get all rules", description = "Retrieves all rules with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RuleResponse>>> getAllRules(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        Page<RuleResponse> response = ruleService.getAllRules(page, size, sortBy, sortDir);
        ApiResponse.MetaData meta = ApiResponse.MetaData.builder()
                .pageNumber(page)
                .pageSize(size)
                .totalPages(response.getTotalPages())
                .totalElements(response.getTotalElements())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response, meta));
    }

    @Operation(summary = "Get rules by status", description = "Retrieves rules filtered by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<RuleResponse>>> getRulesByStatus(
            @Parameter(description = "Rule status") @PathVariable RuleStatus status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Page<RuleResponse> response = ruleService.getRulesByStatus(status, page, size);
        ApiResponse.MetaData meta = ApiResponse.MetaData.builder()
                .pageNumber(page)
                .pageSize(size)
                .totalPages(response.getTotalPages())
                .totalElements(response.getTotalElements())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response, meta));
    }

    @Operation(summary = "Get all published rules", description = "Retrieves all published rules")
    @GetMapping("/published")
    public ResponseEntity<ApiResponse<List<RuleResponse>>> getAllPublishedRules() {
        List<RuleResponse> response = ruleService.getAllPublishedRules();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Delete a rule", description = "Archives a rule (sets status to ARCHIVED)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRule(
            @Parameter(description = "Rule ID") @PathVariable Long id,
            @RequestHeader(value = "X-User", defaultValue = "system") String username) {
        ruleService.deleteRule(id, username);
        return ResponseEntity.ok(ApiResponse.success(null, "Rule archived successfully"));
    }
}
