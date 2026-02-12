package com.market.analysis.unit.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.RuleResult;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.service.RuleEvaluator;

/**
 * Unit tests for RuleEvaluator domain service.
 * Tests cover deterministic rule evaluation logic for various technical indicators.
 */
@DisplayName("RuleEvaluator Domain Service Tests")
class RuleEvaluatorTest {

    private RuleEvaluator ruleEvaluator;
    private Stock testStock;

    @BeforeEach
    void setUp() {
        ruleEvaluator = new RuleEvaluator();
        
        testStock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.00))
                .openPrice(BigDecimal.valueOf(148.00))
                .highOfDay(BigDecimal.valueOf(152.00))
                .lowOfDay(BigDecimal.valueOf(147.00))
                .previousClose(BigDecimal.valueOf(149.00))
                .sma20(BigDecimal.valueOf(145.00))
                .sma50(BigDecimal.valueOf(140.00))
                .sma200(BigDecimal.valueOf(130.00))
                .volume(10000000L)
                .averageVolume(8000000L)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create RuleEvaluator instance")
    void testRuleEvaluatorInstantiation() {
        assertThat(ruleEvaluator).isNotNull();
    }

    @Nested
    @DisplayName("Price Comparison Tests")
    class PriceComparisonTests {

        @Test
        @DisplayName("Should pass when price is greater than SMA20")
        void shouldPassWhenPriceGreaterThanSma20() {
            // Arrange
            Rule rule = Rule.builder()
                    .id(1L)
                    .name("Price > SMA20")
                    .subjectCode("PRICE")
                    .operator(">")
                    .targetCode("SMA")
                    .targetParam(20.0)
                    .build();

            // Act
            RuleResult result = ruleEvaluator.evaluate(rule, testStock);

            // Assert
            assertThat(result.isPassed()).isTrue();
            assertThat(result.getJustification()).contains("PASSED");
        }

        @Test
        @DisplayName("Should fail when price is less than SMA50")
        void shouldFailWhenPriceLessThanSma50() {
            // Arrange - create stock with price below SMA50
            Stock lowPriceStock = Stock.builder()
                    .ticker("TEST")
                    .currentPrice(BigDecimal.valueOf(100.00))
                    .sma50(BigDecimal.valueOf(120.00))
                    .build();

            Rule rule = Rule.builder()
                    .id(2L)
                    .name("Price > SMA50")
                    .subjectCode("PRICE")
                    .operator(">")
                    .targetCode("SMA")
                    .targetParam(50.0)
                    .build();

            // Act
            RuleResult result = ruleEvaluator.evaluate(rule, lowPriceStock);

            // Assert
            assertThat(result.isPassed()).isFalse();
            assertThat(result.getJustification()).contains("FAILED");
        }
    }

    @Nested
    @DisplayName("SMA Crossover Tests")
    class SmaCrossoverTests {

        @Test
        @DisplayName("Should pass when SMA20 > SMA50")
        void shouldPassWhenSma20GreaterThanSma50() {
            // Arrange
            Rule rule = Rule.builder()
                    .id(3L)
                    .name("SMA20 > SMA50")
                    .subjectCode("SMA")
                    .subjectParam(20.0)
                    .operator(">")
                    .targetCode("SMA")
                    .targetParam(50.0)
                    .build();

            // Act
            RuleResult result = ruleEvaluator.evaluate(rule, testStock);

            // Assert
            assertThat(result.isPassed()).isTrue();
        }

        @Test
        @DisplayName("Should pass when SMA50 > SMA200")
        void shouldPassWhenSma50GreaterThanSma200() {
            // Arrange
            Rule rule = Rule.builder()
                    .id(4L)
                    .name("SMA50 > SMA200")
                    .subjectCode("SMA")
                    .subjectParam(50.0)
                    .operator(">")
                    .targetCode("SMA")
                    .targetParam(200.0)
                    .build();

            // Act
            RuleResult result = ruleEvaluator.evaluate(rule, testStock);

            // Assert
            assertThat(result.isPassed()).isTrue();
        }
    }

    @Nested
    @DisplayName("Volume Tests")
    class VolumeTests {

        @Test
        @DisplayName("Should pass when volume > average volume")
        void shouldPassWhenVolumeGreaterThanAverage() {
            // Arrange
            Rule rule = Rule.builder()
                    .id(5L)
                    .name("Volume > Avg Volume")
                    .subjectCode("VOLUME")
                    .operator(">")
                    .targetCode("AVG_VOLUME")
                    .build();

            // Act
            RuleResult result = ruleEvaluator.evaluate(rule, testStock);

            // Assert
            assertThat(result.isPassed()).isTrue();
        }

        @Test
        @DisplayName("Should pass when volume > constant threshold")
        void shouldPassWhenVolumeGreaterThanConstant() {
            // Arrange
            Rule rule = Rule.builder()
                    .id(6L)
                    .name("Volume > 5M")
                    .subjectCode("VOLUME")
                    .operator(">")
                    .targetCode("CONSTANT")
                    .targetParam(5000000.0)
                    .build();

            // Act
            RuleResult result = ruleEvaluator.evaluate(rule, testStock);

            // Assert
            assertThat(result.isPassed()).isTrue();
        }
    }

    @Nested
    @DisplayName("Operator Tests")
    class OperatorTests {

        @Test
        @DisplayName("Should handle >= operator correctly")
        void shouldHandleGreaterThanOrEqualOperator() {
            // Arrange
            Rule rule = Rule.builder()
                    .id(7L)
                    .name("Price >= Previous Close")
                    .subjectCode("PRICE")
                    .operator(">=")
                    .targetCode("PREV_CLOSE")
                    .build();

            // Act
            RuleResult result = ruleEvaluator.evaluate(rule, testStock);

            // Assert
            assertThat(result.isPassed()).isTrue();
        }

        @Test
        @DisplayName("Should handle < operator correctly")
        void shouldHandleLessThanOperator() {
            // Arrange
            Rule rule = Rule.builder()
                    .id(8L)
                    .name("Price < High of Day")
                    .subjectCode("PRICE")
                    .operator("<")
                    .targetCode("HIGH")
                    .build();

            // Act
            RuleResult result = ruleEvaluator.evaluate(rule, testStock);

            // Assert
            assertThat(result.isPassed()).isTrue();
        }
    }

    @Nested
    @DisplayName("Missing Data Tests")
    class MissingDataTests {

        @Test
        @DisplayName("Should fail when SMA data is missing")
        void shouldFailWhenSmaDataMissing() {
            // Arrange
            Stock stockWithoutSma = Stock.builder()
                    .ticker("TEST")
                    .currentPrice(BigDecimal.valueOf(100.00))
                    .build();

            Rule rule = Rule.builder()
                    .id(9L)
                    .name("Price > SMA20")
                    .subjectCode("PRICE")
                    .operator(">")
                    .targetCode("SMA")
                    .targetParam(20.0)
                    .build();

            // Act
            RuleResult result = ruleEvaluator.evaluate(rule, stockWithoutSma);

            // Assert
            assertThat(result.isPassed()).isFalse();
            assertThat(result.getJustification()).contains("Missing");
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when rule is null")
        void shouldThrowExceptionWhenRuleIsNull() {
            assertThatThrownBy(() -> ruleEvaluator.evaluate(null, testStock))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Rule cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when stock is null")
        void shouldThrowExceptionWhenStockIsNull() {
            Rule rule = Rule.builder()
                    .id(10L)
                    .name("Test Rule")
                    .subjectCode("PRICE")
                    .operator(">")
                    .targetCode("CONSTANT")
                    .targetParam(100.0)
                    .build();

            assertThatThrownBy(() -> ruleEvaluator.evaluate(rule, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Stock cannot be null");
        }
    }
}
