package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.Rule;

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
                                .subjectCode("SMA")
                                .subjectParam(50.0)
                                .operator(">")
                                .targetCode("SMA")
                                .targetParam(200.0)
                                .description("SMA 50 crosses above SMA 200")
                                .build();

                // Assert
                assertNotNull(rule);
                assertEquals(1L, rule.getId());
                assertEquals("SMA Crossover", rule.getName());
                assertEquals("SMA", rule.getSubjectCode());
                assertEquals(50.0, rule.getSubjectParam());
                assertEquals(">", rule.getOperator());
                assertEquals("SMA", rule.getTargetCode());
                assertEquals(200.0, rule.getTargetParam());
                assertEquals("SMA 50 crosses above SMA 200", rule.getDescription());
        }

        @Test
        @DisplayName("Should create rule with subject parameter but no target parameter")
        void testCreateRuleWithSubjectParamOnly() {
                // Arrange & Act
                Rule rule = Rule.builder()
                                .id(2L)
                                .name("RSI Overbought")
                                .subjectCode("RSI")
                                .subjectParam(14.0)
                                .operator(">")
                                .targetCode("CONSTANT")
                                .targetParam(70.0)
                                .description("RSI 14 above 70")
                                .build();

                // Assert
                assertNotNull(rule);
                assertEquals(14.0, rule.getSubjectParam());
                assertEquals(70.0, rule.getTargetParam());
        }

        @Test
        @DisplayName("Should create rule with null parameters when not required")
        void testCreateRuleWithNullParams() {
                // Arrange & Act
                Rule rule = Rule.builder()
                                .id(3L)
                                .name("Price vs Fixed Value")
                                .subjectCode("PRICE")
                                .subjectParam(null)
                                .operator(">")
                                .targetCode("CONSTANT")
                                .targetParam(100.0)
                                .description("Current price above 100")
                                .build();

                // Assert
                assertNotNull(rule);
                assertNull(rule.getSubjectParam());
                assertEquals(100.0, rule.getTargetParam());
        }

        @Test
        @DisplayName("Should create rule with comparison between price and SMA")
        void testCreatePriceVsSmaRule() {
                // Arrange & Act
                Rule rule = Rule.builder()
                                .id(4L)
                                .name("Price Above SMA")
                                .subjectCode("PRICE")
                                .subjectParam(null)
                                .operator(">")
                                .targetCode("SMA")
                                .targetParam(200.0)
                                .description("Price above 200-day SMA")
                                .build();

                // Assert
                assertNotNull(rule);
                assertEquals("PRICE", rule.getSubjectCode());
                assertNull(rule.getSubjectParam());
                assertEquals("SMA", rule.getTargetCode());
                assertEquals(200.0, rule.getTargetParam());
        }

        @Test
        @DisplayName("Should create rule with different operators")
        void testCreateRuleWithDifferentOperators() {
                // Arrange & Act
                Rule rule1 = Rule.builder()
                                .id(5L)
                                .name("Volume Below Average")
                                .subjectCode("VOLUME")
                                .subjectParam(null)
                                .operator("<")
                                .targetCode("AVG_VOLUME")
                                .targetParam(20.0)
                                .description("Volume below 20-day average")
                                .build();

                Rule rule2 = Rule.builder()
                                .id(6L)
                                .name("Price Crosses Above")
                                .subjectCode("PRICE")
                                .subjectParam(null)
                                .operator("crosses_above")
                                .targetCode("SMA")
                                .targetParam(50.0)
                                .description("Price crosses above 50-day SMA")
                                .build();

                // Assert
                assertEquals("<", rule1.getOperator());
                assertEquals("crosses_above", rule2.getOperator());
        }

        @Test
        @DisplayName("Should consider rules equal if they have same ID")
        void testEqualsBasedOnId() {
                // Arrange
                Rule rule1 = Rule.builder()
                                .id(1L)
                                .name("Rule A")
                                .subjectCode("SMA")
                                .subjectParam(50.0)
                                .operator(">")
                                .targetCode("SMA")
                                .targetParam(200.0)
                                .description("Description A")
                                .build();

                Rule rule2 = Rule.builder()
                                .id(1L)
                                .name("Rule B")
                                .subjectCode("RSI")
                                .subjectParam(14.0)
                                .operator("<")
                                .targetCode("CONSTANT")
                                .targetParam(30.0)
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
                                .subjectCode("SMA")
                                .subjectParam(50.0)
                                .operator(">")
                                .targetCode("SMA")
                                .targetParam(200.0)
                                .description("Description")
                                .build();

                Rule rule2 = Rule.builder()
                                .id(2L)
                                .name("Rule")
                                .subjectCode("SMA")
                                .subjectParam(50.0)
                                .operator(">")
                                .targetCode("SMA")
                                .targetParam(200.0)
                                .description("Description")
                                .build();

                // Act & Assert
                assertNotEquals(rule1, rule2);
        }

        @Test
        @DisplayName("Should properly store all field values")
        void testAllFieldsAreStoredCorrectly() {
                // Arrange & Act
                Rule rule = Rule.builder()
                                .id(10L)
                                .name("Complex Rule")
                                .subjectCode("MACD")
                                .subjectParam(12.0)
                                .operator("crosses_below")
                                .targetCode("SIGNAL")
                                .targetParam(9.0)
                                .description("MACD crosses below signal line")
                                .build();

                // Assert
                assertEquals(10L, rule.getId());
                assertEquals("Complex Rule", rule.getName());
                assertEquals("MACD", rule.getSubjectCode());
                assertEquals(12.0, rule.getSubjectParam());
                assertEquals("crosses_below", rule.getOperator());
                assertEquals("SIGNAL", rule.getTargetCode());
                assertEquals(9.0, rule.getTargetParam());
                assertEquals("MACD crosses below signal line", rule.getDescription());
        }

        @Test
        @DisplayName("Should create rule for volume comparison")
        void testCreateVolumeRule() {
                // Arrange & Act
                Rule rule = Rule.builder()
                                .id(11L)
                                .name("High Volume")
                                .subjectCode("VOLUME")
                                .subjectParam(null)
                                .operator(">")
                                .targetCode("AVG_VOLUME")
                                .targetParam(50.0)
                                .description("Volume above 50-day average")
                                .build();

                // Assert
                assertNotNull(rule);
                assertEquals("VOLUME", rule.getSubjectCode());
                assertEquals("AVG_VOLUME", rule.getTargetCode());
                assertNull(rule.getSubjectParam());
                assertEquals(50.0, rule.getTargetParam());
        }
}
