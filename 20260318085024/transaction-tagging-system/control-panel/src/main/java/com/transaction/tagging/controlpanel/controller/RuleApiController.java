package com.transaction.tagging.controlpanel.controller;

import com.transaction.tagging.common.dto.ApiResponse;
import com.transaction.tagging.common.entity.RuleMetadata;
import com.transaction.tagging.controlpanel.service.RuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for internal APIs used by Data Panel.
 * These APIs are designed for inter-service communication.
 */
@Slf4j
@RestController
@RequestMapping("/v1/internal/rules")
@RequiredArgsConstructor
@Tag(name = "Internal Rule APIs", description = "Internal APIs for Data Panel to fetch rules")
public class RuleApiController {

    private final RuleService ruleService;

    @GetMapping("/published")
    @Operation(summary = "Get published rules for Data Panel", 
               description = "Retrieves all published rules in format suitable for Data Panel consumption")
    public ResponseEntity<ApiResponse<List<RuleMetadata>>> getPublishedRules() {
        log.info("Data Panel fetching published rules");
        List<RuleMetadata> rules = ruleService.getPublishedRulesForDataPanel();
        log.info("Returning {} published rules", rules.size());
        return ResponseEntity.ok(ApiResponse.success(rules));
    }
}
