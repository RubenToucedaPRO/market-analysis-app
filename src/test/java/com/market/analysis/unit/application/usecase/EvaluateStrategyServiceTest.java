package com.market.analysis.unit.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.domain.exception.MissingIndicatorException;
import com.market.analysis.domain.model.ObjectiveType;
import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.RuleResult;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.domain.model.StrategyObjective;
import com.market.analysis.domain.port.out.StrategyEvaluationRepository;
import com.market.analysis.domain.service.EvaluateStrategyService;
import com.market.analysis.domain.service.RiskRewardCalculator;
import com.market.analysis.domain.service.RuleEvaluator;

@ExtendWith(MockitoExtension.class)
@DisplayName("EvaluateStrategyService Tests")
class EvaluateStrategyServiceTest {

        @Mock
        private RuleEvaluator ruleEvaluator;

        @Mock
        private RiskRewardCalculator riskRewardCalculator;

        @Mock
        private StrategyEvaluationRepository strategyEvaluationRepository;

        @InjectMocks
        private EvaluateStrategyService service;

        private Stock testStock;
        private Strategy testStrategy;
        private Rule rule1;
        private Rule rule2;

        @BeforeEach
        void setUp() {
                StrategyEvaluation strategyEvaluation = StrategyEvaluation.builder()
                                .id(1L)
                                .ticker("AAPL")
                                .strategyId(1L)
                                .strategyName("Momentum Strategy")
                                .compliant(false)
                                .complianceRate(BigDecimal.ZERO)
                                .summary("")
                                .evaluatedAt(Instant.now())
                                .priceAtEvaluation(BigDecimal.ZERO)
                                .isLatest(false)
                                .build();

                testStock = Stock.builder()
                                .ticker("AAPL")
                                .currentPrice(BigDecimal.valueOf(150.00))
                                .sma20(BigDecimal.valueOf(145.00))
                                .sma50(BigDecimal.valueOf(140.00))
                                .volume(10000000L)
                                .averageVolume(8000000L)
                                .lastUpdated(Instant.now())
                                .strategyEvaluation(strategyEvaluation)
                                .build();

                rule1 = Rule.builder()
                                .id(1L)
                                .name("Price > SMA20")
                                .subjectCode("PRICE")
                                .operator(">")
                                .targetCode("SMA")
                                .targetParam(20.0)
                                .description("Price should be above SMA20")
                                .build();

                rule2 = Rule.builder()
                                .id(2L)
                                .name("Volume > Avg Volume")
                                .subjectCode("VOLUME")
                                .operator(">")
                                .targetCode("AVG_VOLUME")
                                .description("Volume should exceed average")
                                .build();

                testStrategy = Strategy.builder()
                                .id(1L)
                                .name("Momentum Strategy")
                                .description("Bullish momentum indicator")
                                .rules(List.of(rule1, rule2))
                                .objective(StrategyObjective.builder()
                                                .targetType(ObjectiveType.PERCENTAGE)
                                                .stopLossType(ObjectiveType.PERCENTAGE)
                                                .targetValue(BigDecimal.valueOf(5.0))
                                                .stopLossValue(BigDecimal.valueOf(2.0))
                                                .capitalToRisk(BigDecimal.valueOf(1000.0))
                                                .description("Momentum objective")
                                                .build())
                                .build();

                // lenient: non-compliant test paths do not call the calculator,
                // so these stubs must be lenient to avoid UnnecessaryStubbingException.
                lenient().when(riskRewardCalculator.calculateTargetPrice(any(), any(), any()))
                                .thenReturn(BigDecimal.valueOf(157.50));
                lenient().when(riskRewardCalculator.calculateStopLossPrice(any(), any(), any()))
                                .thenReturn(BigDecimal.valueOf(147.00));
                lenient().when(riskRewardCalculator.calculateRiskRewardRatio(any(), any(), any()))
                                .thenReturn(BigDecimal.valueOf(2.5));
                lenient().when(riskRewardCalculator.calculatePositionSize(any(), any(), any()))
                                .thenReturn(BigDecimal.valueOf(94));
        }

        @Nested
        @DisplayName("Successful Evaluation Tests")
        class SuccessfulEvaluationTests {

                @Test
                @DisplayName("Should evaluate strategy successfully when all rules pass")
                void shouldEvaluateStrategySuccessfully() {
                        // Arrange
                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1)
                                        .passed(true)
                                        .justification("PASSED: PRICE (150.00) > SMA20 (145.00)")
                                        .build();

                        RuleResult result2 = RuleResult.builder()
                                        .rule(rule2)
                                        .passed(true)
                                        .justification("PASSED: VOLUME (10000000) > AVG_VOLUME (8000000)")
                                        .build();

                        when(ruleEvaluator.evaluate(rule1, testStock)).thenReturn(result1);
                        when(ruleEvaluator.evaluate(rule2, testStock)).thenReturn(result2);

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert
                        assertThat(result).isNotNull();
                        assertThat(result.isCompliant()).isTrue();
                        assertThat(result.getTicker()).isEqualTo("AAPL");
                        assertThat(result.getStrategyId()).isEqualTo(1L);
                        assertThat(result.getStrategyName()).isEqualTo("Momentum Strategy");
                        assertThat(result.getComplianceRate()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
                        assertThat(result.getSummary()).contains("2/2 rules passed");
                        assertThat(result.isLatest()).isTrue();

                        verify(ruleEvaluator, times(2)).evaluate(any(Rule.class), any(Stock.class));
                }

                @Test
                @DisplayName("Should evaluate with mixed rule results")
                void shouldEvaluateWithMixedResults() {
                        // Arrange
                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();

                        RuleResult result2 = RuleResult.builder()
                                        .rule(rule2)
                                        .passed(false)
                                        .justification("FAILED")
                                        .build();

                        when(ruleEvaluator.evaluate(rule1, testStock)).thenReturn(result1);
                        when(ruleEvaluator.evaluate(rule2, testStock)).thenReturn(result2);

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert
                        assertThat(result.isCompliant()).isFalse();
                        assertThat(result.getComplianceRate()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
                        assertThat(result.getSummary()).contains("FAILED").contains("Volume > Avg Volume");
                }

                @Test
                @DisplayName("Should capture stock price at evaluation time")
                void shouldCaptureStockPriceAtEvaluation() {
                        // Arrange
                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();

                        when(ruleEvaluator.evaluate(any(Rule.class), any(Stock.class))).thenReturn(result1);

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert
                        assertThat(result.getPriceAtEvaluation()).isEqualByComparingTo(BigDecimal.valueOf(150.00));
                }

                @Test
                @DisplayName("Should record evaluation timestamp")
                void shouldRecordEvaluationTimestamp() {
                        // Arrange
                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();

                        when(ruleEvaluator.evaluate(any(Rule.class), any(Stock.class))).thenReturn(result1);
                        Instant before = Instant.now();

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(testStrategy, testStock);
                        Instant after = Instant.now();

                        // Assert
                        assertThat(result.getEvaluatedAt()).isBetween(before, after);
                }

                @Test
                @DisplayName("Should handle single rule strategy")
                void shouldHandleSingleRuleStrategy() {
                        // Arrange
                        Strategy singleRuleStrategy = Strategy.builder()
                                        .id(2L)
                                        .name("Single Rule Strategy")
                                        .description("Only one rule")
                                        .rules(List.of(rule1))
                                        .objective(StrategyObjective.builder()
                                                        .targetType(ObjectiveType.PERCENTAGE)
                                                        .stopLossType(ObjectiveType.PERCENTAGE)
                                                        .targetValue(BigDecimal.valueOf(5.0))
                                                        .stopLossValue(BigDecimal.valueOf(2.0))
                                                        .capitalToRisk(BigDecimal.valueOf(1000.0))
                                                        .description("Single rule objective")
                                                        .build())
                                        .build();

                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();

                        when(ruleEvaluator.evaluate(rule1, testStock)).thenReturn(result1);

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(singleRuleStrategy, testStock);

                        // Assert
                        assertThat(result.isCompliant()).isTrue();
                        assertThat(result.getStrategyName()).isEqualTo("Single Rule Strategy");
                        assertThat(result.getSummary()).contains("1/1 rules passed");
                }
        }

        @Nested
        @DisplayName("Should fail overall when one rule fails")
        class ShouldFailOverallWhenOneRuleFails {

                @Test
                @DisplayName("Should fail overall when one rule fails")
                void shouldFailOverallWhenOneRuleFails() {
                        // Arrange
                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1)
                                        .passed(true)
                                        .justification("PASSED: PRICE (150.00) > SMA20 (145.00)")
                                        .build();

                        RuleResult result2 = RuleResult.builder()
                                        .rule(rule2)
                                        .passed(false)
                                        .justification("FAILED: VOLUME (5000000) < AVG_VOLUME (8000000)")
                                        .build();

                        when(ruleEvaluator.evaluate(rule1, testStock)).thenReturn(result1);
                        when(ruleEvaluator.evaluate(rule2, testStock)).thenReturn(result2);

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert
                        assertThat(result).isNotNull();
                        assertThat(result.isCompliant()).isFalse();
                        assertThat(result.getComplianceRate()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
                        assertThat(result.getSummary()).contains("FAILED");
                        assertThat(result.getSummary()).contains("Volume > Avg Volume");
                }

                @Test
                @DisplayName("Should include evaluation timestamp")
                void shouldIncludeAnalysisTimestamp() {
                        // Arrange
                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();

                        RuleResult result2 = RuleResult.builder()
                                        .rule(rule2)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();

                        when(ruleEvaluator.evaluate(any(Rule.class), any(Stock.class)))
                                        .thenReturn(result1, result2);

                        Instant before = Instant.now();

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(testStrategy, testStock);

                        Instant after = Instant.now();

                        // Assert
                        assertThat(result.getEvaluatedAt()).isNotNull();
                        assertThat(result.getEvaluatedAt()).isBetween(before, after);
                }
        }

        @Nested
        @DisplayName("Validation Tests")
        class ValidationTests {

                @Test
                @DisplayName("Should throw exception when strategy is null")
                void shouldThrowExceptionWhenStrategyIsNull() {
                        assertThatThrownBy(() -> service.evaluateStrategy(null, testStock))
                                        .isInstanceOf(IllegalArgumentException.class)
                                        .hasMessageContaining("Strategy cannot be null");
                }

                @Test
                @DisplayName("Should throw exception when ticker data is null")
                void shouldThrowExceptionWhenTickerDataIsNull() {
                        assertThatThrownBy(() -> service.evaluateStrategy(testStrategy, null))
                                        .isInstanceOf(IllegalArgumentException.class)
                                        .hasMessageContaining("Stock data cannot be null");
                }

                @Test
                @DisplayName("Should throw exception when strategy has invalid configuration")
                void shouldThrowExceptionWhenStrategyHasInvalidConfiguration() {
                        // Arrange
                        Strategy invalidStrategy = Strategy.builder()
                                        .id(99L)
                                        .name("") // Invalid: empty name
                                        .description("Test")
                                        .rules(List.of(rule1))
                                        .build();

                        // Act & Assert
                        assertThatThrownBy(() -> service.evaluateStrategy(invalidStrategy, testStock))
                                        .isInstanceOf(IllegalStateException.class)
                                        .hasMessageContaining("name cannot be null or empty");
                }
        }

        @Nested
        @DisplayName("Summary Generation Tests")
        class SummaryGenerationTests {

                @Test
                @DisplayName("Should generate correct summary for passed strategy")
                void shouldGenerateCorrectSummaryForPassedStrategy() {
                        // Arrange
                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();

                        RuleResult result2 = RuleResult.builder()
                                        .rule(rule2)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();

                        when(ruleEvaluator.evaluate(any(Rule.class), any(Stock.class)))
                                        .thenReturn(result1, result2);

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert
                        assertThat(result.getSummary())
                                        .contains("Momentum Strategy")
                                        .contains("AAPL")
                                        .contains("2/2 rules passed");
                }

                @Test
                @DisplayName("Should list failed rules in summary when strategy fails")
                void shouldListFailedRulesInSummary() {
                        // Arrange
                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1)
                                        .passed(false)
                                        .justification("FAILED")
                                        .build();

                        RuleResult result2 = RuleResult.builder()
                                        .rule(rule2)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();

                        when(ruleEvaluator.evaluate(any(Rule.class), any(Stock.class)))
                                        .thenReturn(result1, result2);

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert
                        assertThat(result.getSummary())
                                        .contains("FAILED")
                                        .contains("1/2 rules passed");
                }
        }

        @Nested
        @DisplayName("Persistence Tests")
        class PersistenceTests {

                @Test
                @DisplayName("Should return evaluation object with correct structure")
                void shouldReturnEvaluationWithCorrectStructure() {
                        // Arrange
                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();

                        RuleResult result2 = RuleResult.builder()
                                        .rule(rule2)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();

                        when(ruleEvaluator.evaluate(any(Rule.class), any(Stock.class)))
                                        .thenReturn(result1, result2);

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert
                        assertThat(result).isNotNull();
                        assertThat(result.getStrategyId()).isEqualTo(1L);
                        assertThat(result.getTicker()).isEqualTo("AAPL");
                        assertThat(result.isLatest()).isTrue();
                }

                @Test
                @DisplayName("Should return evaluation with all required fields")
                void shouldReturnEvaluationWithAllRequiredFields() {
                        // Arrange
                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();

                        RuleResult result2 = RuleResult.builder()
                                        .rule(rule2)
                                        .passed(false)
                                        .justification("FAILED")
                                        .build();

                        when(ruleEvaluator.evaluate(any(Rule.class), any(Stock.class)))
                                        .thenReturn(result1, result2);

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert - Verify all required fields are set
                        assertThat(result.getTicker()).isEqualTo("AAPL");
                        assertThat(result.getStrategyId()).isEqualTo(1L);
                        assertThat(result.getStrategyName()).isEqualTo("Momentum Strategy");
                        assertThat(result.isCompliant()).isFalse();
                        assertThat(result.getComplianceRate()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
                        assertThat(result.getSummary()).isNotEmpty();
                        assertThat(result.getEvaluatedAt()).isNotNull();
                        assertThat(result.getPriceAtEvaluation()).isEqualByComparingTo(BigDecimal.valueOf(150.00));
                        assertThat(result.isLatest()).isTrue();
                }

                @Test
                @DisplayName("Should complete evaluation even if stock has no previous evaluation")
                void shouldCompleteEvaluationWhenStockHasEvaluation() {
                        // Arrange
                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();

                        RuleResult result2 = RuleResult.builder()
                                        .rule(rule2)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();

                        when(ruleEvaluator.evaluate(any(Rule.class), any(Stock.class)))
                                        .thenReturn(result1, result2);

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert - Should return valid evaluation object
                        assertThat(result).isNotNull();
                        assertThat(result.isCompliant()).isTrue();
                        assertThat(result.getTicker()).isEqualTo("AAPL");
                        assertThat(result.getStrategyId()).isEqualTo(1L);
                }
        }

        @Nested
        @DisplayName("Risk Calculation Tests")
        class RiskCalculationTests {

                @Test
                @DisplayName("Should populate risk fields when strategy is compliant")
                void shouldPopulateRiskFieldsWhenCompliant() {
                        // Arrange
                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();
                        RuleResult result2 = RuleResult.builder()
                                        .rule(rule2)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();

                        when(ruleEvaluator.evaluate(any(Rule.class), any(Stock.class)))
                                        .thenReturn(result1, result2);

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert
                        assertThat(result.isCompliant()).isTrue();
                        assertThat(result.getTargetPrice()).isEqualByComparingTo(BigDecimal.valueOf(157.50));
                        assertThat(result.getStopLossPrice()).isEqualByComparingTo(BigDecimal.valueOf(147.00));
                        assertThat(result.getRiskRewardRatio()).isEqualByComparingTo(BigDecimal.valueOf(2.5));
                        assertThat(result.getRecommendedShares()).isEqualTo(94);
                }

                @Test
                @DisplayName("Should leave risk fields null when strategy is not compliant")
                void shouldLeaveRiskFieldsNullWhenNotCompliant() {
                        // Arrange
                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1)
                                        .passed(false)
                                        .justification("FAILED")
                                        .build();
                        RuleResult result2 = RuleResult.builder()
                                        .rule(rule2)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();

                        when(ruleEvaluator.evaluate(any(Rule.class), any(Stock.class)))
                                        .thenReturn(result1, result2);

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert
                        assertThat(result.isCompliant()).isFalse();
                        assertThat(result.getTargetPrice()).isNull();
                        assertThat(result.getStopLossPrice()).isNull();
                        assertThat(result.getRiskRewardRatio()).isNull();
                        assertThat(result.getRecommendedShares()).isNull();
                }

                @Test
                @DisplayName("Should mark as compliant but leave risk fields null when indicator is missing")
                void shouldLeaveRiskFieldsNullOnMissingIndicator() {
                        // Arrange
                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();
                        RuleResult result2 = RuleResult.builder()
                                        .rule(rule2)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();

                        when(ruleEvaluator.evaluate(any(Rule.class), any(Stock.class)))
                                        .thenReturn(result1, result2);
                        when(riskRewardCalculator.calculateTargetPrice(any(), any(), any()))
                                        .thenThrow(new MissingIndicatorException("SMA200 missing"));

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert
                        assertThat(result.isCompliant()).isTrue();
                        assertThat(result.getTargetPrice()).isNull();
                        assertThat(result.getStopLossPrice()).isNull();
                        assertThat(result.getRiskRewardRatio()).isNull();
                        assertThat(result.getRecommendedShares()).isNull();
                        assertThat(result.getSummary()).contains("Risk plan could not be calculated");
                }

                @Test
                @DisplayName("Should mark as compliant but leave risk fields null when stop-loss is above entry price")
                void shouldLeaveRiskFieldsNullOnStopLossAboveEntry() {
                        // Arrange
                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();
                        RuleResult result2 = RuleResult.builder()
                                        .rule(rule2)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();

                        when(ruleEvaluator.evaluate(any(Rule.class), any(Stock.class)))
                                        .thenReturn(result1, result2);
                        when(riskRewardCalculator.calculateTargetPrice(any(), any(), any()))
                                        .thenReturn(BigDecimal.valueOf(160.00));
                        when(riskRewardCalculator.calculateStopLossPrice(any(), any(), any()))
                                        .thenThrow(new IllegalArgumentException(
                                                        "Stop-loss price (155.00) must be less than entry price (150.00) for long positions"));

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert
                        assertThat(result.isCompliant()).isTrue();
                        assertThat(result.getTargetPrice()).isNull();
                        assertThat(result.getStopLossPrice()).isNull();
                        assertThat(result.getRiskRewardRatio()).isNull();
                        assertThat(result.getRecommendedShares()).isNull();
                        assertThat(result.getSummary()).contains("Risk plan could not be calculated");
                        assertThat(result.getSummary()).contains("Stop-loss price");
                }

                @Test
                @DisplayName("Should mark as compliant but leave risk fields null when target price is below entry")
                void shouldLeaveRiskFieldsNullOnTargetBelowEntry() {
                        // Arrange
                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();
                        RuleResult result2 = RuleResult.builder()
                                        .rule(rule2)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();

                        when(ruleEvaluator.evaluate(any(Rule.class), any(Stock.class)))
                                        .thenReturn(result1, result2);
                        when(riskRewardCalculator.calculateTargetPrice(any(), any(), any()))
                                        .thenThrow(new IllegalArgumentException(
                                                        "Target price must be greater than entry price for long positions"));

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert
                        assertThat(result.isCompliant()).isTrue();
                        assertThat(result.getTargetPrice()).isNull();
                        assertThat(result.getStopLossPrice()).isNull();
                        assertThat(result.getRiskRewardRatio()).isNull();
                        assertThat(result.getRecommendedShares()).isNull();
                        assertThat(result.getSummary()).contains("Risk plan could not be calculated");
                        assertThat(result.getSummary()).contains("Target price");
                }

                @Test
                @DisplayName("Should mark as compliant but leave risk fields null when SMA period is unsupported")
                void shouldLeaveRiskFieldsNullOnUnsupportedSmaPeriod() {
                        // Arrange
                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();
                        RuleResult result2 = RuleResult.builder()
                                        .rule(rule2)
                                        .passed(true)
                                        .justification("PASSED")
                                        .build();

                        when(ruleEvaluator.evaluate(any(Rule.class), any(Stock.class)))
                                        .thenReturn(result1, result2);
                        when(riskRewardCalculator.calculateTargetPrice(any(), any(), any()))
                                        .thenThrow(new IllegalArgumentException(
                                                        "SMA period 99 is not supported. Only periods 20, 50, and 200 are allowed."));

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert
                        assertThat(result.isCompliant()).isTrue();
                        assertThat(result.getTargetPrice()).isNull();
                        assertThat(result.getStopLossPrice()).isNull();
                        assertThat(result.getRiskRewardRatio()).isNull();
                        assertThat(result.getRecommendedShares()).isNull();
                        assertThat(result.getSummary()).contains("Risk plan could not be calculated");
                        assertThat(result.getSummary()).contains("SMA period 99");
                }
        }

        @Nested
        @DisplayName("Risk Warning Tests")
        class RiskWarningTests {

                @Test
                @DisplayName("Should populate riskWarnings when objective has high stop-loss percentage")
                void shouldPopulateRiskWarningsForHighStopLossPercentage() {
                        // Arrange — strategy with 25% stop-loss (above 20% threshold)
                        Strategy highRiskStrategy = Strategy.builder()
                                        .id(2L)
                                        .name("High Risk Strategy")
                                        .description("High stop-loss percentage")
                                        .rules(List.of(rule1, rule2))
                                        .objective(StrategyObjective.builder()
                                                        .targetType(ObjectiveType.PERCENTAGE)
                                                        .stopLossType(ObjectiveType.PERCENTAGE)
                                                        .targetValue(BigDecimal.valueOf(5.0))
                                                        .stopLossValue(BigDecimal.valueOf(25.0))
                                                        .capitalToRisk(BigDecimal.valueOf(1000.0))
                                                        .description("High risk objective")
                                                        .build())
                                        .build();

                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1).passed(true).justification("PASSED").build();
                        RuleResult result2 = RuleResult.builder()
                                        .rule(rule2).passed(true).justification("PASSED").build();

                        when(ruleEvaluator.evaluate(any(Rule.class), any(Stock.class)))
                                        .thenReturn(result1, result2);

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(highRiskStrategy, testStock);

                        // Assert
                        assertThat(result.getRiskWarnings()).isNotEmpty();
                        assertThat(result.getRiskWarnings())
                                        .anyMatch(w -> w.contains("Stop-loss percentage"));
                }

                @Test
                @DisplayName("Should populate riskWarnings when objective has high target percentage")
                void shouldPopulateRiskWarningsForHighTargetPercentage() {
                        // Arrange — strategy with 150% target (above 100% threshold)
                        Strategy longTermStrategy = Strategy.builder()
                                        .id(3L)
                                        .name("Long Term Strategy")
                                        .description("Very high target percentage")
                                        .rules(List.of(rule1, rule2))
                                        .objective(StrategyObjective.builder()
                                                        .targetType(ObjectiveType.PERCENTAGE)
                                                        .stopLossType(ObjectiveType.PERCENTAGE)
                                                        .targetValue(BigDecimal.valueOf(150.0))
                                                        .stopLossValue(BigDecimal.valueOf(5.0))
                                                        .capitalToRisk(BigDecimal.valueOf(1000.0))
                                                        .description("Long term objective")
                                                        .build())
                                        .build();

                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1).passed(true).justification("PASSED").build();
                        RuleResult result2 = RuleResult.builder()
                                        .rule(rule2).passed(true).justification("PASSED").build();

                        when(ruleEvaluator.evaluate(any(Rule.class), any(Stock.class)))
                                        .thenReturn(result1, result2);

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(longTermStrategy, testStock);

                        // Assert
                        assertThat(result.getRiskWarnings()).isNotEmpty();
                        assertThat(result.getRiskWarnings())
                                        .anyMatch(w -> w.contains("Target percentage"));
                }

                @Test
                @DisplayName("Should add riskWarning when R:R ratio is below 1.0")
                void shouldAddRiskWarningWhenRatioIsBelowOne() {
                        // Arrange — mock a ratio below 1.0
                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1).passed(true).justification("PASSED").build();
                        RuleResult result2 = RuleResult.builder()
                                        .rule(rule2).passed(true).justification("PASSED").build();

                        when(ruleEvaluator.evaluate(any(Rule.class), any(Stock.class)))
                                        .thenReturn(result1, result2);
                        when(riskRewardCalculator.calculateTargetPrice(any(), any(), any()))
                                        .thenReturn(BigDecimal.valueOf(152.00));
                        when(riskRewardCalculator.calculateStopLossPrice(any(), any(), any()))
                                        .thenReturn(BigDecimal.valueOf(145.00));
                        when(riskRewardCalculator.calculateRiskRewardRatio(any(), any(), any()))
                                        .thenReturn(BigDecimal.valueOf(0.40));
                        when(riskRewardCalculator.calculatePositionSize(any(), any(), any()))
                                        .thenReturn(BigDecimal.valueOf(200));

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert
                        assertThat(result.getRiskWarnings()).isNotEmpty();
                        assertThat(result.getRiskWarnings())
                                        .anyMatch(w -> w.contains("Risk-reward ratio") && w.contains("below"));
                }

                @Test
                @DisplayName("Should have empty riskWarnings when strategy has no warning conditions")
                void shouldHaveEmptyRiskWarningsForNormalStrategy() {
                        // Arrange — standard strategy with normal values
                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1).passed(true).justification("PASSED").build();
                        RuleResult result2 = RuleResult.builder()
                                        .rule(rule2).passed(true).justification("PASSED").build();

                        when(ruleEvaluator.evaluate(any(Rule.class), any(Stock.class)))
                                        .thenReturn(result1, result2);

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert
                        assertThat(result.getRiskWarnings()).isEmpty();
                }

                @Test
                @DisplayName("Should still collect objective warnings even when strategy is non-compliant")
                void shouldCollectObjectiveWarningsEvenWhenNonCompliant() {
                        // Arrange — strategy with high stop-loss but fails evaluation
                        Strategy highRiskStrategy = Strategy.builder()
                                        .id(4L)
                                        .name("Non-Compliant High Risk")
                                        .description("Fails rules but has warning conditions")
                                        .rules(List.of(rule1, rule2))
                                        .objective(StrategyObjective.builder()
                                                        .targetType(ObjectiveType.PERCENTAGE)
                                                        .stopLossType(ObjectiveType.PERCENTAGE)
                                                        .targetValue(BigDecimal.valueOf(5.0))
                                                        .stopLossValue(BigDecimal.valueOf(25.0))
                                                        .capitalToRisk(BigDecimal.valueOf(1000.0))
                                                        .description("High risk objective")
                                                        .build())
                                        .build();

                        RuleResult result1 = RuleResult.builder()
                                        .rule(rule1).passed(false).justification("FAILED").build();
                        RuleResult result2 = RuleResult.builder()
                                        .rule(rule2).passed(true).justification("PASSED").build();

                        when(ruleEvaluator.evaluate(any(Rule.class), any(Stock.class)))
                                        .thenReturn(result1, result2);

                        // Act
                        StrategyEvaluation result = service.evaluateStrategy(highRiskStrategy, testStock);

                        // Assert — warnings should be collected regardless of compliance
                        assertThat(result.isCompliant()).isFalse();
                        assertThat(result.getRiskWarnings()).isNotEmpty();
                        assertThat(result.getRiskWarnings())
                                        .anyMatch(w -> w.contains("Stop-loss percentage"));
                }
        }
}
