package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.RuleResult;

/**
 * Unit tests for RuleResult domain entity.
 */
@DisplayName("RuleResult Domain Model Tests")
class RuleResultTest {

    @Test
    @DisplayName("Should create rule result with passed status")
    void testCreateRuleResultPassed() {
        // Arrange
        Rule rule = Rule.builder()
                .id(1L)
                .name("Test Rule")
                .subjectCode("PRICE")
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .description("Test description")
                .build();

        // Act
        RuleResult result = RuleResult.builder()
                .passed(true)
                .justification("Price is above 100")
                .rule(rule)
                .build();

        // Assert
        assertNotNull(result);
        assertTrue(result.isPassed());
        assertEquals("Price is above 100", result.getJustification());
        assertEquals(rule, result.getRule());
    }

    @Test
    @DisplayName("Should create rule result with failed status")
    void testCreateRuleResultFailed() {
        // Arrange
        Rule rule = Rule.builder()
                .id(2L)
                .name("RSI Rule")
                .subjectCode("RSI")
                .subjectParam(14.0)
                .operator("<")
                .targetCode("CONSTANT")
                .targetParam(30.0)
                .description("RSI oversold check")
                .build();

        // Act
        RuleResult result = RuleResult.builder()
                .passed(false)
                .justification("RSI is 45.2, not below 30")
                .rule(rule)
                .build();

        // Assert
        assertNotNull(result);
        assertFalse(result.isPassed());
        assertEquals("RSI is 45.2, not below 30", result.getJustification());
        assertEquals(rule, result.getRule());
    }

    @Test
    @DisplayName("Should create rule result with detailed justification")
    void testCreateRuleResultWithDetailedJustification() {
        // Arrange
        Rule rule = Rule.builder()
                .id(3L)
                .name("SMA Crossover")
                .subjectCode("SMA")
                .subjectParam(50.0)
                .operator(">")
                .targetCode("SMA")
                .targetParam(200.0)
                .description("Golden cross")
                .build();

        // Act
        RuleResult result = RuleResult.builder()
                .passed(true)
                .justification("SMA(50)=152.3 > SMA(200)=148.7, bullish signal detected")
                .rule(rule)
                .build();

        // Assert
        assertTrue(result.isPassed());
        assertTrue(result.getJustification().contains("SMA(50)"));
        assertTrue(result.getJustification().contains("SMA(200)"));
    }

    @Test
    @DisplayName("Should have proper toString representation")
    void testToString() {
        // Arrange
        Rule rule = Rule.builder()
                .id(4L)
                .name("Volume Check")
                .subjectCode("VOLUME")
                .operator(">")
                .targetCode("AVG_VOLUME")
                .targetParam(20.0)
                .description("High volume check")
                .build();

        RuleResult result = RuleResult.builder()
                .passed(true)
                .justification("Volume exceeds 20-day average")
                .rule(rule)
                .build();

        // Act
        String toString = result.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("passed=true") || toString.contains("true"));
        assertTrue(toString.contains("Volume exceeds 20-day average"));
    }

    @Test
    @DisplayName("Should get rule reference from result")
    void testGetRuleReference() {
        // Arrange
        Rule rule = Rule.builder()
                .id(5L)
                .name("Price Above SMA")
                .subjectCode("PRICE")
                .operator(">")
                .targetCode("SMA")
                .targetParam(200.0)
                .description("Price above 200-day SMA")
                .build();

        RuleResult result = RuleResult.builder()
                .passed(true)
                .justification("Price is above 200-day SMA")
                .rule(rule)
                .build();

        // Act
        Rule retrievedRule = result.getRule();

        // Assert
        assertNotNull(retrievedRule);
        assertEquals(5L, retrievedRule.getId());
        assertEquals("Price Above SMA", retrievedRule.getName());
    }

    @Test
    @DisplayName("Should create rule result for each evaluation scenario")
    void testMultipleEvaluationScenarios() {
        // Arrange
        Rule rule1 = Rule.builder()
                .id(1L)
                .name("Rule 1")
                .subjectCode("PRICE")
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .build();

        Rule rule2 = Rule.builder()
                .id(2L)
                .name("Rule 2")
                .subjectCode("RSI")
                .subjectParam(14.0)
                .operator("<")
                .targetCode("CONSTANT")
                .targetParam(70.0)
                .build();

        // Act
        RuleResult result1 = RuleResult.builder()
                .passed(true)
                .justification("Condition met")
                .rule(rule1)
                .build();

        RuleResult result2 = RuleResult.builder()
                .passed(false)
                .justification("Condition not met")
                .rule(rule2)
                .build();

        // Assert
        assertTrue(result1.isPassed());
        assertFalse(result2.isPassed());
        assertNotNull(result1.getRule());
        assertNotNull(result2.getRule());
    }
}
