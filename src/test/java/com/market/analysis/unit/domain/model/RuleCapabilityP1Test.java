package com.market.analysis.unit.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.RuleCapabilityCatalog;
import com.market.analysis.domain.model.Stock;

/**
 * P1 regression tests for the extended rule capability model.
 *
 * <p>Verifies:
 * <ul>
 *   <li>All capabilities carry a valid {@code IndicatorResolver} that resolves
 *       the correct field on {@link Stock}.</li>
 *   <li>{@code RuleCapability#isOperatorAllowed()} enforces per-capability
 *       operator constraints.</li>
 *   <li>{@code RuleCapability#isSubjectAllowed()} and
 *       {@code RuleCapability#isTargetAllowed()} reflect the role flags.</li>
 *   <li>{@code Rule#validate()} rejects indicators forbidden as subject or
 *       target when the catalog marks them as not allowed.</li>
 *   <li>The catalog {@code isSubjectAllowed()} / {@code isTargetAllowed()} helpers
 *       proxy the capability flags correctly.</li>
 * </ul>
 * </p>
 */
@DisplayName("RuleCapability / RuleCapabilityCatalog P1 Model Tests")
class RuleCapabilityP1Test {

    // =========================================================================
    // Resolver wiring – catalog resolvers return correct stock values
    // =========================================================================

    @Nested
    @DisplayName("Capability resolvers return correct Stock values")
    class ResolverWiringTests {

        private Stock fullStock() {
            return Stock.builder()
                    .ticker("X")
                    .currentPrice(BigDecimal.valueOf(100))
                    .openPrice(BigDecimal.valueOf(98))
                    .highOfDay(BigDecimal.valueOf(105))
                    .lowOfDay(BigDecimal.valueOf(95))
                    .previousClose(BigDecimal.valueOf(99))
                    .sma20(BigDecimal.valueOf(90))
                    .sma50(BigDecimal.valueOf(85))
                    .sma200(BigDecimal.valueOf(80))
                    .ema9(BigDecimal.valueOf(101))
                    .ema12(BigDecimal.valueOf(100))
                    .ema20(BigDecimal.valueOf(98))
                    .ema26(BigDecimal.valueOf(96))
                    .ema50(BigDecimal.valueOf(92))
                    .ema200(BigDecimal.valueOf(80))
                    .rsi14(BigDecimal.valueOf(65))
                    .rsi30(BigDecimal.valueOf(60))
                    .macdLine(BigDecimal.valueOf(2))
                    .macdSignal(BigDecimal.valueOf(1.5))
                    .macdHistogram(BigDecimal.valueOf(0.5))
                    .bbUpper20(BigDecimal.valueOf(110))
                    .bbLower20(BigDecimal.valueOf(88))
                    .atr14(BigDecimal.valueOf(3))
                    .volume(5_000_000L)
                    .averageVolume(4_000_000L)
                    .build();
        }

        @Test
        void priceResolverReturnsCurrentPrice() {
            Stock stock = fullStock();
            BigDecimal val = RuleCapabilityCatalog.getCapability("PRICE").orElseThrow()
                    .resolve(null, stock);
            assertThat(val).isEqualByComparingTo(BigDecimal.valueOf(100));
        }

        @Test
        void sma20ResolverReturnsSma20() {
            BigDecimal val = RuleCapabilityCatalog.getCapability("SMA").orElseThrow()
                    .resolve(20.0, fullStock());
            assertThat(val).isEqualByComparingTo(BigDecimal.valueOf(90));
        }

        @Test
        void sma50ResolverReturnsSma50() {
            BigDecimal val = RuleCapabilityCatalog.getCapability("SMA").orElseThrow()
                    .resolve(50.0, fullStock());
            assertThat(val).isEqualByComparingTo(BigDecimal.valueOf(85));
        }

        @Test
        void sma200ResolverReturnsSma200() {
            BigDecimal val = RuleCapabilityCatalog.getCapability("SMA").orElseThrow()
                    .resolve(200.0, fullStock());
            assertThat(val).isEqualByComparingTo(BigDecimal.valueOf(80));
        }

        @Test
        void smaResolverReturnsNullForUnsupportedPeriod() {
            BigDecimal val = RuleCapabilityCatalog.getCapability("SMA").orElseThrow()
                    .resolve(100.0, fullStock());
            assertThat(val).isNull();
        }

        @Test
        void ema9ResolverReturnsEma9() {
            BigDecimal val = RuleCapabilityCatalog.getCapability("EMA").orElseThrow()
                    .resolve(9.0, fullStock());
            assertThat(val).isEqualByComparingTo(BigDecimal.valueOf(101));
        }

        @Test
        void rsi14ResolverReturnsRsi14() {
            BigDecimal val = RuleCapabilityCatalog.getCapability("RSI").orElseThrow()
                    .resolve(14.0, fullStock());
            assertThat(val).isEqualByComparingTo(BigDecimal.valueOf(65));
        }

        @Test
        void rsiResolverReturnsNullForUnsupportedPeriod() {
            BigDecimal val = RuleCapabilityCatalog.getCapability("RSI").orElseThrow()
                    .resolve(50.0, fullStock());
            assertThat(val).isNull();
        }

        @Test
        void macdLineResolverReturnsMacdLine() {
            BigDecimal val = RuleCapabilityCatalog.getCapability("MACD_LINE").orElseThrow()
                    .resolve(null, fullStock());
            assertThat(val).isEqualByComparingTo(BigDecimal.valueOf(2));
        }

        @Test
        void macdSignalResolverReturnsMacdSignal() {
            BigDecimal val = RuleCapabilityCatalog.getCapability("MACD_SIGNAL").orElseThrow()
                    .resolve(null, fullStock());
            assertThat(val).isEqualByComparingTo(BigDecimal.valueOf(1.5));
        }

        @Test
        void macdHistResolverReturnsMacdHistogram() {
            BigDecimal val = RuleCapabilityCatalog.getCapability("MACD_HIST").orElseThrow()
                    .resolve(null, fullStock());
            assertThat(val).isEqualByComparingTo(BigDecimal.valueOf(0.5));
        }

        @Test
        void bbUpper20ResolverReturnsBbUpper20() {
            BigDecimal val = RuleCapabilityCatalog.getCapability("BB_UPPER").orElseThrow()
                    .resolve(20.0, fullStock());
            assertThat(val).isEqualByComparingTo(BigDecimal.valueOf(110));
        }

        @Test
        void bbUpper20ResolverReturnsNullForUnsupportedPeriod() {
            BigDecimal val = RuleCapabilityCatalog.getCapability("BB_UPPER").orElseThrow()
                    .resolve(50.0, fullStock());
            assertThat(val).isNull();
        }

        @Test
        void bbLower20ResolverReturnsBbLower20() {
            BigDecimal val = RuleCapabilityCatalog.getCapability("BB_LOWER").orElseThrow()
                    .resolve(20.0, fullStock());
            assertThat(val).isEqualByComparingTo(BigDecimal.valueOf(88));
        }

        @Test
        void atr14ResolverReturnsAtr14() {
            BigDecimal val = RuleCapabilityCatalog.getCapability("ATR").orElseThrow()
                    .resolve(14.0, fullStock());
            assertThat(val).isEqualByComparingTo(BigDecimal.valueOf(3));
        }

        @Test
        void atrResolverReturnsNullForUnsupportedPeriod() {
            BigDecimal val = RuleCapabilityCatalog.getCapability("ATR").orElseThrow()
                    .resolve(30.0, fullStock());
            assertThat(val).isNull();
        }

        @Test
        void volumeResolverReturnsVolume() {
            BigDecimal val = RuleCapabilityCatalog.getCapability("VOLUME").orElseThrow()
                    .resolve(null, fullStock());
            assertThat(val).isEqualByComparingTo(BigDecimal.valueOf(5_000_000));
        }

        @Test
        void avgVolumeResolverReturnsAverageVolume() {
            BigDecimal val = RuleCapabilityCatalog.getCapability("AVG_VOLUME").orElseThrow()
                    .resolve(null, fullStock());
            assertThat(val).isEqualByComparingTo(BigDecimal.valueOf(4_000_000));
        }

        @Test
        void constantResolverReturnsParamValue() {
            BigDecimal val = RuleCapabilityCatalog.getCapability("CONSTANT").orElseThrow()
                    .resolve(99.5, fullStock());
            assertThat(val).isEqualByComparingTo(BigDecimal.valueOf(99.5));
        }

        @Test
        void constantResolverReturnsNullForNullParam() {
            BigDecimal val = RuleCapabilityCatalog.getCapability("CONSTANT").orElseThrow()
                    .resolve(null, fullStock());
            assertThat(val).isNull();
        }

        @Test
        void valueResolverReturnsParamValue() {
            BigDecimal val = RuleCapabilityCatalog.getCapability("VALUE").orElseThrow()
                    .resolve(42.0, fullStock());
            assertThat(val).isEqualByComparingTo(BigDecimal.valueOf(42.0));
        }
    }

    // =========================================================================
    // Per-capability operator constraints
    // =========================================================================

    @Nested
    @DisplayName("Per-capability operator constraints (isOperatorAllowed)")
    class OperatorConstraintTests {

        @ParameterizedTest
        @ValueSource(strings = {">", ">=", "<", "<=", "=", "==", "!=",
                "GREATER_THAN", "GREATER_THAN_OR_EQUAL",
                "LESS_THAN", "LESS_THAN_OR_EQUAL",
                "EQUALS", "NOT_EQUALS"})
        @DisplayName("All VALID_OPERATORS are allowed for PRICE")
        void allValidOperatorsAllowedForPrice(String operator) {
            var cap = RuleCapabilityCatalog.getCapability("PRICE").orElseThrow();
            assertTrue(cap.isOperatorAllowed(operator));
        }

        @ParameterizedTest
        @ValueSource(strings = {"CROSS_ABOVE", "CROSS_BELOW", "AND", "OR"})
        @DisplayName("Unsupported operators not allowed for PRICE")
        void unsupportedOperatorsNotAllowedForPrice(String operator) {
            var cap = RuleCapabilityCatalog.getCapability("PRICE").orElseThrow();
            assertFalse(cap.isOperatorAllowed(operator));
        }

        @Test
        @DisplayName("isOperatorAllowed returns false for null")
        void nullOperatorNotAllowed() {
            var cap = RuleCapabilityCatalog.getCapability("SMA").orElseThrow();
            assertFalse(cap.isOperatorAllowed(null));
        }

        @Test
        @DisplayName("getAllowedOperators returns non-empty set")
        void getAllowedOperatorsNotEmpty() {
            var cap = RuleCapabilityCatalog.getCapability("RSI").orElseThrow();
            assertNotNull(cap.getAllowedOperators());
            assertFalse(cap.getAllowedOperators().isEmpty());
        }
    }

    // =========================================================================
    // Role constraints
    // =========================================================================

    @Nested
    @DisplayName("Role constraints – isSubjectAllowed / isTargetAllowed")
    class RoleConstraintTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "PRICE", "SMA", "EMA", "RSI",
                "MACD_LINE", "MACD_SIGNAL", "MACD_HIST",
                "BB_UPPER", "BB_LOWER", "ATR",
                "VOLUME", "AVG_VOLUME",
                "OPEN", "HIGH", "LOW", "PREV_CLOSE",
                "CONSTANT", "VALUE"
        })
        @DisplayName("All catalog indicators are allowed as subject")
        void allIndicatorsAllowedAsSubject(String code) {
            assertTrue(RuleCapabilityCatalog.isSubjectAllowed(code));
            assertTrue(RuleCapabilityCatalog.getCapability(code).orElseThrow().isSubjectAllowed());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "PRICE", "SMA", "EMA", "RSI",
                "MACD_LINE", "MACD_SIGNAL", "MACD_HIST",
                "BB_UPPER", "BB_LOWER", "ATR",
                "VOLUME", "AVG_VOLUME",
                "OPEN", "HIGH", "LOW", "PREV_CLOSE",
                "CONSTANT", "VALUE"
        })
        @DisplayName("All catalog indicators are allowed as target")
        void allIndicatorsAllowedAsTarget(String code) {
            assertTrue(RuleCapabilityCatalog.isTargetAllowed(code));
            assertTrue(RuleCapabilityCatalog.getCapability(code).orElseThrow().isTargetAllowed());
        }

        @Test
        @DisplayName("isSubjectAllowed returns false for unsupported code")
        void unsupportedCodeNotAllowedAsSubject() {
            assertFalse(RuleCapabilityCatalog.isSubjectAllowed("VWAP"));
        }

        @Test
        @DisplayName("isTargetAllowed returns false for unsupported code")
        void unsupportedCodeNotAllowedAsTarget() {
            assertFalse(RuleCapabilityCatalog.isTargetAllowed("STOCH"));
        }

        @Test
        @DisplayName("isSubjectAllowed returns false for null")
        void nullNotAllowedAsSubject() {
            assertFalse(RuleCapabilityCatalog.isSubjectAllowed(null));
        }

        @Test
        @DisplayName("isTargetAllowed returns false for null")
        void nullNotAllowedAsTarget() {
            assertFalse(RuleCapabilityCatalog.isTargetAllowed(null));
        }
    }

    // =========================================================================
    // Rule.validate() role constraint enforcement
    // =========================================================================

    @Nested
    @DisplayName("Rule.validate() enforces role constraints via catalog")
    class RuleValidateRoleTests {

        @Test
        @DisplayName("Valid PRICE (subject) > CONSTANT (target) passes validation")
        void validSubjectAndTargetPassValidation() {
            Rule rule = Rule.builder()
                    .subjectCode("PRICE")
                    .operator(">")
                    .targetCode("CONSTANT")
                    .targetParam(100.0)
                    .build();

            // Should not throw
            rule.validate();
        }

        @Test
        @DisplayName("SMA(50) as both subject and target passes validation")
        void smaAsSubjectAndTargetBothAllowed() {
            Rule rule = Rule.builder()
                    .subjectCode("SMA")
                    .subjectParam(50.0)
                    .operator(">")
                    .targetCode("SMA")
                    .targetParam(200.0)
                    .build();

            // Should not throw
            rule.validate();
        }

        @Test
        @DisplayName("CONSTANT as subject passes validation (currently allowed)")
        void constantAsSubjectCurrentlyAllowed() {
            // CONSTANT is allowed as subject for now (same role flags as all other indicators)
            Rule rule = Rule.builder()
                    .subjectCode("CONSTANT")
                    .subjectParam(100.0)
                    .operator(">")
                    .targetCode("CONSTANT")
                    .targetParam(50.0)
                    .build();

            // Should not throw since all indicators allow subject role currently
            Set<String> supported = RuleCapabilityCatalog.getSupportedCodes();
            assertThat(supported).contains("CONSTANT");
            assertTrue(RuleCapabilityCatalog.isSubjectAllowed("CONSTANT"));
            rule.validate();
        }

        @Test
        @DisplayName("Rule.validate() still rejects unsupported subject codes (P0 + P1)")
        void validateRejectsUnsupportedSubjectCode() {
            Rule rule = Rule.builder()
                    .subjectCode("VWAP")
                    .operator(">")
                    .targetCode("CONSTANT")
                    .targetParam(50.0)
                    .build();

            assertThrows(IllegalArgumentException.class, rule::validate);
        }

        @Test
        @DisplayName("Rule.validate() still rejects unsupported target codes (P0 + P1)")
        void validateRejectsUnsupportedTargetCode() {
            Rule rule = Rule.builder()
                    .subjectCode("PRICE")
                    .operator(">")
                    .targetCode("UNKNOWN")
                    .build();

            assertThrows(IllegalArgumentException.class, rule::validate);
        }
    }
}
