package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.RuleDefinition;

/**
 * Unit tests for RuleDefinition domain entity.
 */
@DisplayName("RuleDefinition Domain Model Tests")
class RuleDefinitionTest {

    @Test
    @DisplayName("Should create rule definition with all fields")
    void testCreateRuleDefinitionWithAllFields() {
        // Arrange & Act
        RuleDefinition ruleDefinition = RuleDefinition.builder()
                .id(1L)
                .code("SMA")
                .name("Simple Moving Average")
                .requiresParam(true)
                .description("Calculates the simple moving average over a period")
                .build();

        // Assert
        assertNotNull(ruleDefinition);
        assertEquals(1L, ruleDefinition.getId());
        assertEquals("SMA", ruleDefinition.getCode());
        assertEquals("Simple Moving Average", ruleDefinition.getName());
        assertTrue(ruleDefinition.isRequiresParam());
        assertEquals("Calculates the simple moving average over a period", ruleDefinition.getDescription());
    }

    @Test
    @DisplayName("Should create rule definition that requires parameter")
    void testCreateRuleDefinitionRequiresParam() {
        // Arrange & Act
        RuleDefinition ruleDefinition = RuleDefinition.builder()
                .id(2L)
                .code("RSI")
                .name("Relative Strength Index")
                .requiresParam(true)
                .description("Momentum oscillator")
                .build();

        // Assert
        assertTrue(ruleDefinition.isRequiresParam());
    }

    @Test
    @DisplayName("Should create rule definition that does not require parameter")
    void testCreateRuleDefinitionNoParam() {
        // Arrange & Act
        RuleDefinition ruleDefinition = RuleDefinition.builder()
                .id(3L)
                .code("PRICE")
                .name("Current Price")
                .requiresParam(false)
                .description("Current market price")
                .build();

        // Assert
        assertFalse(ruleDefinition.isRequiresParam());
    }

    @Test
    @DisplayName("Should create rule definition for CONSTANT")
    void testCreateConstantRuleDefinition() {
        // Arrange & Act
        RuleDefinition ruleDefinition = RuleDefinition.builder()
                .id(4L)
                .code("CONSTANT")
                .name("Fixed Value")
                .requiresParam(true)
                .description("A constant numeric value")
                .build();

        // Assert
        assertEquals("CONSTANT", ruleDefinition.getCode());
        assertEquals("Fixed Value", ruleDefinition.getName());
        assertTrue(ruleDefinition.isRequiresParam());
    }

    @Test
    @DisplayName("Should create rule definition for VOLUME")
    void testCreateVolumeRuleDefinition() {
        // Arrange & Act
        RuleDefinition ruleDefinition = RuleDefinition.builder()
                .id(5L)
                .code("VOLUME")
                .name("Trading Volume")
                .requiresParam(false)
                .description("Current trading volume")
                .build();

        // Assert
        assertEquals("VOLUME", ruleDefinition.getCode());
        assertFalse(ruleDefinition.isRequiresParam());
    }

    @Test
    @DisplayName("Should have proper toString representation")
    void testToString() {
        // Arrange
        RuleDefinition ruleDefinition = RuleDefinition.builder()
                .id(6L)
                .code("MACD")
                .name("Moving Average Convergence Divergence")
                .requiresParam(true)
                .description("Trend-following momentum indicator")
                .build();

        // Act
        String toString = ruleDefinition.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("MACD"));
        assertTrue(toString.contains("Moving Average Convergence Divergence"));
    }

    @Test
    @DisplayName("Should create rule definition with null id")
    void testCreateRuleDefinitionWithNullId() {
        // Arrange & Act
        RuleDefinition ruleDefinition = RuleDefinition.builder()
                .id(null)
                .code("TEST")
                .name("Test Indicator")
                .requiresParam(false)
                .description("Test description")
                .build();

        // Assert
        assertNotNull(ruleDefinition);
        assertEquals(null, ruleDefinition.getId());
    }

    @Test
    @DisplayName("Should create multiple distinct rule definitions")
    void testCreateMultipleRuleDefinitions() {
        // Arrange & Act
        RuleDefinition sma = RuleDefinition.builder()
                .id(1L)
                .code("SMA")
                .name("Simple Moving Average")
                .requiresParam(true)
                .description("SMA description")
                .build();

        RuleDefinition ema = RuleDefinition.builder()
                .id(2L)
                .code("EMA")
                .name("Exponential Moving Average")
                .requiresParam(true)
                .description("EMA description")
                .build();

        RuleDefinition price = RuleDefinition.builder()
                .id(3L)
                .code("PRICE")
                .name("Current Price")
                .requiresParam(false)
                .description("Current market price")
                .build();

        // Assert
        assertNotNull(sma);
        assertNotNull(ema);
        assertNotNull(price);
        assertTrue(sma.isRequiresParam());
        assertTrue(ema.isRequiresParam());
        assertFalse(price.isRequiresParam());
    }
}
