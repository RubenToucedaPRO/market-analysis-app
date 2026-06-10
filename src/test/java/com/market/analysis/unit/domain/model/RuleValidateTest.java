package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.Rule;

/**
 * Unit tests for Rule.validate() – verifies that the domain model
 * correctly rejects invalid subject/target codes, parameter values,
 * and unsupported operators before strategy persistence.
 */
@DisplayName("Rule.validate() P0 Tests")
class RuleValidateTest {

    // -------------------------------------------------------------------------
    // Valid rules
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should pass for PRICE > CONSTANT with valid param")
    void testValidPriceVsConstant() {
        Rule rule = Rule.builder()
                .subjectCode("PRICE")
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .build();
        assertDoesNotThrow(rule::validate);
    }

    @Test
    @DisplayName("Should pass for SMA50 > SMA200 with valid params")
    void testValidSma50VsSma200() {
        Rule rule = Rule.builder()
                .subjectCode("SMA")
                .subjectParam(50.0)
                .operator(">")
                .targetCode("SMA")
                .targetParam(200.0)
                .build();
        assertDoesNotThrow(rule::validate);
    }

    @Test
    @DisplayName("Should pass for RSI14 < CONSTANT 30")
    void testValidRsiVsConstant() {
        Rule rule = Rule.builder()
                .subjectCode("RSI")
                .subjectParam(14.0)
                .operator("<")
                .targetCode("CONSTANT")
                .targetParam(30.0)
                .build();
        assertDoesNotThrow(rule::validate);
    }

    @Test
    @DisplayName("Should pass for MACD_LINE >= MACD_SIGNAL")
    void testValidMacdLineCrossMacdSignal() {
        Rule rule = Rule.builder()
                .subjectCode("MACD_LINE")
                .operator(">=")
                .targetCode("MACD_SIGNAL")
                .build();
        assertDoesNotThrow(rule::validate);
    }

    @Test
    @DisplayName("Should pass for VOLUME > AVG_VOLUME")
    void testValidVolumeVsAvgVolume() {
        Rule rule = Rule.builder()
                .subjectCode("VOLUME")
                .operator(">")
                .targetCode("AVG_VOLUME")
                .build();
        assertDoesNotThrow(rule::validate);
    }

    @Test
    @DisplayName("Should pass for BB_UPPER with param 20")
    void testValidBbUpperWithParam20() {
        Rule rule = Rule.builder()
                .subjectCode("PRICE")
                .operator(">")
                .targetCode("BB_UPPER")
                .targetParam(20.0)
                .build();
        assertDoesNotThrow(rule::validate);
    }

    @Test
    @DisplayName("Should accept all supported text operators")
    void testValidOperators() {
        String[] operators = {">", ">=", "<", "<=", "=", "==", "!=",
                "GREATER_THAN", "LESS_THAN", "EQUALS", "NOT_EQUALS"};
        for (String op : operators) {
            Rule rule = Rule.builder()
                    .subjectCode("PRICE")
                    .operator(op)
                    .targetCode("CONSTANT")
                    .targetParam(50.0)
                    .build();
            assertDoesNotThrow(rule::validate, "Operator '" + op + "' should be valid");
        }
    }

    // -------------------------------------------------------------------------
    // Invalid subject codes
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should reject unsupported subject code")
    void testInvalidSubjectCode() {
        Rule rule = Rule.builder()
                .subjectCode("VWAP")
                .operator(">")
                .targetCode("PRICE")
                .build();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, rule::validate);
        assertTrue(ex.getMessage().contains("subject"));
        assertTrue(ex.getMessage().contains("VWAP"));
    }

    @Test
    @DisplayName("Should reject null subject code")
    void testNullSubjectCode() {
        Rule rule = Rule.builder()
                .subjectCode(null)
                .operator(">")
                .targetCode("PRICE")
                .build();
        assertThrows(IllegalArgumentException.class, rule::validate);
    }

    // -------------------------------------------------------------------------
    // Invalid target codes
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should reject unsupported target code")
    void testInvalidTargetCode() {
        Rule rule = Rule.builder()
                .subjectCode("PRICE")
                .operator(">")
                .targetCode("UNKNOWN_INDICATOR")
                .build();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, rule::validate);
        assertTrue(ex.getMessage().contains("target"));
        assertTrue(ex.getMessage().contains("UNKNOWN_INDICATOR"));
    }

    // -------------------------------------------------------------------------
    // Invalid parameter values
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should reject SMA with unsupported param 100")
    void testSmaWithInvalidParam() {
        Rule rule = Rule.builder()
                .subjectCode("SMA")
                .subjectParam(100.0)
                .operator(">")
                .targetCode("PRICE")
                .build();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, rule::validate);
        assertTrue(ex.getMessage().contains("100.0"));
    }

    @Test
    @DisplayName("Should reject EMA with null param when param is required")
    void testEmaWithNullParam() {
        Rule rule = Rule.builder()
                .subjectCode("EMA")
                .subjectParam(null)
                .operator(">")
                .targetCode("PRICE")
                .build();
        assertThrows(IllegalArgumentException.class, rule::validate);
    }

    @Test
    @DisplayName("Should reject RSI with param 7 (unsupported)")
    void testRsiWithInvalidParam() {
        Rule rule = Rule.builder()
                .subjectCode("RSI")
                .subjectParam(7.0)
                .operator("<")
                .targetCode("CONSTANT")
                .targetParam(30.0)
                .build();
        assertThrows(IllegalArgumentException.class, rule::validate);
    }

    @Test
    @DisplayName("Should reject PRICE with unexpected param (no-param indicator)")
    void testPriceWithUnexpectedParam() {
        Rule rule = Rule.builder()
                .subjectCode("PRICE")
                .subjectParam(10.0)
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(50.0)
                .build();
        assertThrows(IllegalArgumentException.class, rule::validate);
    }

    @Test
    @DisplayName("Should reject CONSTANT with null param")
    void testConstantWithNullParam() {
        Rule rule = Rule.builder()
                .subjectCode("PRICE")
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(null)
                .build();
        assertThrows(IllegalArgumentException.class, rule::validate);
    }

    // -------------------------------------------------------------------------
    // Invalid operators
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should reject unsupported operator CROSS_ABOVE")
    void testInvalidOperator() {
        Rule rule = Rule.builder()
                .subjectCode("PRICE")
                .operator("CROSS_ABOVE")
                .targetCode("SMA")
                .targetParam(50.0)
                .build();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, rule::validate);
        assertTrue(ex.getMessage().contains("CROSS_ABOVE"));
    }

    @Test
    @DisplayName("Should reject null operator")
    void testNullOperator() {
        Rule rule = Rule.builder()
                .subjectCode("PRICE")
                .operator(null)
                .targetCode("CONSTANT")
                .targetParam(50.0)
                .build();
        assertThrows(IllegalArgumentException.class, rule::validate);
    }
}
