package com.etl.control.service;

import com.etl.control.dto.RuleRequest;
import com.etl.control.dto.RuleResponse;
import com.etl.control.entity.RuleDefinition;
import com.etl.control.entity.RuleDefinition.RuleStatus;
import com.etl.control.exception.BusinessException;
import com.etl.control.repository.RuleDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Rule Service Test
 * Unit tests for RuleService
 */
@ExtendWith(MockitoExtension.class)
class RuleServiceTest {

    @Mock
    private RuleDefinitionRepository ruleRepository;

    @Mock
    private DroolsService droolsService;

    @InjectMocks
    private RuleService ruleService;

    private RuleRequest ruleRequest;
    private RuleDefinition ruleDefinition;

    @BeforeEach
    void setUp() {
        ruleRequest = RuleRequest.builder()
                .name("test-rule")
                .description("Test rule description")
                .ruleContent("package com.etl.rules\nrule \"test\" when then end")
                .ruleType("TAGGING")
                .targetType("TRANSACTION")
                .priority("HIGH")
                .tags("test")
                .build();

        ruleDefinition = RuleDefinition.builder()
                .id(1L)
                .name("test-rule")
                .description("Test rule description")
                .ruleContent("package com.etl.rules\nrule \"test\" when then end")
                .version(1)
                .status(RuleStatus.DRAFT)
                .ruleType("TAGGING")
                .targetType("TRANSACTION")
                .priority("HIGH")
                .tags("test")
                .createdBy("testuser")
                .updatedBy("testuser")
                .build();
    }

    @Test
    @DisplayName("Should create rule successfully")
    void createRule_Success() {
        // Given
        when(ruleRepository.existsByName(anyString())).thenReturn(false);
        when(ruleRepository.save(any(RuleDefinition.class))).thenReturn(ruleDefinition);
        doNothing().when(droolsService).validateRule(anyString());

        // When
        RuleResponse response = ruleService.createRule(ruleRequest, "testuser");

        // Then
        assertNotNull(response);
        assertEquals("test-rule", response.getName());
        assertEquals("Test rule description", response.getDescription());
        assertEquals(RuleStatus.DRAFT.name(), response.getStatus());
        verify(ruleRepository).save(any(RuleDefinition.class));
    }

    @Test
    @DisplayName("Should throw exception when rule name already exists")
    void createRule_NameExists() {
        // Given
        when(ruleRepository.existsByName(anyString())).thenReturn(true);

        // When & Then
        assertThrows(BusinessException.class, () -> 
            ruleService.createRule(ruleRequest, "testuser"));
        verify(ruleRepository, never()).save(any(RuleDefinition.class));
    }

    @Test
    @DisplayName("Should update rule successfully")
    void updateRule_Success() {
        // Given
        when(ruleRepository.findById(anyLong())).thenReturn(Optional.of(ruleDefinition));
        when(ruleRepository.save(any(RuleDefinition.class))).thenReturn(ruleDefinition);
        doNothing().when(droolsService).validateRule(anyString());

        // When
        RuleResponse response = ruleService.updateRule(1L, ruleRequest, "testuser");

        // Then
        assertNotNull(response);
        verify(ruleRepository).save(any(RuleDefinition.class));
    }

    @Test
    @DisplayName("Should throw exception when rule not found for update")
    void updateRule_NotFound() {
        // Given
        when(ruleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> 
            ruleService.updateRule(1L, ruleRequest, "testuser"));
        verify(ruleRepository, never()).save(any(RuleDefinition.class));
    }

    @Test
    @DisplayName("Should publish rule successfully")
    void publishRule_Success() {
        // Given
        when(ruleRepository.findById(anyLong())).thenReturn(Optional.of(ruleDefinition));
        when(ruleRepository.save(any(RuleDefinition.class))).thenReturn(ruleDefinition);
        doNothing().when(droolsService).validateRule(anyString());

        // When
        RuleResponse response = ruleService.publishRule(1L, "testuser");

        // Then
        assertNotNull(response);
        assertEquals(RuleStatus.PUBLISHED.name(), response.getStatus());
        verify(ruleRepository).save(any(RuleDefinition.class));
    }

    @Test
    @DisplayName("Should get rule by ID successfully")
    void getRuleById_Success() {
        // Given
        when(ruleRepository.findById(anyLong())).thenReturn(Optional.of(ruleDefinition));

        // When
        RuleResponse response = ruleService.getRuleById(1L);

        // Then
        assertNotNull(response);
        assertEquals("test-rule", response.getName());
    }

    @Test
    @DisplayName("Should throw exception when rule not found by ID")
    void getRuleById_NotFound() {
        // Given
        when(ruleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> ruleService.getRuleById(1L));
    }

    @Test
    @DisplayName("Should get all published rules successfully")
    void getAllPublishedRules_Success() {
        // Given
        when(ruleRepository.findAllPublishedRules()).thenReturn(List.of(ruleDefinition));

        // When
        List<RuleResponse> responses = ruleService.getAllPublishedRules();

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("test-rule", responses.get(0).getName());
    }

    @Test
    @DisplayName("Should archive rule successfully")
    void deleteRule_Success() {
        // Given
        when(ruleRepository.findById(anyLong())).thenReturn(Optional.of(ruleDefinition));
        when(ruleRepository.save(any(RuleDefinition.class))).thenReturn(ruleDefinition);

        // When
        ruleService.deleteRule(1L, "testuser");

        // Then
        verify(ruleRepository).save(any(RuleDefinition.class));
    }
}
