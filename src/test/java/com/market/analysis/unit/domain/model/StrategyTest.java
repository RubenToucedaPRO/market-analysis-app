package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.Strategy;

/**
 * Unit tests for Strategy domain entity.
 */
@DisplayName("Strategy Domain Model Tests")
class StrategyTest {

    @Test
    @DisplayName("Should create strategy with valid data")
    void testCreateStrategyWithValidData() {
        // Arrange
        List<Rule> rules = List.of(
            Rule.builder()
                .id(1L)
                .name("SMA Crossover")
                .subjectCode("SMA")
                .subjectParam(20.0)
                .operator(">")
                .targetCode("SMA")
                .targetParam(50.0)
                
                .description("20-day SMA crossover")
                .build()
        );

        // Act
        Strategy strategy = Strategy.builder()
                .id(1L)
                .name("Conservative Growth")
                .description("A conservative strategy for long-term growth")
                .rules(rules)
                .build();

        // Assert
        assertNotNull(strategy);
        assertEquals(1L, strategy.getId());
        assertEquals("Conservative Growth", strategy.getName());
        assertEquals("A conservative strategy for long-term growth", strategy.getDescription());
        assertEquals(1, strategy.getRules().size());
    }

    @Test
    @DisplayName("Should return immutable copy of rules list")
    void testGetRulesReturnsImmutableCopy() {
        // Arrange
        List<Rule> rules = new ArrayList<>();
        rules.add(Rule.builder()
                .id(1L)
                .name("Rule 1")
                .subjectCode("PRICE")
                .subjectParam(null)
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                
                .description("Description")
                .build());

        Strategy strategy = Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Test Description")
                .rules(rules)
                .build();

        // Act
        List<Rule> retrievedRules = strategy.getRules();

        // Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            retrievedRules.add(Rule.builder()
                    .id(2L)
                    .name("Rule 2")
                    .subjectCode("PRICE")
                .subjectParam(null)
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                    
                    .description("Description")
                    .build());
        });
    }

    @Test
    @DisplayName("Should validate consistency and throw exception when name is null")
    void testValidateConsistencyThrowsExceptionWhenNameIsNull() {
        // Arrange
        Strategy strategy = Strategy.builder()
                .id(1L)
                .name(null)
                .description("Description")
                .rules(List.of())
                .build();

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            strategy::validateConsistency);
        assertTrue(exception.getMessage().contains("name"));
    }

    @Test
    @DisplayName("Should validate consistency and throw exception when name is empty")
    void testValidateConsistencyThrowsExceptionWhenNameIsEmpty() {
        // Arrange
        Strategy strategy = Strategy.builder()
                .id(1L)
                .name("   ")
                .description("Description")
                .rules(List.of())
                .build();

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            strategy::validateConsistency);
        assertTrue(exception.getMessage().contains("name"));
    }

    @Test
    @DisplayName("Should validate consistency and throw exception when description is null")
    void testValidateConsistencyThrowsExceptionWhenDescriptionIsNull() {
        // Arrange
        Strategy strategy = Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description(null)
                .rules(List.of())
                .build();

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            strategy::validateConsistency);
        assertTrue(exception.getMessage().contains("description"));
    }

    @Test
    @DisplayName("Should validate consistency and throw exception when rules list is empty")
    void testValidateConsistencyThrowsExceptionWhenRulesListIsEmpty() {
        // Arrange
        Strategy strategy = Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Description")
                .rules(List.of())
                .build();

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            strategy::validateConsistency);
        assertTrue(exception.getMessage().contains("at least one rule"));
    }

    @Test
    @DisplayName("Should validate consistency and throw exception when rules list contains null")
    void testValidateConsistencyThrowsExceptionWhenRulesContainNull() {
        // Arrange
        List<Rule> rules = new ArrayList<>();
        rules.add(null);

        Strategy strategy = Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Description")
                .rules(rules)
                .build();

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            strategy::validateConsistency);
        assertTrue(exception.getMessage().contains("null rules"));
    }

    @Test
    @DisplayName("Should pass validation with valid strategy")
    void testValidateConsistencyPassesWithValidStrategy() {
        // Arrange
        List<Rule> rules = List.of(
            Rule.builder()
                .id(1L)
                .name("Rule 1")
                .subjectCode("PRICE")
                .subjectParam(null)
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                
                .description("Description")
                .build()
        );

        Strategy strategy = Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Description")
                .rules(rules)
                .build();

        // Act & Assert - should not throw
        strategy.validateConsistency();
    }

    @Test
    @DisplayName("Should consider strategies equal if they have same ID")
    void testEqualsBasedOnId() {
        // Arrange
        List<Rule> rules = List.of(
            Rule.builder()
                .id(1L)
                .name("Rule")
                .subjectCode("PRICE")
                .subjectParam(null)
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                
                .description("Description")
                .build()
        );

        Strategy strategy1 = Strategy.builder()
                .id(1L)
                .name("Strategy A")
                .description("Description A")
                .rules(rules)
                .build();

        Strategy strategy2 = Strategy.builder()
                .id(1L)
                .name("Strategy B")
                .description("Description B")
                .rules(rules)
                .build();

        // Act & Assert
        assertEquals(strategy1, strategy2);
        assertEquals(strategy1.hashCode(), strategy2.hashCode());
    }

    @Test
    @DisplayName("Should consider strategies different if they have different IDs")
    void testNotEqualsWithDifferentIds() {
        // Arrange
        List<Rule> rules = List.of(
            Rule.builder()
                .id(1L)
                .name("Rule")
                .subjectCode("PRICE")
                .subjectParam(null)
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                
                .description("Description")
                .build()
        );

        Strategy strategy1 = Strategy.builder()
                .id(1L)
                .name("Strategy")
                .description("Description")
                .rules(rules)
                .build();

        Strategy strategy2 = Strategy.builder()
                .id(2L)
                .name("Strategy")
                .description("Description")
                .rules(rules)
                .build();

        // Act & Assert
        assertFalse(strategy1.equals(strategy2));
    }

    @Test
    @DisplayName("Should handle null rules list in builder")
    void testBuilderWithNullRulesList() {
        // Act
        Strategy strategy = Strategy.builder()
                .id(1L)
                .name("Test")
                .description("Test")
                .rules(null)
                .build();

        // Assert
        assertNotNull(strategy.getRules());
        assertTrue(strategy.getRules().isEmpty());
    }
}
