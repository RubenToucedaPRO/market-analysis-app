package com.market.analysis.unit.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;

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
 * Tests cover deterministic rule evaluation logic for various technical
 * indicators.
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
                .lastUpdated(Instant.now())
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

    @Nested
    @DisplayName("EMA Rule Tests")
    class EmaRuleTests {

        private Stock stockWithEma;

        @BeforeEach
        void setUpEmaStock() {
            stockWithEma = Stock.builder()
                    .ticker("AAPL")
                    .currentPrice(BigDecimal.valueOf(155.00))
                    .ema9(BigDecimal.valueOf(150.00))
                    .ema12(BigDecimal.valueOf(148.00))
                    .ema20(BigDecimal.valueOf(145.00))
                    .ema26(BigDecimal.valueOf(142.00))
                    .ema50(BigDecimal.valueOf(135.00))
                    .ema200(BigDecimal.valueOf(120.00))
                    .build();
        }

        @Test
        @DisplayName("Should pass rule when price is above EMA9")
        void shouldPassRuleWhenPriceAboveEma9() {
            Rule rule = Rule.builder()
                    .id(1L)
                    .name("Price > EMA9")
                    .subjectCode("PRICE")
                    .operator(">")
                    .targetCode("EMA")
                    .targetParam(9.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockWithEma);

            assertThat(result.isPassed()).isTrue();
            assertThat(result.getJustification()).contains("PASSED");
        }

        @Test
        @DisplayName("Should fail rule when price is below EMA200")
        void shouldFailRuleWhenPriceBelowEma200() {
            Stock lowPriceStock = Stock.builder()
                    .ticker("AAPL")
                    .currentPrice(BigDecimal.valueOf(100.00))
                    .ema200(BigDecimal.valueOf(120.00))
                    .build();

            Rule rule = Rule.builder()
                    .id(2L)
                    .name("Price > EMA200")
                    .subjectCode("PRICE")
                    .operator(">")
                    .targetCode("EMA")
                    .targetParam(200.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, lowPriceStock);

            assertThat(result.isPassed()).isFalse();
            assertThat(result.getJustification()).contains("FAILED");
        }

        @Test
        @DisplayName("Should return failed result when EMA value is null")
        void shouldReturnFailedWhenEmaValueIsNull() {
            Stock stockNoEma = Stock.builder()
                    .ticker("AAPL")
                    .currentPrice(BigDecimal.valueOf(150.00))
                    .ema9(null)
                    .build();

            Rule rule = Rule.builder()
                    .id(3L)
                    .name("Price > EMA9")
                    .subjectCode("PRICE")
                    .operator(">")
                    .targetCode("EMA")
                    .targetParam(9.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockNoEma);

            assertThat(result.isPassed()).isFalse();
            assertThat(result.getJustification()).contains("FAILED");
        }

        @Test
        @DisplayName("Should format EMA name correctly in justification")
        void shouldFormatEmaNameCorrectlyInJustification() {
            Rule rule = Rule.builder()
                    .id(4L)
                    .name("Price > EMA9")
                    .subjectCode("PRICE")
                    .operator(">")
                    .targetCode("EMA")
                    .targetParam(9.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockWithEma);

            assertThat(result.getJustification()).contains("EMA9");
        }

        @Test
        @DisplayName("Should evaluate EMA cross rule: EMA9 > EMA26 (golden cross)")
        void shouldEvaluateEmaCrossRule() {
            Rule rule = Rule.builder()
                    .id(5L)
                    .name("EMA9 > EMA26")
                    .subjectCode("EMA")
                    .subjectParam(9.0)
                    .operator(">")
                    .targetCode("EMA")
                    .targetParam(26.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockWithEma);

            // EMA9=150 > EMA26=142 → should pass
            assertThat(result.isPassed()).isTrue();
        }

        @Test
        @DisplayName("Should return null for unsupported EMA period")
        void shouldReturnFailedForUnsupportedEmaPeriod() {
            Rule rule = Rule.builder()
                    .id(6L)
                    .name("Price > EMA100")
                    .subjectCode("PRICE")
                    .operator(">")
                    .targetCode("EMA")
                    .targetParam(100.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockWithEma);

            // EMA100 is not a supported period → target value null → failed
            assertThat(result.isPassed()).isFalse();
        }

        @Test
        @DisplayName("Should handle all six supported EMA periods")
        void shouldHandleAllSixSupportedEmaPeriods() {
            double[] periods = {9.0, 12.0, 20.0, 26.0, 50.0, 200.0};

            for (double period : periods) {
                Rule rule = Rule.builder()
                        .id(7L)
                        .name("Price > EMA" + (int) period)
                        .subjectCode("PRICE")
                        .operator(">")
                        .targetCode("EMA")
                        .targetParam(period)
                        .build();

                RuleResult result = ruleEvaluator.evaluate(rule, stockWithEma);
                // price=155 > all EMA values (200→120, 50→135, 26→142, 20→145, 12→148, 9→150)
                assertThat(result.isPassed())
                        .as("Expected PRICE > EMA%d to pass", (int) period)
                        .isTrue();
            }
        }
    }

    @Nested
    @DisplayName("RSI Rule Tests")
    class RsiRuleTests {

        private Stock stockWithRsi;

        @BeforeEach
        void setUpRsiStock() {
            stockWithRsi = Stock.builder()
                    .ticker("AAPL")
                    .currentPrice(BigDecimal.valueOf(155.00))
                    .rsi14(BigDecimal.valueOf(65.00))
                    .rsi30(BigDecimal.valueOf(55.00))
                    .build();
        }

        @Test
        @DisplayName("Should pass rule when RSI14 is below oversold threshold (RSI14 < 30)")
        void shouldPassRuleWhenRsi14BelowOversoldThreshold() {
            Stock oversoldStock = Stock.builder()
                    .ticker("AAPL")
                    .currentPrice(BigDecimal.valueOf(100.00))
                    .rsi14(BigDecimal.valueOf(25.00))
                    .build();

            Rule rule = Rule.builder()
                    .id(1L)
                    .name("RSI14 < 30")
                    .subjectCode("RSI")
                    .subjectParam(14.0)
                    .operator("<")
                    .targetCode("CONSTANT")
                    .targetParam(30.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, oversoldStock);

            assertThat(result.isPassed()).isTrue();
            assertThat(result.getJustification()).contains("PASSED");
        }

        @Test
        @DisplayName("Should pass rule when RSI14 is above overbought threshold (RSI14 > 70)")
        void shouldPassRuleWhenRsi14AboveOverboughtThreshold() {
            Stock overboughtStock = Stock.builder()
                    .ticker("AAPL")
                    .currentPrice(BigDecimal.valueOf(200.00))
                    .rsi14(BigDecimal.valueOf(75.00))
                    .build();

            Rule rule = Rule.builder()
                    .id(2L)
                    .name("RSI14 > 70")
                    .subjectCode("RSI")
                    .subjectParam(14.0)
                    .operator(">")
                    .targetCode("CONSTANT")
                    .targetParam(70.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, overboughtStock);

            assertThat(result.isPassed()).isTrue();
        }

        @Test
        @DisplayName("Should pass rule when RSI30 > 50 (positive long-term momentum)")
        void shouldPassRuleWhenRsi30AboveFifty() {
            Rule rule = Rule.builder()
                    .id(3L)
                    .name("RSI30 > 50")
                    .subjectCode("RSI")
                    .subjectParam(30.0)
                    .operator(">")
                    .targetCode("CONSTANT")
                    .targetParam(50.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockWithRsi);

            // RSI30 = 55 > 50 → should pass
            assertThat(result.isPassed()).isTrue();
        }

        @Test
        @DisplayName("Should fail rule when RSI14 is missing (null)")
        void shouldFailRuleWhenRsi14IsNull() {
            Stock stockNoRsi = Stock.builder()
                    .ticker("AAPL")
                    .currentPrice(BigDecimal.valueOf(150.00))
                    .rsi14(null)
                    .build();

            Rule rule = Rule.builder()
                    .id(4L)
                    .name("RSI14 < 30")
                    .subjectCode("RSI")
                    .subjectParam(14.0)
                    .operator("<")
                    .targetCode("CONSTANT")
                    .targetParam(30.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockNoRsi);

            assertThat(result.isPassed()).isFalse();
            assertThat(result.getJustification()).contains("Missing");
        }

        @Test
        @DisplayName("Should fail rule for unsupported RSI period")
        void shouldFailRuleForUnsupportedRsiPeriod() {
            Rule rule = Rule.builder()
                    .id(5L)
                    .name("RSI50 < 50")
                    .subjectCode("RSI")
                    .subjectParam(50.0)
                    .operator("<")
                    .targetCode("CONSTANT")
                    .targetParam(50.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockWithRsi);

            // RSI50 is not a supported period → null → failed
            assertThat(result.isPassed()).isFalse();
        }

        @Test
        @DisplayName("Should format RSI name correctly in justification")
        void shouldFormatRsiNameCorrectlyInJustification() {
            Rule rule = Rule.builder()
                    .id(6L)
                    .name("RSI14 > 50")
                    .subjectCode("RSI")
                    .subjectParam(14.0)
                    .operator(">")
                    .targetCode("CONSTANT")
                    .targetParam(50.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockWithRsi);

            assertThat(result.getJustification()).contains("RSI14");
        }

        @Test
        @DisplayName("Should handle both RSI14 and RSI30 periods")
        void shouldHandleBothSupportedRsiPeriods() {
            double[] periods = {14.0, 30.0};

            for (double period : periods) {
                Rule rule = Rule.builder()
                        .id(7L)
                        .name("RSI" + (int) period + " > 0")
                        .subjectCode("RSI")
                        .subjectParam(period)
                        .operator(">")
                        .targetCode("CONSTANT")
                        .targetParam(0.0)
                        .build();

                RuleResult result = ruleEvaluator.evaluate(rule, stockWithRsi);
                assertThat(result.isPassed())
                        .as("Expected RSI%d > 0 to pass", (int) period)
                        .isTrue();
            }
        }
    }

    @Nested
    @DisplayName("MACD Rule Tests")
    class MacdRuleTests {

        private Stock stockWithMacd;

        @BeforeEach
        void setUpMacdStock() {
            stockWithMacd = Stock.builder()
                    .ticker("AAPL")
                    .currentPrice(BigDecimal.valueOf(155.00))
                    .macdLine(BigDecimal.valueOf(2.50))
                    .macdSignal(BigDecimal.valueOf(1.80))
                    .macdHistogram(BigDecimal.valueOf(0.70))
                    .build();
        }

        @Test
        @DisplayName("Should pass rule when MACD_LINE crosses above zero (bullish)")
        void shouldPassRuleWhenMacdLineAboveZero() {
            Rule rule = Rule.builder()
                    .id(1L)
                    .name("MACD_LINE > 0")
                    .subjectCode("MACD_LINE")
                    .subjectParam(null)
                    .operator(">")
                    .targetCode("CONSTANT")
                    .targetParam(0.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockWithMacd);

            assertThat(result.isPassed()).isTrue();
            assertThat(result.getJustification()).contains("PASSED");
        }

        @Test
        @DisplayName("Should pass rule when MACD_LINE is above MACD_SIGNAL (bullish crossover)")
        void shouldPassRuleWhenMacdLineAboveMacdSignal() {
            Rule rule = Rule.builder()
                    .id(2L)
                    .name("MACD_LINE > MACD_SIGNAL")
                    .subjectCode("MACD_LINE")
                    .subjectParam(null)
                    .operator(">")
                    .targetCode("MACD_SIGNAL")
                    .targetParam(null)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockWithMacd);

            // macdLine (2.50) > macdSignal (1.80) → should pass
            assertThat(result.isPassed()).isTrue();
        }

        @Test
        @DisplayName("Should pass rule when MACD_HIST is positive (bullish momentum)")
        void shouldPassRuleWhenMacdHistIsPositive() {
            Rule rule = Rule.builder()
                    .id(3L)
                    .name("MACD_HIST > 0")
                    .subjectCode("MACD_HIST")
                    .subjectParam(null)
                    .operator(">")
                    .targetCode("CONSTANT")
                    .targetParam(0.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockWithMacd);

            // macdHistogram (0.70) > 0 → should pass
            assertThat(result.isPassed()).isTrue();
        }

        @Test
        @DisplayName("Should fail rule when MACD_LINE is null (no data available)")
        void shouldFailRuleWhenMacdLineIsNull() {
            Stock stockNoMacd = Stock.builder()
                    .ticker("AAPL")
                    .currentPrice(BigDecimal.valueOf(100.00))
                    .build();

            Rule rule = Rule.builder()
                    .id(4L)
                    .name("MACD_LINE > 0")
                    .subjectCode("MACD_LINE")
                    .subjectParam(null)
                    .operator(">")
                    .targetCode("CONSTANT")
                    .targetParam(0.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockNoMacd);

            assertThat(result.isPassed()).isFalse();
            assertThat(result.getJustification()).contains("Missing");
        }

        @Test
        @DisplayName("Should fail rule when MACD_LINE is below zero (bearish)")
        void shouldFailRuleWhenMacdLineIsBelowZero() {
            Stock bearishStock = Stock.builder()
                    .ticker("AAPL")
                    .currentPrice(BigDecimal.valueOf(100.00))
                    .macdLine(BigDecimal.valueOf(-1.50))
                    .macdSignal(BigDecimal.valueOf(-1.00))
                    .macdHistogram(BigDecimal.valueOf(-0.50))
                    .build();

            Rule rule = Rule.builder()
                    .id(5L)
                    .name("MACD_LINE > 0")
                    .subjectCode("MACD_LINE")
                    .subjectParam(null)
                    .operator(">")
                    .targetCode("CONSTANT")
                    .targetParam(0.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, bearishStock);

            assertThat(result.isPassed()).isFalse();
            assertThat(result.getJustification()).contains("FAILED");
        }

        @Test
        @DisplayName("Justification should include MACD_LINE code for display")
        void justificationShouldIncludeMacdLineCode() {
            Rule rule = Rule.builder()
                    .id(6L)
                    .name("MACD_LINE > 0")
                    .subjectCode("MACD_LINE")
                    .subjectParam(null)
                    .operator(">")
                    .targetCode("CONSTANT")
                    .targetParam(0.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockWithMacd);

            assertThat(result.getJustification()).contains("MACD_LINE");
        }
    }

    @Nested
    @DisplayName("Phase 5 — Bollinger Bands and ATR Rule Tests")
    class BollingerAtrRuleTests {

        private Stock stockWithBbAtr;

        @BeforeEach
        void setUp() {
            stockWithBbAtr = Stock.builder()
                    .ticker("AAPL")
                    .currentPrice(BigDecimal.valueOf(102.00))
                    .bbUpper20(BigDecimal.valueOf(110.00))
                    .bbLower20(BigDecimal.valueOf(90.00))
                    .atr14(BigDecimal.valueOf(3.50))
                    .build();
        }

        @Test
        @DisplayName("Should pass rule when price is below BB_UPPER(20)")
        void shouldPassRuleWhenPriceIsBelowBbUpper() {
            Rule rule = Rule.builder()
                    .id(1L)
                    .name("PRICE < BB_UPPER(20)")
                    .subjectCode("PRICE")
                    .subjectParam(null)
                    .operator("<")
                    .targetCode("BB_UPPER")
                    .targetParam(20.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockWithBbAtr);

            assertThat(result.isPassed()).isTrue();
        }

        @Test
        @DisplayName("Should pass rule when price is above BB_LOWER(20)")
        void shouldPassRuleWhenPriceIsAboveBbLower() {
            Rule rule = Rule.builder()
                    .id(2L)
                    .name("PRICE > BB_LOWER(20)")
                    .subjectCode("PRICE")
                    .subjectParam(null)
                    .operator(">")
                    .targetCode("BB_LOWER")
                    .targetParam(20.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockWithBbAtr);

            assertThat(result.isPassed()).isTrue();
        }

        @Test
        @DisplayName("Should fail rule when BB_UPPER param is unsupported period")
        void shouldReturnNullForUnsupportedBbPeriod() {
            Stock stock = Stock.builder()
                    .ticker("AAPL")
                    .currentPrice(BigDecimal.valueOf(100.00))
                    .build();

            Rule rule = Rule.builder()
                    .id(3L)
                    .name("PRICE < BB_UPPER(50)")
                    .subjectCode("PRICE")
                    .subjectParam(null)
                    .operator("<")
                    .targetCode("BB_UPPER")
                    .targetParam(50.0) // unsupported period
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stock);

            assertThat(result.isPassed()).isFalse();
            assertThat(result.getJustification()).contains("Missing");
        }

        @Test
        @DisplayName("Should pass rule when ATR(14) is below a constant threshold")
        void shouldPassRuleWhenAtrIsBelowThreshold() {
            Rule rule = Rule.builder()
                    .id(4L)
                    .name("ATR < 5.00")
                    .subjectCode("ATR")
                    .subjectParam(14.0)
                    .operator("<")
                    .targetCode("CONSTANT")
                    .targetParam(5.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockWithBbAtr);

            assertThat(result.isPassed()).isTrue();
        }

        @Test
        @DisplayName("Should fail rule when ATR(14) is above a constant threshold")
        void shouldFailRuleWhenAtrIsAboveThreshold() {
            Rule rule = Rule.builder()
                    .id(5L)
                    .name("ATR < 2.00")
                    .subjectCode("ATR")
                    .subjectParam(14.0)
                    .operator("<")
                    .targetCode("CONSTANT")
                    .targetParam(2.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockWithBbAtr);

            assertThat(result.isPassed()).isFalse();
        }

        @Test
        @DisplayName("Should fail rule when ATR is null (no data)")
        void shouldFailRuleWhenAtrIsNull() {
            Stock stockNoAtr = Stock.builder()
                    .ticker("AAPL")
                    .currentPrice(BigDecimal.valueOf(100.00))
                    .build();

            Rule rule = Rule.builder()
                    .id(6L)
                    .name("ATR < 5.00")
                    .subjectCode("ATR")
                    .subjectParam(14.0)
                    .operator("<")
                    .targetCode("CONSTANT")
                    .targetParam(5.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockNoAtr);

            assertThat(result.isPassed()).isFalse();
            assertThat(result.getJustification()).contains("Missing");
        }

        @Test
        @DisplayName("Should return null for unsupported ATR period")
        void shouldReturnNullForUnsupportedAtrPeriod() {
            Rule rule = Rule.builder()
                    .id(7L)
                    .name("ATR(30) < 5.00")
                    .subjectCode("ATR")
                    .subjectParam(30.0) // unsupported period
                    .operator("<")
                    .targetCode("CONSTANT")
                    .targetParam(5.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockWithBbAtr);

            assertThat(result.isPassed()).isFalse();
            assertThat(result.getJustification()).contains("Missing");
        }

        @Test
        @DisplayName("Should pass rule when price is below BB_LOWER (oversold signal)")
        void shouldPassRuleWhenPriceBelowBbLower() {
            Stock stockBelowBands = Stock.builder()
                    .ticker("AAPL")
                    .currentPrice(BigDecimal.valueOf(85.00))
                    .bbUpper20(BigDecimal.valueOf(110.00))
                    .bbLower20(BigDecimal.valueOf(90.00))
                    .build();

            Rule rule = Rule.builder()
                    .id(8L)
                    .name("PRICE < BB_LOWER(20)")
                    .subjectCode("PRICE")
                    .subjectParam(null)
                    .operator("<")
                    .targetCode("BB_LOWER")
                    .targetParam(20.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockBelowBands);

            assertThat(result.isPassed()).isTrue();
        }

        @Test
        @DisplayName("Should pass rule when price is above BB_UPPER (overbought signal)")
        void shouldPassRuleWhenPriceAboveBbUpper() {
            Stock stockAboveBands = Stock.builder()
                    .ticker("AAPL")
                    .currentPrice(BigDecimal.valueOf(115.00))
                    .bbUpper20(BigDecimal.valueOf(110.00))
                    .bbLower20(BigDecimal.valueOf(90.00))
                    .build();

            Rule rule = Rule.builder()
                    .id(9L)
                    .name("PRICE > BB_UPPER(20)")
                    .subjectCode("PRICE")
                    .subjectParam(null)
                    .operator(">")
                    .targetCode("BB_UPPER")
                    .targetParam(20.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockAboveBands);

            assertThat(result.isPassed()).isTrue();
        }

        @Test
        @DisplayName("Should fail rule when BB value is null (no indicator data available)")
        void shouldReturnFailedWhenBbValueIsNull() {
            Stock stockNoBb = Stock.builder()
                    .ticker("AAPL")
                    .currentPrice(BigDecimal.valueOf(100.00))
                    .build();

            Rule rule = Rule.builder()
                    .id(10L)
                    .name("PRICE < BB_UPPER(20)")
                    .subjectCode("PRICE")
                    .subjectParam(null)
                    .operator("<")
                    .targetCode("BB_UPPER")
                    .targetParam(20.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockNoBb);

            assertThat(result.isPassed()).isFalse();
            assertThat(result.getJustification()).contains("Missing");
        }

        @Test
        @DisplayName("Justification should include ATR code when evaluating ATR rule")
        void shouldFormatAtrNameInJustification() {
            Rule rule = Rule.builder()
                    .id(11L)
                    .name("ATR < 5.00")
                    .subjectCode("ATR")
                    .subjectParam(14.0)
                    .operator("<")
                    .targetCode("CONSTANT")
                    .targetParam(5.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stockWithBbAtr);

            assertThat(result.getJustification()).contains("ATR");
        }
    }
}
