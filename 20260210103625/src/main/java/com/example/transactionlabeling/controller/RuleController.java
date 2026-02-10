package com.example.transactionlabeling.controller;

import com.example.transactionlabeling.dto.ApiResponse;
import com.example.transactionlabeling.dto.RuleRequest;
import com.example.transactionlabeling.dto.RuleResponse;
import com.example.transactionlabeling.service.RuleManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing Drools rules
 */

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
@Tag(name = "Rule Management", description = "APIs for managing Drools rules")
public class RuleController {

    private static final Logger log = LoggerFactory.getLogger(RuleController.class);

    private final RuleManagementService ruleManagementService;

    /**
     * Create a new rule
     */
    @PostMapping
    @Operation(summary = "Create a new rule", description = "Create a new Drools rule with the provided content")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rule created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data or rule syntax")
    })
    public ResponseEntity<ApiResponse<RuleResponse>> createRule(@Valid @RequestBody RuleRequest request) {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Creating rule: {}, traceId: {}", request.getRuleName(), traceId);
        RuleResponse response = ruleManagementService.createRule(request);
        return ResponseEntity.ok(ApiResponse.success(response).withTraceId(traceId));
    }

    /**
     * Get rule by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get rule by ID", description = "Retrieve a rule by its database ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rule found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Rule not found")
    })
    public ResponseEntity<ApiResponse<RuleResponse>> getRuleById(
            @Parameter(description = "Rule ID") @PathVariable Long id) {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Fetching rule by ID: {}, traceId: {}", id, traceId);
        RuleResponse response = ruleManagementService.getRuleById(id);
        return ResponseEntity.ok(ApiResponse.success(response).withTraceId(traceId));
    }

    /**
     * Get rule by rule name
     */
    @GetMapping("/rule-name/{ruleName}")
    @Operation(summary = "Get rule by name", description = "Retrieve a rule by its rule name")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rule found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Rule not found")
    })
    public ResponseEntity<ApiResponse<RuleResponse>> getRuleByRuleName(
            @Parameter(description = "Rule name") @PathVariable String ruleName) {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Fetching rule by rule name: {}, traceId: {}", ruleName, traceId);
        RuleResponse response = ruleManagementService.getRuleByRuleName(ruleName);
        return ResponseEntity.ok(ApiResponse.success(response).withTraceId(traceId));
    }

    /**
     * Get all rules with pagination
     */
    @GetMapping
    @Operation(summary = "Get all rules", description = "Retrieve all rules with pagination")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rules retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<RuleResponse>>> getAllRules(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDir) {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Fetching rules - page: {}, size: {}, sortBy: {}, sortDir: {}, traceId: {}", page, size, sortBy, sortDir, traceId);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<RuleResponse> rules = ruleManagementService.getAllRules(pageable);

        ApiResponse.ApiResponseMeta meta = ApiResponse.ApiResponseMeta.builder()
                .pageNumber(page)
                .pageSize(size)
                .totalPages(rules.getTotalPages())
                .totalElements(rules.getTotalElements())
                .build();

        return ResponseEntity.ok(ApiResponse.success(rules.getContent()).withMeta(meta).withTraceId(traceId));
    }

    /**
     * Get active rules
     */
    @GetMapping("/active")
    @Operation(summary = "Get active rules", description = "Retrieve all active rules")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Active rules retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<RuleResponse>>> getActiveRules() {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Fetching active rules, traceId: {}", traceId);
        List<RuleResponse> rules = ruleManagementService.getActiveRules();
        return ResponseEntity.ok(ApiResponse.success(rules).withTraceId(traceId));
    }

    /**
     * Get rules by category
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get rules by category", description = "Retrieve all rules in a specific category")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rules retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<RuleResponse>>> getRulesByCategory(
            @Parameter(description = "Rule category") @PathVariable String category) {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Fetching rules for category: {}, traceId: {}", category, traceId);
        List<RuleResponse> rules = ruleManagementService.getRulesByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(rules).withTraceId(traceId));
    }

    /**
     * Update an existing rule
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update rule", description = "Update an existing rule")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rule updated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data or rule syntax"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Rule not found")
    })
    public ResponseEntity<ApiResponse<RuleResponse>> updateRule(
            @Parameter(description = "Rule ID") @PathVariable Long id,
            @Valid @RequestBody RuleRequest request) {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Updating rule: {}, traceId: {}", id, traceId);
        RuleResponse response = ruleManagementService.updateRule(id, request);
        return ResponseEntity.ok(ApiResponse.success(response).withTraceId(traceId));
    }

    /**
     * Delete rule by ID
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete rule", description = "Delete a rule by its database ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rule deleted successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Rule not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteRule(
            @Parameter(description = "Rule ID") @PathVariable Long id) {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Deleting rule: {}, traceId: {}", id, traceId);
        ruleManagementService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Rule deleted successfully").withTraceId(traceId));
    }

    /**
     * Activate rule
     */
    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate rule", description = "Activate a rule by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rule activated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Rule not found")
    })
    public ResponseEntity<ApiResponse<RuleResponse>> activateRule(
            @Parameter(description = "Rule ID") @PathVariable Long id) {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Activating rule: {}, traceId: {}", id, traceId);
        RuleResponse response = ruleManagementService.activateRule(id);
        return ResponseEntity.ok(ApiResponse.success(response).withTraceId(traceId));
    }

    /**
     * Deactivate rule
     */
    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate rule", description = "Deactivate a rule by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rule deactivated successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Rule not found")
    })
    public ResponseEntity<ApiResponse<RuleResponse>> deactivateRule(
            @Parameter(description = "Rule ID") @PathVariable Long id) {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Deactivating rule: {}, traceId: {}", id, traceId);
        RuleResponse response = ruleManagementService.deactivateRule(id);
        return ResponseEntity.ok(ApiResponse.success(response).withTraceId(traceId));
    }

    /**
     * Reload all rules in the engine
     */
    @PostMapping("/reload")
    @Operation(summary = "Reload rules", description = "Reload all rules in the Drools engine")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rules reloaded successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<Void>> reloadRules() {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Reloading all rules, traceId: {}", traceId);
        ruleManagementService.reloadAllRules();
        return ResponseEntity.ok(ApiResponse.<Void>success("Rules reloaded successfully").withTraceId(traceId));
    }

    /**
     * Get loaded rule names from the engine
     */
    @GetMapping("/loaded")
    @Operation(summary = "Get loaded rules", description = "Get the names of all loaded rules in the Drools engine")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Loaded rules retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<List<String>>> getLoadedRuleNames() {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Fetching loaded rule names, traceId: {}", traceId);
        List<String> ruleNames = ruleManagementService.getLoadedRuleNames();
        return ResponseEntity.ok(ApiResponse.success(ruleNames).withTraceId(traceId));
    }

    /**
     * Validate rule content
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate rule", description = "Validate rule content without saving")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Validation completed",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<Boolean>> validateRule(@RequestBody String ruleContent) {
        String traceId = MDC.get("traceId") != null ? MDC.get("traceId") : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Validating rule content, traceId: {}", traceId);
        boolean isValid = ruleManagementService.validateRule(ruleContent);
        String message = isValid ? "Rule content is valid" : "Rule content is invalid";
        return ResponseEntity.ok(ApiResponse.success(message, isValid).withTraceId(traceId));
    }
}
