package com.transaction.tagging.controlpanel.controller;

import com.transaction.tagging.common.dto.ApiResponse;
import com.transaction.tagging.common.util.TraceContext;
import com.transaction.tagging.controlpanel.dto.CreateRuleRequest;
import com.transaction.tagging.controlpanel.dto.RuleResponse;
import com.transaction.tagging.controlpanel.dto.UpdateRuleRequest;
import com.transaction.tagging.controlpanel.entity.RuleEntity;
import com.transaction.tagging.controlpanel.service.RuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for rule management operations.
 */
@Slf4j
@RestController
@RequestMapping("/v1/rules")
@RequiredArgsConstructor
@Tag(name = "Rule Management", description = "APIs for managing transaction tagging rules")
public class RuleController {

    private final RuleService ruleService;

    @PostMapping
    @Operation(summary = "Create a new rule", description = "Creates a new rule in DRAFT status")
    public ResponseEntity<ApiResponse<RuleResponse>> createRule(
            @Valid @RequestBody CreateRuleRequest request) {
        log.info("Creating rule: {}", request.getRuleId());
        RuleResponse response = ruleService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Rule created successfully"));
    }

    @GetMapping("/{ruleId}")
    @Operation(summary = "Get rule by ID", description = "Retrieves a rule by its unique identifier")
    public ResponseEntity<ApiResponse<RuleResponse>> getRule(
            @Parameter(description = "Rule ID") @PathVariable String ruleId) {
        log.info("Getting rule: {}", ruleId);
        RuleResponse response = ruleService.getRule(ruleId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{ruleId}")
    @Operation(summary = "Update a rule", description = "Updates an existing DRAFT rule")
    public ResponseEntity<ApiResponse<RuleResponse>> updateRule(
            @Parameter(description = "Rule ID") @PathVariable String ruleId,
            @Valid @RequestBody UpdateRuleRequest request) {
        log.info("Updating rule: {}", ruleId);
        RuleResponse response = ruleService.updateRule(ruleId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Rule updated successfully"));
    }

    @DeleteMapping("/{ruleId}")
    @Operation(summary = "Delete a rule", description = "Deletes a DRAFT or ARCHIVED rule")
    public ResponseEntity<ApiResponse<Void>> deleteRule(
            @Parameter(description = "Rule ID") @PathVariable String ruleId) {
        log.info("Deleting rule: {}", ruleId);
        ruleService.deleteRule(ruleId);
        return ResponseEntity.ok(ApiResponse.success(null, "Rule deleted successfully"));
    }

    @PostMapping("/{ruleId}/publish")
    @Operation(summary = "Publish a rule", description = "Publishes a DRAFT rule, making it active for data processing")
    public ResponseEntity<ApiResponse<RuleResponse>> publishRule(
            @Parameter(description = "Rule ID") @PathVariable String ruleId,
            @Parameter(description = "User publishing the rule") @RequestParam(required = false) String publishedBy) {
        log.info("Publishing rule: {}", ruleId);
        RuleResponse response = ruleService.publishRule(ruleId, publishedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "Rule published successfully"));
    }

    @PostMapping("/{ruleId}/deprecate")
    @Operation(summary = "Deprecate a rule", description = "Deprecates a PUBLISHED rule")
    public ResponseEntity<ApiResponse<RuleResponse>> deprecateRule(
            @Parameter(description = "Rule ID") @PathVariable String ruleId) {
        log.info("Deprecating rule: {}", ruleId);
        RuleResponse response = ruleService.deprecateRule(ruleId);
        return ResponseEntity.ok(ApiResponse.success(response, "Rule deprecated successfully"));
    }

    @PostMapping("/{ruleId}/archive")
    @Operation(summary = "Archive a rule", description = "Archives a rule")
    public ResponseEntity<ApiResponse<RuleResponse>> archiveRule(
            @Parameter(description = "Rule ID") @PathVariable String ruleId) {
        log.info("Archiving rule: {}", ruleId);
        RuleResponse response = ruleService.archiveRule(ruleId);
        return ResponseEntity.ok(ApiResponse.success(response, "Rule archived successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all rules", description = "Retrieves all rules with pagination")
    public ResponseEntity<ApiResponse<Page<RuleResponse>>> getAllRules(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDir) {
        log.info("Getting all rules, page: {}, size: {}", page, size);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<RuleResponse> response = ruleService.getAllRules(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, 
                ApiResponse.PageMeta.builder()
                        .pageNumber(response.getNumber())
                        .pageSize(response.getSize())
                        .totalPages(response.getTotalPages())
                        .totalElements(response.getTotalElements())
                        .build()));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get rules by status", description = "Retrieves rules filtered by status")
    public ResponseEntity<ApiResponse<Page<RuleResponse>>> getRulesByStatus(
            @Parameter(description = "Rule status") @PathVariable RuleEntity.RuleStatus status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.info("Getting rules by status: {}", status);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("priority").descending());
        Page<RuleResponse> response = ruleService.getRulesByStatus(status, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(response,
                ApiResponse.PageMeta.builder()
                        .pageNumber(response.getNumber())
                        .pageSize(response.getSize())
                        .totalPages(response.getTotalPages())
                        .totalElements(response.getTotalElements())
                        .build()));
    }

    @GetMapping("/group/{ruleGroup}")
    @Operation(summary = "Get rules by group", description = "Retrieves all rules in a specific group")
    public ResponseEntity<ApiResponse<List<RuleResponse>>> getRulesByGroup(
            @Parameter(description = "Rule group") @PathVariable String ruleGroup) {
        log.info("Getting rules by group: {}", ruleGroup);
        List<RuleResponse> response = ruleService.getRulesByGroup(ruleGroup);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @Operation(summary = "Search rules", description = "Searches rules by name")
    public ResponseEntity<ApiResponse<Page<RuleResponse>>> searchRules(
            @Parameter(description = "Search term") @RequestParam String name,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        log.info("Searching rules with name: {}", name);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<RuleResponse> response = ruleService.searchRules(name, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(response,
                ApiResponse.PageMeta.builder()
                        .pageNumber(response.getNumber())
                        .pageSize(response.getSize())
                        .totalPages(response.getTotalPages())
                        .totalElements(response.getTotalElements())
                        .build()));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active rules", description = "Retrieves all active (published and enabled) rules")
    public ResponseEntity<ApiResponse<List<RuleResponse>>> getActiveRules() {
        log.info("Getting active rules");
        List<RuleResponse> response = ruleService.getActiveRules();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate rule content", description = "Validates Drools rule syntax")
    public ResponseEntity<ApiResponse<Boolean>> validateRule(
            @RequestBody String ruleContent) {
        log.info("Validating rule content");
        boolean valid = ruleService.validateRuleContent(ruleContent);
        return ResponseEntity.ok(ApiResponse.success(valid, 
                valid ? "Rule is valid" : "Rule validation failed"));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get rule statistics", description = "Retrieves statistics about rules")
    public ResponseEntity<ApiResponse<RuleService.RuleStatistics>> getStatistics() {
        log.info("Getting rule statistics");
        RuleService.RuleStatistics stats = ruleService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
