package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.market.analysis.domain.model.RuleCapabilityCatalog;
import com.market.analysis.domain.model.RuleCapability;

/**
 * Unit tests for the canonical RuleCapabilityCatalog.
 * Verifies that all codes supported by RuleEvaluator are present and
 * that parameter and operator constraints are correctly represented.
 */
@DisplayName("RuleCapabilityCatalog Unit Tests")
class RuleCapabilityCatalogTest {

    // -------------------------------------------------------------------------
    // isSupported
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {
            "PRICE", "SMA", "EMA", "RSI",
            "MACD_LINE", "MACD_SIGNAL", "MACD_HIST",
            "BB_UPPER", "BB_LOWER", "ATR",
            "VOLUME", "AVG_VOLUME",
            "OPEN", "HIGH", "LOW", "PREV_CLOSE",
            "CONSTANT", "VALUE"
    })
    @DisplayName("Should recognise all codes that RuleEvaluator supports")
    void testSupportedCodes(String code) {
        assertTrue(RuleCapabilityCatalog.isSupported(code));
    }

    @ParameterizedTest
    @ValueSource(strings = {"MACD", "VWAP", "STOCH", "UNKNOWN", "", "null_like"})
    @DisplayName("Should reject codes not supported by RuleEvaluator")
    void testUnsupportedCodes(String code) {
        assertFalse(RuleCapabilityCatalog.isSupported(code));
    }

    @Test
    @DisplayName("Should return false for null code")
    void testNullCode() {
        assertFalse(RuleCapabilityCatalog.isSupported(null));
    }

    @Test
    @DisplayName("Should be case-insensitive for isSupported")
    void testCaseInsensitiveSupport() {
        assertTrue(RuleCapabilityCatalog.isSupported("sma"));
        assertTrue(RuleCapabilityCatalog.isSupported("Sma"));
        assertTrue(RuleCapabilityCatalog.isSupported("SMA"));
    }

    // -------------------------------------------------------------------------
    // getCapability – no-param indicators
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"PRICE", "MACD_LINE", "MACD_SIGNAL", "MACD_HIST",
            "VOLUME", "AVG_VOLUME", "OPEN", "HIGH", "LOW", "PREV_CLOSE"})
    @DisplayName("No-param indicators should have requiresParam=false")
    void testNoParamIndicators(String code) {
        Optional<RuleCapability> cap = RuleCapabilityCatalog.getCapability(code);
        assertTrue(cap.isPresent());
        assertFalse(cap.get().isRequiresParam());
        assertTrue(cap.get().isParamAllowed(null));
        assertFalse(cap.get().isParamAllowed(10.0));
    }

    // -------------------------------------------------------------------------
    // getCapability – fixed-param indicators
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("SMA should accept only params 20, 50, 200")
    void testSmaAllowedParams() {
        RuleCapability cap = RuleCapabilityCatalog.getCapability("SMA").orElseThrow();
        assertTrue(cap.isRequiresParam());
        assertTrue(cap.isParamAllowed(20.0));
        assertTrue(cap.isParamAllowed(50.0));
        assertTrue(cap.isParamAllowed(200.0));
        assertFalse(cap.isParamAllowed(100.0));
        assertFalse(cap.isParamAllowed(null));
    }

    @Test
    @DisplayName("EMA should accept only params 9, 12, 20, 26, 50, 200")
    void testEmaAllowedParams() {
        RuleCapability cap = RuleCapabilityCatalog.getCapability("EMA").orElseThrow();
        assertTrue(cap.isRequiresParam());
        Set.of(9.0, 12.0, 20.0, 26.0, 50.0, 200.0).forEach(p -> assertTrue(cap.isParamAllowed(p)));
        assertFalse(cap.isParamAllowed(10.0));
        assertFalse(cap.isParamAllowed(null));
    }

    @Test
    @DisplayName("RSI should accept only params 14, 30")
    void testRsiAllowedParams() {
        RuleCapability cap = RuleCapabilityCatalog.getCapability("RSI").orElseThrow();
        assertTrue(cap.isParamAllowed(14.0));
        assertTrue(cap.isParamAllowed(30.0));
        assertFalse(cap.isParamAllowed(7.0));
        assertFalse(cap.isParamAllowed(null));
    }

    @Test
    @DisplayName("BB_UPPER and BB_LOWER should accept only param 20")
    void testBbAllowedParams() {
        for (String code : new String[]{"BB_UPPER", "BB_LOWER"}) {
            RuleCapability cap = RuleCapabilityCatalog.getCapability(code).orElseThrow();
            assertTrue(cap.isParamAllowed(20.0));
            assertFalse(cap.isParamAllowed(10.0));
            assertFalse(cap.isParamAllowed(null));
        }
    }

    @Test
    @DisplayName("ATR should accept only param 14")
    void testAtrAllowedParams() {
        RuleCapability cap = RuleCapabilityCatalog.getCapability("ATR").orElseThrow();
        assertTrue(cap.isParamAllowed(14.0));
        assertFalse(cap.isParamAllowed(7.0));
        assertFalse(cap.isParamAllowed(null));
    }

    // -------------------------------------------------------------------------
    // getCapability – any-param indicators
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"CONSTANT", "VALUE"})
    @DisplayName("CONSTANT and VALUE should accept any numeric param but not null")
    void testAnyParamIndicators(String code) {
        RuleCapability cap = RuleCapabilityCatalog.getCapability(code).orElseThrow();
        assertTrue(cap.isRequiresParam());
        assertTrue(cap.isParamAllowed(0.0));
        assertTrue(cap.isParamAllowed(99999.99));
        assertTrue(cap.isParamAllowed(-1.0));
        assertFalse(cap.isParamAllowed(null));
    }

    // -------------------------------------------------------------------------
    // isOperatorSupported
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {">", ">=", "<", "<=", "=", "==", "!=",
            "GREATER_THAN", "GREATER_THAN_OR_EQUAL",
            "LESS_THAN", "LESS_THAN_OR_EQUAL",
            "EQUALS", "NOT_EQUALS"})
    @DisplayName("Should recognise all valid operators")
    void testValidOperators(String operator) {
        assertTrue(RuleCapabilityCatalog.isOperatorSupported(operator));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CROSS_ABOVE", "CROSS_BELOW", "AND", "OR", "XOR"})
    @DisplayName("Should reject unsupported operators")
    void testInvalidOperators(String operator) {
        assertFalse(RuleCapabilityCatalog.isOperatorSupported(operator));
    }

    @Test
    @DisplayName("Should return false for null operator")
    void testNullOperator() {
        assertFalse(RuleCapabilityCatalog.isOperatorSupported(null));
    }

    // -------------------------------------------------------------------------
    // getSupportedCodes
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getSupportedCodes should return non-empty set covering all known indicators")
    void testGetSupportedCodes() {
        Set<String> codes = RuleCapabilityCatalog.getSupportedCodes();
        assertNotNull(codes);
        assertFalse(codes.isEmpty());
        assertEquals(18, codes.size());
    }
}
