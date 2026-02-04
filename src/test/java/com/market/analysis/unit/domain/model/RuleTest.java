package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.RuleResult;
import com.market.analysis.domain.model.TickerData;

/**
 * Unit tests for Rule domain entity.
 */
@DisplayName("Rule Domain Model Tests")
class RuleTest {

    @Test
    @DisplayName("Should create rule with valid data")
    void testCreateRuleWithValidData() {
        // Arrange & Act
        Rule rule = Rule.builder()
                .id(1L)
                .name("SMA Crossover")
                .ruleType("MOVING_AVERAGE")
                .parameters(Map.of("period", 20))
                .description("20-day SMA crossover rule")
                .build();

        // Assert
        assertNotNull(rule);
        assertEquals(1L, rule.getId());
        assertEquals("SMA Crossover", rule.getName());
        assertEquals("MOVING_AVERAGE", rule.getRuleType());
        assertNotNull(rule.getParameters());
        assertEquals("20-day SMA crossover rule", rule.getDescription());
    }

    @Test
    @DisplayName("Should evaluate rule with valid ticker data")
    void testEvaluateRuleWithValidTickerData() {
        // Arrange
        Rule rule = Rule.builder()
                .id(1L)
                .name("Price Check")
                .ruleType("PRICE")
                .parameters(Map.of("threshold", 100))
                .description("Price above threshold")
                .build();

        TickerData tickerData = TickerData.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.0))
                .volume(1000000L)
                .timestamp(LocalDateTime.now())
                .indicators(new HashMap<>())
                .historicalData(new HashMap<>())
                .build();

        // Act
        RuleResult result = rule.evaluate(tickerData);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getJustification());
        assertNotNull(result.getRule());
        assertEquals(rule, result.getRule());
    }

    @Test
    @DisplayName("Should throw exception when evaluating with null ticker data")
    void testEvaluateThrowsExceptionWithNullTickerData() {
        // Arrange
        Rule rule = Rule.builder()
                .id(1L)
                .name("Test Rule")
                .ruleType("TYPE")
                .parameters(Map.of())
                .description("Description")
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> rule.evaluate(null));
    }

    @Test
    @DisplayName("Should throw exception when evaluating rule with null name")
    void testEvaluateThrowsExceptionWithNullName() {
        // Arrange
        Rule rule = Rule.builder()
                .id(1L)
                .name(null)
                .ruleType("TYPE")
                .parameters(Map.of())
                .description("Description")
                .build();

        TickerData tickerData = TickerData.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.0))
                .volume(1000000L)
                .timestamp(LocalDateTime.now())
                .indicators(new HashMap<>())
                .historicalData(new HashMap<>())
                .build();

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> rule.evaluate(tickerData));
        assertTrue(exception.getMessage().contains("name"));
    }

    @Test
    @DisplayName("Should throw exception when evaluating rule with empty name")
    void testEvaluateThrowsExceptionWithEmptyName() {
        // Arrange
        Rule rule = Rule.builder()
                .id(1L)
                .name("   ")
                .ruleType("TYPE")
                .parameters(Map.of())
                .description("Description")
                .build();

        TickerData tickerData = TickerData.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.0))
                .volume(1000000L)
                .timestamp(LocalDateTime.now())
                .indicators(new HashMap<>())
                .historicalData(new HashMap<>())
                .build();

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> rule.evaluate(tickerData));
        assertTrue(exception.getMessage().contains("name"));
    }

    @Test
    @DisplayName("Should throw exception when evaluating rule with null rule type")
    void testEvaluateThrowsExceptionWithNullRuleType() {
        // Arrange
        Rule rule = Rule.builder()
                .id(1L)
                .name("Test Rule")
                .ruleType(null)
                .parameters(Map.of())
                .description("Description")
                .build();

        TickerData tickerData = TickerData.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.0))
                .volume(1000000L)
                .timestamp(LocalDateTime.now())
                .indicators(new HashMap<>())
                .historicalData(new HashMap<>())
                .build();

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> rule.evaluate(tickerData));
        assertTrue(exception.getMessage().contains("type"));
    }

    @Test
    @DisplayName("Should throw exception when evaluating rule with null parameters")
    void testEvaluateThrowsExceptionWithNullParameters() {
        // Arrange
        Rule rule = Rule.builder()
                .id(1L)
                .name("Test Rule")
                .ruleType("TYPE")
                .parameters(null)
                .description("Description")
                .build();

        TickerData tickerData = TickerData.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.0))
                .volume(1000000L)
                .timestamp(LocalDateTime.now())
                .indicators(new HashMap<>())
                .historicalData(new HashMap<>())
                .build();

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> rule.evaluate(tickerData));
        assertTrue(exception.getMessage().contains("parameters"));
    }

    @Test
    @DisplayName("Should consider rules equal if they have same ID")
    void testEqualsBasedOnId() {
        // Arrange
        Rule rule1 = Rule.builder()
                .id(1L)
                .name("Rule A")
                .ruleType("TYPE_A")
                .parameters(Map.of())
                .description("Description A")
                .build();

        Rule rule2 = Rule.builder()
                .id(1L)
                .name("Rule B")
                .ruleType("TYPE_B")
                .parameters(Map.of())
                .description("Description B")
                .build();

        // Act & Assert
        assertEquals(rule1, rule2);
        assertEquals(rule1.hashCode(), rule2.hashCode());
    }

    @Test
    @DisplayName("Should consider rules different if they have different IDs")
    void testNotEqualsWithDifferentIds() {
        // Arrange
        Rule rule1 = Rule.builder()
                .id(1L)
                .name("Rule")
                .ruleType("TYPE")
                .parameters(Map.of())
                .description("Description")
                .build();

        Rule rule2 = Rule.builder()
                .id(2L)
                .name("Rule")
                .ruleType("TYPE")
                .parameters(Map.of())
                .description("Description")
                .build();

        // Act & Assert
        assertFalse(rule1.equals(rule2));
    }

    @Test
    @DisplayName("Should generate justification in result")
    void testEvaluateGeneratesJustification() {
        // Arrange
        Rule rule = Rule.builder()
                .id(1L)
                .name("Test Rule")
                .ruleType("TYPE")
                .parameters(Map.of())
                .description("Description")
                .build();

        TickerData tickerData = TickerData.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.0))
                .volume(1000000L)
                .timestamp(LocalDateTime.now())
                .indicators(new HashMap<>())
                .historicalData(new HashMap<>())
                .build();

        // Act
        RuleResult result = rule.evaluate(tickerData);

        // Assert
        assertNotNull(result.getJustification());
        assertTrue(result.getJustification().contains("Test Rule"));
        assertTrue(result.getJustification().contains("AAPL"));
    }
}
