package com.example.drools.controller;

import com.example.drools.domain.dto.RuleExecutionRequest;
import com.example.drools.domain.dto.RuleExecutionResponse;
import com.example.drools.service.RuleEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for rule engine operations
 */
@Slf4j
@RestController
@RequestMapping("/v1/rules")
@RequiredArgsConstructor
@Tag(name = "Rule Engine", description = "Dynamic rule engine using Drools")
public class RuleEngineController {

    private final RuleEngineService ruleEngineService;

    /**
     * Execute a rule based on rule name and fact data
     */
    @PostMapping(value = "/execute", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Execute a rule",
            description = "Execute a specific rule with provided fact data"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule executed successfully",
                    content = @Content(schema = @Schema(implementation = RuleExecutionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<com.example.drools.common.response.ApiResponse<RuleExecutionResponse>> executeRule(
            @Parameter(description = "Rule execution request", required = true)
            @Valid @RequestBody RuleExecutionRequest request) {

        log.info("Executing rule: {}", request.getRuleName());
        RuleExecutionResponse response = ruleEngineService.executeRule(request);
        return ResponseEntity.ok(
                com.example.drools.common.response.ApiResponse.success(response, "Rule executed successfully")
        );
    }

    /**
     * Get all available rules
     */
    @GetMapping(value = "/available", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get available rules",
            description = "Retrieve all available rule sets and their descriptions"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Available rules retrieved successfully")
    })
    public ResponseEntity<com.example.drools.common.response.ApiResponse<Map<String, String>>> getAvailableRules() {
        log.info("Retrieving available rules");
        Map<String, String> rules = ruleEngineService.getAvailableRules();
        return ResponseEntity.ok(
                com.example.drools.common.response.ApiResponse.success(rules, "Available rules retrieved successfully")
        );
    }
}
