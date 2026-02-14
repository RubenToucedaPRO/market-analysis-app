package com.market.analysis.unit.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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

import com.market.analysis.application.usecase.EvaluateStrategyService;
import com.market.analysis.domain.model.AnalysisResult;
import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.RuleResult;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.port.out.StrategyEvaluationRepository;
import com.market.analysis.domain.service.RuleEvaluator;

@ExtendWith(MockitoExtension.class)
@DisplayName("EvaluateStrategyService Tests")
class EvaluateStrategyServiceTest {

        @Mock
        private RuleEvaluator ruleEvaluator;

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
                testStock = Stock.builder()
                                .ticker("AAPL")
                                .currentPrice(BigDecimal.valueOf(150.00))
                                .sma20(BigDecimal.valueOf(145.00))
                                .sma50(BigDecimal.valueOf(140.00))
                                .volume(10000000L)
                                .averageVolume(8000000L)
                                .lastUpdated(Instant.now())
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
                                .build();
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
                        AnalysisResult result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert
                        assertThat(result).isNotNull();
                        assertThat(result.isOverallPassed()).isTrue();
                        assertThat(result.getTicker()).isEqualTo("AAPL");
                        assertThat(result.getStrategy()).isEqualTo(testStrategy);
                        assertThat(result.getRuleResults()).hasSize(2);
                        assertThat(result.getCalculatedMetrics()).containsEntry("totalRules", 2L);
                        assertThat(result.getCalculatedMetrics()).containsEntry("passedRules", 2L);
                        assertThat(result.getCalculatedMetrics()).containsEntry("failedRules", 0L);
                        assertThat(result.calculateComplianceRate()).isEqualByComparingTo(BigDecimal.valueOf(100.00));

                        verify(ruleEvaluator, times(2)).evaluate(any(Rule.class), any(Stock.class));
                }

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
                        AnalysisResult result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert
                        assertThat(result).isNotNull();
                        assertThat(result.isOverallPassed()).isFalse();
                        assertThat(result.getRuleResults()).hasSize(2);
                        assertThat(result.getCalculatedMetrics()).containsEntry("passedRules", 1L);
                        assertThat(result.getCalculatedMetrics()).containsEntry("failedRules", 1L);
                        assertThat(result.calculateComplianceRate()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
                        assertThat(result.getSummary()).contains("FAILED");
                        assertThat(result.getSummary()).contains("Volume > Avg Volume");
                }

                @Test
                @DisplayName("Should include analysis timestamp")
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
                        AnalysisResult result = service.evaluateStrategy(testStrategy, testStock);

                        Instant after = Instant.now();

                        // Assert
                        assertThat(result.getAnalysisTimestamp()).isNotNull();
                        assertThat(result.getAnalysisTimestamp()).isBetween(before, after);
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
                        AnalysisResult result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert
                        assertThat(result.getSummary())
                                        .contains("Momentum Strategy")
                                        .contains("AAPL")
                                        .contains("PASSED")
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
                        AnalysisResult result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert
                        assertThat(result.getSummary())
                                        .contains("FAILED")
                                        .contains("1/2 rules passed")
                                        .contains("Failed rules:")
                                        .contains("Price > SMA20");
                }
        }

        @Nested
        @DisplayName("Persistence Tests")
        class PersistenceTests {

                @Test
                @DisplayName("Should persist evaluation when strategy evaluation succeeds")
                void shouldPersistEvaluationWhenStrategyEvaluationSucceeds() {
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
                        AnalysisResult result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert
                        assertThat(result.isOverallPassed()).isTrue();
                        verify(strategyEvaluationRepository, times(1)).save(any(), any());
                }

                @Test
                @DisplayName("Should persist evaluation with correct data")
                void shouldPersistEvaluationWithCorrectData() {
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
                        AnalysisResult result = service.evaluateStrategy(testStrategy, testStock);

                        // Assert - Verify that save was called with correct data
                        verify(strategyEvaluationRepository)
                                        .save(argThat(evaluation -> evaluation.getTicker().equals("AAPL") &&
                                                        evaluation.getStrategyId().equals(1L) &&
                                                        !evaluation.isCompliant() &&
                                                        evaluation.getComplianceRate()
                                                                        .compareTo(BigDecimal.valueOf(50.00)) == 0
                                                        &&
                                                        evaluation.isLatest()),
                                                        any(Stock.class));
                        assertNotNull(result);
                }

                @Test
                @DisplayName("Should not fail evaluation if persistence fails")
                void shouldNotFailEvaluationIfPersistenceFails() {
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

                        // Make repository throw exception
                        when(strategyEvaluationRepository.save(any(), any()))
                                        .thenThrow(new RuntimeException("Database error"));

                        // Act & Assert - Should not throw, just log error
                        assertThat(service.evaluateStrategy(testStrategy, testStock))
                                        .isNotNull()
                                        .extracting(AnalysisResult::isOverallPassed)
                                        .isEqualTo(true);
                }
        }
}
