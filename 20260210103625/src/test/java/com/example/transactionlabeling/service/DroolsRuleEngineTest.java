package com.example.transactionlabeling.service;

import com.example.transactionlabeling.entity.Rule;
import com.example.transactionlabeling.repository.RuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Test class for DroolsRuleEngine
 */
@ExtendWith(MockitoExtension.class)
class DroolsRuleEngineTest {

    @Mock
    private RuleRepository ruleRepository;

    @InjectMocks
    private DroolsRuleEngine droolsRuleEngine;

    private Rule sampleRule;

    @BeforeEach
    void setUp() {
        sampleRule = Rule.builder()
                .id(1L)
                .ruleName("Test Rule")
                .ruleCategory("TEST")
                .priority(100)
                .active(true)
                .description("Test rule description")
                .ruleContent("""
                        package com.example.transactionlabeling.rules

                        import com.example.transactionlabeling.entity.Transaction

                        rule "Test Rule"
                            when
                                $transaction : Transaction(amount > 1000)
                            then
                                $transaction.getLabels().add("HIGH_VALUE");
                        end
                        """)
                .build();
    }

    @Test
    void testValidateRuleContent() {
        boolean isValid = droolsRuleEngine.validateRuleContent(sampleRule.getRuleContent());
        assertThat(isValid).isTrue();
    }

    @Test
    void testValidateInvalidRuleContent() {
        String invalidRuleContent = "invalid rule content";
        boolean isValid = droolsRuleEngine.validateRuleContent(invalidRuleContent);
        assertThat(isValid).isFalse();
    }

    @Test
    void testGetLoadedRuleNames() {
        when(ruleRepository.findActiveRulesOrderedByPriority()).thenReturn(List.of(sampleRule));
        droolsRuleEngine.reloadRules();

        List<String> ruleNames = droolsRuleEngine.getLoadedRuleNames();
        assertThat(ruleNames).isNotNull();
    }
}
