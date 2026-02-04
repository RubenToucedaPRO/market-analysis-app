package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.AnalysisResult;
import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.RuleResult;
import com.market.analysis.domain.model.Strategy;

/**
 * Unit tests for AnalysisResult domain entity.
 */
@DisplayName("AnalysisResult Domain Model Tests")
class AnalysisResultTest {

    @Test
    @DisplayName("Should create analysis result with valid data")
    void testCreateAnalysisResultWithValidData() {
        // Arrange
        Rule rule = Rule.builder()
                .id(1L)
                .name("Test Rule")
                .subjectCode("PRICE")
                .subjectParam(null)
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .description("Description")
                .build();

        Strategy strategy = Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Description")
                .rules(List.of(rule))
                .build();

        RuleResult ruleResult = RuleResult.builder()
                .passed(true)
                .justification("Passed")
                .rule(rule)
                .build();

        Map<String, Object> metrics = Map.of("riskReward", 2.5);

        // Act
        AnalysisResult analysisResult = AnalysisResult.builder()
                .strategy(strategy)
                .ticker("AAPL")
                .analysisTimestamp(LocalDateTime.now())
                .ruleResults(List.of(ruleResult))
                .calculatedMetrics(metrics)
                .overallPassed(true)
                .summary("Analysis passed")
                .build();

        // Assert
        assertNotNull(analysisResult);
        assertEquals(strategy, analysisResult.getStrategy());
        assertEquals("AAPL", analysisResult.getTicker());
        assertNotNull(analysisResult.getAnalysisTimestamp());
        assertEquals(1, analysisResult.getRuleResults().size());
        assertNotNull(analysisResult.getCalculatedMetrics());
        assertTrue(analysisResult.isOverallPassed());
        assertEquals("Analysis passed", analysisResult.getSummary());
    }

    @Test
    @DisplayName("Should return immutable copy of rule results list")
    void testGetRuleResultsReturnsImmutableCopy() {
        // Arrange
        Rule rule = Rule.builder()
                .id(1L)
                .name("Test Rule")
                .subjectCode("PRICE")
                .subjectParam(null)
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .description("Description")
                .build();

        Strategy strategy = Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Description")
                .rules(List.of(rule))
                .build();

        RuleResult ruleResult = RuleResult.builder()
                .passed(true)
                .justification("Passed")
                .rule(rule)
                .build();

        AnalysisResult analysisResult = AnalysisResult.builder()
                .strategy(strategy)
                .ticker("AAPL")
                .analysisTimestamp(LocalDateTime.now())
                .ruleResults(List.of(ruleResult))
                .calculatedMetrics(Map.of())
                .overallPassed(true)
                .summary("Summary")
                .build();

        // Act
        List<RuleResult> retrievedResults = analysisResult.getRuleResults();

        // Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            retrievedResults.add(RuleResult.builder()
                    .passed(false)
                    .justification("Failed")
                    .rule(rule)
                    .build());
        });
    }

    @Test
    @DisplayName("Should validate consistency and throw exception when strategy is null")
    void testValidateConsistencyThrowsExceptionWhenStrategyIsNull() {
        // Arrange
        AnalysisResult analysisResult = AnalysisResult.builder()
                .strategy(null)
                .ticker("AAPL")
                .analysisTimestamp(LocalDateTime.now())
                .ruleResults(List.of())
                .calculatedMetrics(Map.of())
                .overallPassed(false)
                .summary("Summary")
                .build();

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            analysisResult::validateConsistency);
        assertTrue(exception.getMessage().contains("Strategy"));
    }

    @Test
    @DisplayName("Should validate consistency and throw exception when ticker is null")
    void testValidateConsistencyThrowsExceptionWhenTickerIsNull() {
        // Arrange
        Rule rule = Rule.builder()
                .id(1L)
                .name("Test Rule")
                .subjectCode("PRICE")
                .subjectParam(null)
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .description("Description")
                .build();

        Strategy strategy = Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Description")
                .rules(List.of(rule))
                .build();

        AnalysisResult analysisResult = AnalysisResult.builder()
                .strategy(strategy)
                .ticker(null)
                .analysisTimestamp(LocalDateTime.now())
                .ruleResults(List.of())
                .calculatedMetrics(Map.of())
                .overallPassed(false)
                .summary("Summary")
                .build();

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            analysisResult::validateConsistency);
        assertTrue(exception.getMessage().contains("Ticker"));
    }

    @Test
    @DisplayName("Should validate consistency and throw exception when analysis timestamp is null")
    void testValidateConsistencyThrowsExceptionWhenTimestampIsNull() {
        // Arrange
        Rule rule = Rule.builder()
                .id(1L)
                .name("Test Rule")
                .subjectCode("PRICE")
                .subjectParam(null)
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .description("Description")
                .build();

        Strategy strategy = Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Description")
                .rules(List.of(rule))
                .build();

        AnalysisResult analysisResult = AnalysisResult.builder()
                .strategy(strategy)
                .ticker("AAPL")
                .analysisTimestamp(null)
                .ruleResults(List.of())
                .calculatedMetrics(Map.of())
                .overallPassed(false)
                .summary("Summary")
                .build();

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            analysisResult::validateConsistency);
        assertTrue(exception.getMessage().contains("timestamp"));
    }

    @Test
    @DisplayName("Should validate consistency and throw exception when rule results count mismatch")
    void testValidateConsistencyThrowsExceptionWhenRuleResultsCountMismatch() {
        // Arrange
        Rule rule1 = Rule.builder()
                .id(1L)
                .name("Rule 1")
                .subjectCode("PRICE")
                .subjectParam(null)
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .description("Description")
                .build();

        Rule rule2 = Rule.builder()
                .id(2L)
                .name("Rule 2")
                .subjectCode("PRICE")
                .subjectParam(null)
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .description("Description")
                .build();

        Strategy strategy = Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Description")
                .rules(List.of(rule1, rule2))
                .build();

        RuleResult ruleResult = RuleResult.builder()
                .passed(true)
                .justification("Passed")
                .rule(rule1)
                .build();

        AnalysisResult analysisResult = AnalysisResult.builder()
                .strategy(strategy)
                .ticker("AAPL")
                .analysisTimestamp(LocalDateTime.now())
                .ruleResults(List.of(ruleResult)) // Only 1 result, but strategy has 2 rules
                .calculatedMetrics(Map.of())
                .overallPassed(false)
                .summary("Summary")
                .build();

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            analysisResult::validateConsistency);
        assertTrue(exception.getMessage().contains("does not match"));
    }

    @Test
    @DisplayName("Should pass validation with valid analysis result")
    void testValidateConsistencyPassesWithValidAnalysisResult() {
        // Arrange
        Rule rule = Rule.builder()
                .id(1L)
                .name("Test Rule")
                .subjectCode("PRICE")
                .subjectParam(null)
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .description("Description")
                .build();

        Strategy strategy = Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Description")
                .rules(List.of(rule))
                .build();

        RuleResult ruleResult = RuleResult.builder()
                .passed(true)
                .justification("Passed")
                .rule(rule)
                .build();

        AnalysisResult analysisResult = AnalysisResult.builder()
                .strategy(strategy)
                .ticker("AAPL")
                .analysisTimestamp(LocalDateTime.now())
                .ruleResults(List.of(ruleResult))
                .calculatedMetrics(Map.of())
                .overallPassed(true)
                .summary("Summary")
                .build();

        // Act & Assert - should not throw
        analysisResult.validateConsistency();
    }

    @Test
    @DisplayName("Should calculate compliance rate correctly with all rules passed")
    void testCalculateComplianceRateWithAllRulesPassed() {
        // Arrange
        Rule rule1 = Rule.builder()
                .id(1L)
                .name("Rule 1")
                .subjectCode("PRICE")
                .subjectParam(null)
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .description("Description")
                .build();

        Rule rule2 = Rule.builder()
                .id(2L)
                .name("Rule 2")
                .subjectCode("PRICE")
                .subjectParam(null)
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .description("Description")
                .build();

        Strategy strategy = Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Description")
                .rules(List.of(rule1, rule2))
                .build();

        List<RuleResult> ruleResults = List.of(
            RuleResult.builder().passed(true).justification("Passed").rule(rule1).build(),
            RuleResult.builder().passed(true).justification("Passed").rule(rule2).build()
        );

        AnalysisResult analysisResult = AnalysisResult.builder()
                .strategy(strategy)
                .ticker("AAPL")
                .analysisTimestamp(LocalDateTime.now())
                .ruleResults(ruleResults)
                .calculatedMetrics(Map.of())
                .overallPassed(true)
                .summary("Summary")
                .build();

        // Act
        BigDecimal complianceRate = analysisResult.calculateComplianceRate();

        // Assert
        assertEquals(new BigDecimal("100.00"), complianceRate);
    }

    @Test
    @DisplayName("Should calculate compliance rate correctly with some rules failed")
    void testCalculateComplianceRateWithSomeRulesFailed() {
        // Arrange
        Rule rule1 = Rule.builder()
                .id(1L)
                .name("Rule 1")
                .subjectCode("PRICE")
                .subjectParam(null)
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .description("Description")
                .build();

        Rule rule2 = Rule.builder()
                .id(2L)
                .name("Rule 2")
                .subjectCode("PRICE")
                .subjectParam(null)
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .description("Description")
                .build();

        Strategy strategy = Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Description")
                .rules(List.of(rule1, rule2))
                .build();

        List<RuleResult> ruleResults = List.of(
            RuleResult.builder().passed(true).justification("Passed").rule(rule1).build(),
            RuleResult.builder().passed(false).justification("Failed").rule(rule2).build()
        );

        AnalysisResult analysisResult = AnalysisResult.builder()
                .strategy(strategy)
                .ticker("AAPL")
                .analysisTimestamp(LocalDateTime.now())
                .ruleResults(ruleResults)
                .calculatedMetrics(Map.of())
                .overallPassed(false)
                .summary("Summary")
                .build();

        // Act
        BigDecimal complianceRate = analysisResult.calculateComplianceRate();

        // Assert
        assertEquals(new BigDecimal("50.00"), complianceRate);
    }

    @Test
    @DisplayName("Should return zero compliance rate with empty rule results")
    void testCalculateComplianceRateWithEmptyRuleResults() {
        // Arrange
        Strategy strategy = Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Description")
                .rules(List.of())
                .build();

        AnalysisResult analysisResult = AnalysisResult.builder()
                .strategy(strategy)
                .ticker("AAPL")
                .analysisTimestamp(LocalDateTime.now())
                .ruleResults(List.of())
                .calculatedMetrics(Map.of())
                .overallPassed(false)
                .summary("Summary")
                .build();

        // Act
        BigDecimal complianceRate = analysisResult.calculateComplianceRate();

        // Assert
        assertEquals(BigDecimal.ZERO, complianceRate);
    }

    @Test
    @DisplayName("Should handle null rule results list in builder")
    void testBuilderWithNullRuleResultsList() {
        // Arrange
        Strategy strategy = Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Description")
                .rules(List.of())
                .build();

        // Act
        AnalysisResult analysisResult = AnalysisResult.builder()
                .strategy(strategy)
                .ticker("AAPL")
                .analysisTimestamp(LocalDateTime.now())
                .ruleResults(null)
                .calculatedMetrics(Map.of())
                .overallPassed(false)
                .summary("Summary")
                .build();

        // Assert
        assertNotNull(analysisResult.getRuleResults());
        assertTrue(analysisResult.getRuleResults().isEmpty());
    }
}
