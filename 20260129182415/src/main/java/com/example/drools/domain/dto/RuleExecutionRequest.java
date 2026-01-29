package com.example.drools.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for rule execution
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleExecutionRequest {

    @NotBlank(message = "Rule name is required")
    @JsonProperty("ruleName")
    private String ruleName;

    @NotNull(message = "Fact data is required")
    @JsonProperty("factData")
    private Map<String, Object> factData;
}
