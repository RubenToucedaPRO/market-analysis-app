package com.market.analysis.unit.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.RuleResult;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.domain.model.StrategyObjective;
import com.market.analysis.domain.service.EvaluateStrategyService;
import com.market.analysis.domain.service.RuleEvaluator;

/**
 * Integration tests for EvaluateStrategyService with Strategy Objectives and R:R
 * calculation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EvaluateStrategyService with Objectives Tests")
class EvaluateStrategyWithObjectivesTest {

    @Mock
    private RuleEvaluator ruleEvaluator;

    @InjectMocks
    private EvaluateStrategyService service;

    private Stock testStock;
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
    }

    @Nested
    @DisplayName("Strategy with LONG Objective Tests")
    class StrategyWithLongObjectiveTests {

        @Test
        @DisplayName("Should calculate R:R for strategy with LONG objective when all rules pass")
        void shouldCalculateRiskRewardForLongObjective() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(200.00))
                    .stopLossPrice(BigDecimal.valueOf(140.00))
                    .positionType(StrategyObjective.PositionType.LONG)
                    .description("Target: $200, Stop: $140")
                    .build();

            Strategy strategyWithObjective = Strategy.builder()
                    .id(1L)
                    .name("Momentum Strategy with Objective")
                    .description("Bullish momentum with defined R:R")
                    .rules(List.of(rule1, rule2))
                    .objective(objective)
                    .build();

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

            when(ruleEvaluator.evaluate(rule1, testStock)).thenReturn(result1);
            when(ruleEvaluator.evaluate(rule2, testStock)).thenReturn(result2);

            // Act
            StrategyEvaluation result = service.evaluateStrategy(strategyWithObjective, testStock);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isCompliant()).isTrue();
            assertThat(result.getRiskRewardRatio()).isNotNull();
            // Entry: 150, Target: 200, Stop: 140
            // Reward: 200 - 150 = 50
            // Risk: 150 - 140 = 10
            // R:R: 50 / 10 = 5.00
            assertThat(result.getRiskRewardRatio()).isEqualByComparingTo(BigDecimal.valueOf(5.00));
            assertThat(result.getRewardPercentage()).isNotNull();
            assertThat(result.getRiskPercentage()).isNotNull();
            assertThat(result.getSummary()).contains("Risk:Reward ratio: 1:5.00");

            verify(ruleEvaluator, times(2)).evaluate(any(Rule.class), any(Stock.class));
        }

        @Test
        @DisplayName("Should handle strategy with LONG objective when some rules fail")
        void shouldHandleLongObjectiveWithFailedRules() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(180.00))
                    .stopLossPrice(BigDecimal.valueOf(145.00))
                    .positionType(StrategyObjective.PositionType.LONG)
                    .build();

            Strategy strategyWithObjective = Strategy.builder()
                    .id(1L)
                    .name("Conservative Strategy")
                    .description("Lower risk with defined R:R")
                    .rules(List.of(rule1, rule2))
                    .objective(objective)
                    .build();

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
            StrategyEvaluation result = service.evaluateStrategy(strategyWithObjective, testStock);

            // Assert
            assertThat(result.isCompliant()).isFalse();
            assertThat(result.getRiskRewardRatio()).isNotNull();
            // Entry: 150, Target: 180, Stop: 145
            // Reward: 180 - 150 = 30
            // Risk: 150 - 145 = 5
            // R:R: 30 / 5 = 6.00
            assertThat(result.getRiskRewardRatio()).isEqualByComparingTo(BigDecimal.valueOf(6.00));
            assertThat(result.getSummary()).contains("FAILED");
            assertThat(result.getSummary()).contains("Risk:Reward ratio: 1:6.00");
        }

        @Test
        @DisplayName("Should calculate reward and risk percentages correctly for LONG")
        void shouldCalculatePercentagesForLong() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(165.00)) // 10% gain
                    .stopLossPrice(BigDecimal.valueOf(142.50)) // 5% loss
                    .positionType(StrategyObjective.PositionType.LONG)
                    .build();

            Strategy strategyWithObjective = Strategy.builder()
                    .id(1L)
                    .name("2:1 Strategy")
                    .description("10% reward, 5% risk")
                    .rules(List.of(rule1))
                    .objective(objective)
                    .build();

            RuleResult result1 = RuleResult.builder()
                    .rule(rule1)
                    .passed(true)
                    .justification("PASSED")
                    .build();

            when(ruleEvaluator.evaluate(rule1, testStock)).thenReturn(result1);

            // Act
            StrategyEvaluation result = service.evaluateStrategy(strategyWithObjective, testStock);

            // Assert
            assertThat(result.getRiskRewardRatio()).isEqualByComparingTo(BigDecimal.valueOf(2.00));
            assertThat(result.getRewardPercentage()).isEqualByComparingTo(BigDecimal.valueOf(10.00));
            assertThat(result.getRiskPercentage()).isEqualByComparingTo(BigDecimal.valueOf(5.00));
        }
    }

    @Nested
    @DisplayName("Strategy with SHORT Objective Tests")
    class StrategyWithShortObjectiveTests {

        @Test
        @DisplayName("Should calculate R:R for strategy with SHORT objective")
        void shouldCalculateRiskRewardForShortObjective() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(100.00))
                    .stopLossPrice(BigDecimal.valueOf(160.00))
                    .positionType(StrategyObjective.PositionType.SHORT)
                    .description("Short position with defined R:R")
                    .build();

            Strategy strategyWithObjective = Strategy.builder()
                    .id(1L)
                    .name("Bearish Strategy")
                    .description("Short momentum with defined R:R")
                    .rules(List.of(rule1, rule2))
                    .objective(objective)
                    .build();

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

            when(ruleEvaluator.evaluate(rule1, testStock)).thenReturn(result1);
            when(ruleEvaluator.evaluate(rule2, testStock)).thenReturn(result2);

            // Act
            StrategyEvaluation result = service.evaluateStrategy(strategyWithObjective, testStock);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isCompliant()).isTrue();
            assertThat(result.getRiskRewardRatio()).isNotNull();
            // Entry: 150, Target: 100, Stop: 160
            // Reward: 150 - 100 = 50
            // Risk: 160 - 150 = 10
            // R:R: 50 / 10 = 5.00
            assertThat(result.getRiskRewardRatio()).isEqualByComparingTo(BigDecimal.valueOf(5.00));
        }

        @Test
        @DisplayName("Should calculate reward and risk percentages correctly for SHORT")
        void shouldCalculatePercentagesForShort() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(135.00)) // 10% gain on short
                    .stopLossPrice(BigDecimal.valueOf(157.50)) // 5% loss on short
                    .positionType(StrategyObjective.PositionType.SHORT)
                    .build();

            Strategy strategyWithObjective = Strategy.builder()
                    .id(1L)
                    .name("2:1 Short Strategy")
                    .description("10% reward, 5% risk on short")
                    .rules(List.of(rule1))
                    .objective(objective)
                    .build();

            RuleResult result1 = RuleResult.builder()
                    .rule(rule1)
                    .passed(true)
                    .justification("PASSED")
                    .build();

            when(ruleEvaluator.evaluate(rule1, testStock)).thenReturn(result1);

            // Act
            StrategyEvaluation result = service.evaluateStrategy(strategyWithObjective, testStock);

            // Assert
            assertThat(result.getRiskRewardRatio()).isEqualByComparingTo(BigDecimal.valueOf(2.00));
            assertThat(result.getRewardPercentage()).isEqualByComparingTo(BigDecimal.valueOf(10.00));
            assertThat(result.getRiskPercentage()).isEqualByComparingTo(BigDecimal.valueOf(5.00));
        }
    }

    @Nested
    @DisplayName("Strategy without Objective Tests")
    class StrategyWithoutObjectiveTests {

        @Test
        @DisplayName("Should handle strategy without objective (backward compatibility)")
        void shouldHandleStrategyWithoutObjective() {
            // Arrange
            Strategy strategyWithoutObjective = Strategy.builder()
                    .id(1L)
                    .name("Simple Strategy")
                    .description("No objective defined")
                    .rules(List.of(rule1, rule2))
                    .build();

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

            when(ruleEvaluator.evaluate(rule1, testStock)).thenReturn(result1);
            when(ruleEvaluator.evaluate(rule2, testStock)).thenReturn(result2);

            // Act
            StrategyEvaluation result = service.evaluateStrategy(strategyWithoutObjective, testStock);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isCompliant()).isTrue();
            assertThat(result.getRiskRewardRatio()).isNull();
            assertThat(result.getRewardPercentage()).isNull();
            assertThat(result.getRiskPercentage()).isNull();
            assertThat(result.getSummary()).doesNotContain("Risk:Reward");
        }
    }

    @Nested
    @DisplayName("Invalid Objective Tests")
    class InvalidObjectiveTests {

        @Test
        @DisplayName("Should handle invalid objective gracefully")
        void shouldHandleInvalidObjectiveGracefully() {
            // Arrange - objective with target below entry for LONG (invalid)
            StrategyObjective invalidObjective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(140.00)) // Below entry price
                    .stopLossPrice(BigDecimal.valueOf(130.00))
                    .positionType(StrategyObjective.PositionType.LONG)
                    .build();

            Strategy strategyWithInvalidObjective = Strategy.builder()
                    .id(1L)
                    .name("Invalid Objective Strategy")
                    .description("Has invalid objective")
                    .rules(List.of(rule1))
                    .objective(invalidObjective)
                    .build();

            RuleResult result1 = RuleResult.builder()
                    .rule(rule1)
                    .passed(true)
                    .justification("PASSED")
                    .build();

            when(ruleEvaluator.evaluate(rule1, testStock)).thenReturn(result1);

            // Act
            StrategyEvaluation result = service.evaluateStrategy(strategyWithInvalidObjective, testStock);

            // Assert - Strategy evaluation should still complete
            assertThat(result).isNotNull();
            assertThat(result.isCompliant()).isTrue();
            assertThat(result.getRiskRewardRatio()).isNull(); // R:R not calculated due to invalid objective
            assertThat(result.getRewardPercentage()).isNull();
            assertThat(result.getRiskPercentage()).isNull();
        }
    }
}
