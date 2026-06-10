package com.market.analysis.unit.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.market.analysis.domain.exception.RuleNotEvaluableException;
import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.RuleResult;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.service.RuleEvaluator;

/**
 * P1 regression tests for {@link RuleEvaluator}.
 *
 * <p>Verifies that the evaluator is fully driven by
 * {@code RuleCapabilityCatalog}:
 * <ul>
 *   <li>All 18 supported indicators are resolved correctly via their
 *       registered {@code IndicatorResolver}.</li>
 *   <li>Unsupported indicator codes throw {@link RuleNotEvaluableException}
 *       instead of silently returning {@code null}.</li>
 *   <li>Unsupported operators throw {@link RuleNotEvaluableException}.</li>
 *   <li>Stock data missing for a valid indicator code produces a
 *       {@code FAILED: Missing …} justification (not an exception).</li>
 * </ul>
 * </p>
 */
@DisplayName("RuleEvaluator P1 – Catalog-Driven Resolution Tests")
class RuleEvaluatorP1Test {

    private RuleEvaluator ruleEvaluator;

    @BeforeEach
    void setUp() {
        ruleEvaluator = new RuleEvaluator();
    }

    // =========================================================================
    // Unsupported indicator codes → explicit RuleNotEvaluableException
    // =========================================================================

    @Nested
    @DisplayName("Unsupported indicator code should throw RuleNotEvaluableException")
    class UnsupportedCodeTests {

        @Test
        @DisplayName("Subject code not in catalog throws RuleNotEvaluableException")
        void shouldThrowForUnsupportedSubjectCode() {
            Stock stock = Stock.builder().ticker("X").currentPrice(BigDecimal.valueOf(100)).build();
            Rule rule = Rule.builder()
                    .subjectCode("VWAP")
                    .operator(">")
                    .targetCode("CONSTANT")
                    .targetParam(50.0)
                    .build();

            assertThatThrownBy(() -> ruleEvaluator.evaluate(rule, stock))
                    .isInstanceOf(RuleNotEvaluableException.class)
                    .satisfies(ex -> assertThat(((RuleNotEvaluableException) ex).getErrorCode())
                            .isEqualTo("rule.not_evaluable"));
        }

        @Test
        @DisplayName("Target code not in catalog throws RuleNotEvaluableException")
        void shouldThrowForUnsupportedTargetCode() {
            Stock stock = Stock.builder().ticker("X").currentPrice(BigDecimal.valueOf(100)).build();
            Rule rule = Rule.builder()
                    .subjectCode("PRICE")
                    .operator(">")
                    .targetCode("STOCH")
                    .build();

            assertThatThrownBy(() -> ruleEvaluator.evaluate(rule, stock))
                    .isInstanceOf(RuleNotEvaluableException.class)
                    .satisfies(ex -> assertThat(((RuleNotEvaluableException) ex).getErrorCode())
                            .isEqualTo("rule.not_evaluable"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"MACD", "VWAP", "STOCH", "UNKNOWN", "BOLLINGER"})
        @DisplayName("Various unsupported codes all throw RuleNotEvaluableException")
        void shouldThrowForVariousUnsupportedCodes(String code) {
            Stock stock = Stock.builder().ticker("X").currentPrice(BigDecimal.valueOf(100)).build();
            Rule rule = Rule.builder()
                    .subjectCode(code)
                    .operator(">")
                    .targetCode("CONSTANT")
                    .targetParam(50.0)
                    .build();

            assertThatThrownBy(() -> ruleEvaluator.evaluate(rule, stock))
                    .isInstanceOf(RuleNotEvaluableException.class);
        }
    }

    // =========================================================================
    // Unsupported operator → explicit RuleNotEvaluableException
    // =========================================================================

    @Nested
    @DisplayName("Unsupported operator should throw RuleNotEvaluableException")
    class UnsupportedOperatorTests {

        @Test
        @DisplayName("CROSS_ABOVE operator throws RuleNotEvaluableException")
        void shouldThrowForCrossAboveOperator() {
            Stock stock = Stock.builder().ticker("X")
                    .currentPrice(BigDecimal.valueOf(100))
                    .sma50(BigDecimal.valueOf(90))
                    .build();
            Rule rule = Rule.builder()
                    .subjectCode("PRICE")
                    .operator("CROSS_ABOVE")
                    .targetCode("SMA")
                    .targetParam(50.0)
                    .build();

            assertThatThrownBy(() -> ruleEvaluator.evaluate(rule, stock))
                    .isInstanceOf(RuleNotEvaluableException.class)
                    .satisfies(ex -> assertThat(((RuleNotEvaluableException) ex).getErrorCode())
                            .isEqualTo("rule.not_evaluable"));
        }

        @Test
        @DisplayName("Null operator throws RuleNotEvaluableException with descriptive message")
        void shouldThrowForNullOperator() {
            Stock stock = Stock.builder().ticker("X")
                    .currentPrice(BigDecimal.valueOf(100))
                    .sma50(BigDecimal.valueOf(90))
                    .build();
            Rule rule = Rule.builder()
                    .subjectCode("PRICE")
                    .operator(null)
                    .targetCode("SMA")
                    .targetParam(50.0)
                    .build();

            assertThatThrownBy(() -> ruleEvaluator.evaluate(rule, stock))
                    .isInstanceOf(RuleNotEvaluableException.class)
                    .satisfies(ex -> assertThat(((RuleNotEvaluableException) ex).getErrorCode())
                            .isEqualTo("rule.not_evaluable"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"AND", "OR", "XOR", "CROSS_BELOW", "NOT"})
        @DisplayName("Multiple unsupported operators all throw RuleNotEvaluableException")
        void shouldThrowForVariousUnsupportedOperators(String operator) {
            Stock stock = Stock.builder().ticker("X")
                    .currentPrice(BigDecimal.valueOf(100))
                    .build();
            Rule rule = Rule.builder()
                    .subjectCode("PRICE")
                    .operator(operator)
                    .targetCode("CONSTANT")
                    .targetParam(50.0)
                    .build();

            assertThatThrownBy(() -> ruleEvaluator.evaluate(rule, stock))
                    .isInstanceOf(RuleNotEvaluableException.class);
        }
    }

    // =========================================================================
    // Valid indicator – missing stock data → FAILED: Missing (not exception)
    // =========================================================================

    @Nested
    @DisplayName("Valid indicator code with missing stock data returns FAILED: Missing")
    class MissingStockDataTests {

        @Test
        @DisplayName("SMA in catalog but no SMA data → FAILED: Missing target data")
        void smaInCatalogButNoData() {
            Stock stock = Stock.builder().ticker("X")
                    .currentPrice(BigDecimal.valueOf(100))
                    .build(); // no SMA fields
            Rule rule = Rule.builder()
                    .subjectCode("PRICE")
                    .operator(">")
                    .targetCode("SMA")
                    .targetParam(50.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stock);

            assertThat(result.isPassed()).isFalse();
            assertThat(result.getJustification()).contains("Missing");
        }

        @Test
        @DisplayName("MACD_LINE in catalog but no MACD data → FAILED: Missing subject data")
        void macdLineInCatalogButNoSubjectData() {
            Stock stock = Stock.builder().ticker("X")
                    .currentPrice(BigDecimal.valueOf(100))
                    .build(); // no MACD fields
            Rule rule = Rule.builder()
                    .subjectCode("MACD_LINE")
                    .operator(">")
                    .targetCode("CONSTANT")
                    .targetParam(0.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stock);

            assertThat(result.isPassed()).isFalse();
            assertThat(result.getJustification()).contains("Missing");
        }

        @Test
        @DisplayName("ATR in catalog with unsupported period → FAILED: Missing (null from resolver)")
        void atrInCatalogUnsupportedPeriodNoException() {
            Stock stock = Stock.builder().ticker("X")
                    .currentPrice(BigDecimal.valueOf(100))
                    .atr14(BigDecimal.valueOf(3.5))
                    .build();
            Rule rule = Rule.builder()
                    .subjectCode("ATR")
                    .subjectParam(30.0) // unsupported period
                    .operator("<")
                    .targetCode("CONSTANT")
                    .targetParam(5.0)
                    .build();

            // Should NOT throw; resolver returns null for unsupported period
            RuleResult result = ruleEvaluator.evaluate(rule, stock);

            assertThat(result.isPassed()).isFalse();
            assertThat(result.getJustification()).contains("Missing");
        }

        @Test
        @DisplayName("Both subject and target data missing → FAILED: Missing both")
        void bothSubjectAndTargetMissing() {
            Stock stock = Stock.builder().ticker("X").build(); // no data at all
            Rule rule = Rule.builder()
                    .subjectCode("MACD_LINE")
                    .operator(">")
                    .targetCode("MACD_SIGNAL")
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stock);

            assertThat(result.isPassed()).isFalse();
            assertThat(result.getJustification()).contains("Missing both");
        }
    }

    // =========================================================================
    // Catalog-driven resolution – all 18 indicators resolve correctly
    // =========================================================================

    @Nested
    @DisplayName("All catalog indicators are resolved via IndicatorResolver (not switch)")
    class CatalogDrivenResolutionTests {

        @Test
        @DisplayName("PRICE resolves to currentPrice")
        void priceResolvesCorrectly() {
            Stock stock = Stock.builder().ticker("X")
                    .currentPrice(BigDecimal.valueOf(150))
                    .sma200(BigDecimal.valueOf(130))
                    .build();
            Rule rule = Rule.builder()
                    .subjectCode("PRICE")
                    .operator(">")
                    .targetCode("SMA")
                    .targetParam(200.0)
                    .build();

            assertThat(ruleEvaluator.evaluate(rule, stock).isPassed()).isTrue();
        }

        @Test
        @DisplayName("OPEN resolves to openPrice")
        void openResolvesCorrectly() {
            Stock stock = Stock.builder().ticker("X")
                    .openPrice(BigDecimal.valueOf(100))
                    .currentPrice(BigDecimal.valueOf(110))
                    .build();
            Rule rule = Rule.builder()
                    .subjectCode("PRICE")
                    .operator(">")
                    .targetCode("OPEN")
                    .build();

            assertThat(ruleEvaluator.evaluate(rule, stock).isPassed()).isTrue();
        }

        @Test
        @DisplayName("HIGH resolves to highOfDay")
        void highResolvesCorrectly() {
            Stock stock = Stock.builder().ticker("X")
                    .currentPrice(BigDecimal.valueOf(105))
                    .highOfDay(BigDecimal.valueOf(110))
                    .build();
            Rule rule = Rule.builder()
                    .subjectCode("PRICE")
                    .operator("<")
                    .targetCode("HIGH")
                    .build();

            assertThat(ruleEvaluator.evaluate(rule, stock).isPassed()).isTrue();
        }

        @Test
        @DisplayName("LOW resolves to lowOfDay")
        void lowResolvesCorrectly() {
            Stock stock = Stock.builder().ticker("X")
                    .currentPrice(BigDecimal.valueOf(95))
                    .lowOfDay(BigDecimal.valueOf(90))
                    .build();
            Rule rule = Rule.builder()
                    .subjectCode("PRICE")
                    .operator(">")
                    .targetCode("LOW")
                    .build();

            assertThat(ruleEvaluator.evaluate(rule, stock).isPassed()).isTrue();
        }

        @Test
        @DisplayName("PREV_CLOSE resolves to previousClose")
        void prevCloseResolvesCorrectly() {
            Stock stock = Stock.builder().ticker("X")
                    .currentPrice(BigDecimal.valueOf(155))
                    .previousClose(BigDecimal.valueOf(150))
                    .build();
            Rule rule = Rule.builder()
                    .subjectCode("PRICE")
                    .operator(">")
                    .targetCode("PREV_CLOSE")
                    .build();

            assertThat(ruleEvaluator.evaluate(rule, stock).isPassed()).isTrue();
        }

        @Test
        @DisplayName("VOLUME resolves to volume field")
        void volumeResolvesCorrectly() {
            Stock stock = Stock.builder().ticker("X")
                    .volume(10_000_000L)
                    .averageVolume(8_000_000L)
                    .build();
            Rule rule = Rule.builder()
                    .subjectCode("VOLUME")
                    .operator(">")
                    .targetCode("AVG_VOLUME")
                    .build();

            assertThat(ruleEvaluator.evaluate(rule, stock).isPassed()).isTrue();
        }

        @Test
        @DisplayName("BB_UPPER(20) resolves to bbUpper20")
        void bbUpperResolvesCorrectly() {
            Stock stock = Stock.builder().ticker("X")
                    .currentPrice(BigDecimal.valueOf(95))
                    .bbUpper20(BigDecimal.valueOf(110))
                    .build();
            Rule rule = Rule.builder()
                    .subjectCode("PRICE")
                    .operator("<")
                    .targetCode("BB_UPPER")
                    .targetParam(20.0)
                    .build();

            assertThat(ruleEvaluator.evaluate(rule, stock).isPassed()).isTrue();
        }

        @Test
        @DisplayName("BB_LOWER(20) resolves to bbLower20")
        void bbLowerResolvesCorrectly() {
            Stock stock = Stock.builder().ticker("X")
                    .currentPrice(BigDecimal.valueOf(95))
                    .bbLower20(BigDecimal.valueOf(90))
                    .build();
            Rule rule = Rule.builder()
                    .subjectCode("PRICE")
                    .operator(">")
                    .targetCode("BB_LOWER")
                    .targetParam(20.0)
                    .build();

            assertThat(ruleEvaluator.evaluate(rule, stock).isPassed()).isTrue();
        }

        @Test
        @DisplayName("ATR(14) resolves to atr14")
        void atrResolvesCorrectly() {
            Stock stock = Stock.builder().ticker("X")
                    .atr14(BigDecimal.valueOf(3.5))
                    .build();
            Rule rule = Rule.builder()
                    .subjectCode("ATR")
                    .subjectParam(14.0)
                    .operator("<")
                    .targetCode("CONSTANT")
                    .targetParam(5.0)
                    .build();

            assertThat(ruleEvaluator.evaluate(rule, stock).isPassed()).isTrue();
        }

        @Test
        @DisplayName("VALUE acts like CONSTANT for numeric target")
        void valueActsLikeConstant() {
            Stock stock = Stock.builder().ticker("X")
                    .currentPrice(BigDecimal.valueOf(200))
                    .build();
            Rule rule = Rule.builder()
                    .subjectCode("PRICE")
                    .operator(">")
                    .targetCode("VALUE")
                    .targetParam(150.0)
                    .build();

            assertThat(ruleEvaluator.evaluate(rule, stock).isPassed()).isTrue();
        }

        @Test
        @DisplayName("MACD_HIST resolves to macdHistogram")
        void macdHistResolvesCorrectly() {
            Stock stock = Stock.builder().ticker("X")
                    .macdHistogram(BigDecimal.valueOf(0.5))
                    .build();
            Rule rule = Rule.builder()
                    .subjectCode("MACD_HIST")
                    .operator(">")
                    .targetCode("CONSTANT")
                    .targetParam(0.0)
                    .build();

            assertThat(ruleEvaluator.evaluate(rule, stock).isPassed()).isTrue();
        }

        @Test
        @DisplayName("MACD_SIGNAL resolves to macdSignal")
        void macdSignalResolvesCorrectly() {
            Stock stock = Stock.builder().ticker("X")
                    .macdLine(BigDecimal.valueOf(2.0))
                    .macdSignal(BigDecimal.valueOf(1.5))
                    .build();
            Rule rule = Rule.builder()
                    .subjectCode("MACD_LINE")
                    .operator(">")
                    .targetCode("MACD_SIGNAL")
                    .build();

            assertThat(ruleEvaluator.evaluate(rule, stock).isPassed()).isTrue();
        }
    }

    // =========================================================================
    // All supported text operators evaluate correctly
    // =========================================================================

    @Nested
    @DisplayName("All supported operators evaluate correctly via catalog")
    class AllSupportedOperatorsTests {

        private Stock stock;

        @BeforeEach
        void setUp() {
            stock = Stock.builder().ticker("X")
                    .currentPrice(BigDecimal.valueOf(100))
                    .build();
        }

        @ParameterizedTest
        @ValueSource(strings = {">", ">=", "GREATER_THAN", "GREATER_THAN_OR_EQUAL"})
        @DisplayName("Numeric operators greater-than resolve without exception for PRICE > CONSTANT(50)")
        void greaterThanOperatorsPass(String operator) {
            Rule rule = Rule.builder()
                    .subjectCode("PRICE")
                    .operator(operator)
                    .targetCode("CONSTANT")
                    .targetParam(50.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stock);
            assertThat(result.isPassed()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"<", "<=", "LESS_THAN", "LESS_THAN_OR_EQUAL"})
        @DisplayName("Numeric operators less-than resolve without exception for PRICE < CONSTANT(200)")
        void lessThanOperatorsPass(String operator) {
            Rule rule = Rule.builder()
                    .subjectCode("PRICE")
                    .operator(operator)
                    .targetCode("CONSTANT")
                    .targetParam(200.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stock);
            assertThat(result.isPassed()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"=", "==", "EQUALS"})
        @DisplayName("Equality operators resolve for PRICE == CONSTANT(100)")
        void equalityOperatorsPass(String operator) {
            Rule rule = Rule.builder()
                    .subjectCode("PRICE")
                    .operator(operator)
                    .targetCode("CONSTANT")
                    .targetParam(100.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stock);
            assertThat(result.isPassed()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"!=", "NOT_EQUALS"})
        @DisplayName("Not-equal operators resolve for PRICE != CONSTANT(200)")
        void notEqualOperatorsPass(String operator) {
            Rule rule = Rule.builder()
                    .subjectCode("PRICE")
                    .operator(operator)
                    .targetCode("CONSTANT")
                    .targetParam(200.0)
                    .build();

            RuleResult result = ruleEvaluator.evaluate(rule, stock);
            assertThat(result.isPassed()).isTrue();
        }
    }
}
