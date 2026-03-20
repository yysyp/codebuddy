package com.etl.control.controller;

import com.etl.control.dto.RuleRequest;
import com.etl.control.dto.RuleResponse;
import com.etl.control.entity.RuleDefinition;
import com.etl.control.service.RuleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Rule Controller Test
 * Unit tests for RuleController
 */
@WebMvcTest(RuleController.class)
class RuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RuleService ruleService;

    private RuleRequest ruleRequest;
    private RuleResponse ruleResponse;

    @BeforeEach
    void setUp() {
        ruleRequest = RuleRequest.builder()
                .name("test-rule")
                .description("Test rule")
                .ruleContent("package com.etl.rules\nrule \"test\" when then end")
                .ruleType("TAGGING")
                .targetType("TRANSACTION")
                .priority("HIGH")
                .build();

        ruleResponse = RuleResponse.builder()
                .id(1L)
                .name("test-rule")
                .description("Test rule")
                .ruleContent("package com.etl.rules\nrule \"test\" when then end")
                .version(1)
                .status("DRAFT")
                .ruleType("TAGGING")
                .targetType("TRANSACTION")
                .priority("HIGH")
                .createdAt(Instant.now())
                .createdBy("testuser")
                .updatedAt(Instant.now())
                .updatedBy("testuser")
                .build();
    }

    @Test
    @DisplayName("Should create rule successfully")
    void createRule_Success() throws Exception {
        when(ruleService.createRule(any(RuleRequest.class), anyString())).thenReturn(ruleResponse);

        mockMvc.perform(post("/api/v1/rules")
                        .header("X-User", "testuser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ruleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("test-rule"));
    }

    @Test
    @DisplayName("Should get rule by ID successfully")
    void getRuleById_Success() throws Exception {
        when(ruleService.getRuleById(any(Long.class))).thenReturn(ruleResponse);

        mockMvc.perform(get("/api/v1/rules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("Should get all published rules successfully")
    void getAllPublishedRules_Success() throws Exception {
        when(ruleService.getAllPublishedRules()).thenReturn(List.of(ruleResponse));

        mockMvc.perform(get("/api/v1/rules/published"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Should get rules with pagination successfully")
    void getAllRules_Success() throws Exception {
        PageImpl<RuleResponse> page = new PageImpl<>(List.of(ruleResponse));
        when(ruleService.getAllRules(any(Integer.class), any(Integer.class), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/rules")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("Should publish rule successfully")
    void publishRule_Success() throws Exception {
        when(ruleService.publishRule(any(Long.class), anyString())).thenReturn(ruleResponse);

        mockMvc.perform(post("/api/v1/rules/1/publish")
                        .header("X-User", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
