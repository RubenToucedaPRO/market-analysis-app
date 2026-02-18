package com.market.analysis.unit.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StrategyObjective;
import com.market.analysis.domain.model.StrategyObjective.ObjectiveType;

/**
 * Unit tests for StrategyObjective value object.
 */
@DisplayName("StrategyObjective Tests")
class StrategyObjectiveTest {

    private Stock testStock;

    @BeforeEach
    void setUp() {
        testStock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.00))
                .sma20(BigDecimal.valueOf(145.00))
                .sma50(BigDecimal.valueOf(140.00))
                .sma200(BigDecimal.valueOf(130.00))
                .volume(10000000L)
                .averageVolume(8000000L)
                .lastUpdated(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("Fixed Price Objective Tests")
    class FixedPriceObjectiveTests {

        @Test
        @DisplayName("Should calculate R:R with fixed price target and stop loss")
        void shouldCalculateRiskRewardWithFixedPrices() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.FIXED_PRICE)
                    .targetValue(BigDecimal.valueOf(200.00))
                    .stopLossType(ObjectiveType.FIXED_PRICE)
                    .stopLossValue(BigDecimal.valueOf(140.00))
                    .description("Fixed price target")
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act
            BigDecimal riskReward = objective.calculateRiskRewardRatio(entryPrice, testStock);

            // Assert
            // Reward = 200 - 150 = 50
            // Risk = 150 - 140 = 10
            // R:R = 50 / 10 = 5.00
            assertThat(riskReward).isEqualByComparingTo(BigDecimal.valueOf(5.00));
        }

        @Test
        @DisplayName("Should calculate share quantity with capital to risk")
        void shouldCalculateShareQuantityWithCapitalToRisk() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.FIXED_PRICE)
                    .targetValue(BigDecimal.valueOf(200.00))
                    .stopLossType(ObjectiveType.FIXED_PRICE)
                    .stopLossValue(BigDecimal.valueOf(140.00))
                    .capitalToRisk(BigDecimal.valueOf(1000.00))
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act
            Integer shareQuantity = objective.calculateShareQuantity(entryPrice, testStock);

            // Assert
            // Risk per share = 150 - 140 = 10
            // Shares = 1000 / 10 = 100
            assertThat(shareQuantity).isEqualTo(100);
        }

        @Test
        @DisplayName("Should return null share quantity when capital to risk not specified")
        void shouldReturnNullShareQuantityWhenCapitalNotSpecified() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.FIXED_PRICE)
                    .targetValue(BigDecimal.valueOf(200.00))
                    .stopLossType(ObjectiveType.FIXED_PRICE)
                    .stopLossValue(BigDecimal.valueOf(140.00))
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act
            Integer shareQuantity = objective.calculateShareQuantity(entryPrice, testStock);

            // Assert
            assertThat(shareQuantity).isNull();
        }
    }

    @Nested
    @DisplayName("Percentage Objective Tests")
    class PercentageObjectiveTests {

        @Test
        @DisplayName("Should calculate R:R with percentage target and stop loss")
        void shouldCalculateRiskRewardWithPercentages() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.PERCENTAGE)
                    .targetValue(BigDecimal.valueOf(10.00)) // 10% profit
                    .stopLossType(ObjectiveType.PERCENTAGE)
                    .stopLossValue(BigDecimal.valueOf(5.00)) // 5% loss
                    .description("Percentage-based objective")
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(100.00);

            // Act
            BigDecimal riskReward = objective.calculateRiskRewardRatio(entryPrice, testStock);

            // Assert
            // Target = 100 + (100 * 0.10) = 110
            // Stop Loss = 100 - (100 * 0.05) = 95
            // Reward = 110 - 100 = 10
            // Risk = 100 - 95 = 5
            // R:R = 10 / 5 = 2.00
            assertThat(riskReward).isEqualByComparingTo(BigDecimal.valueOf(2.00));
        }

        @Test
        @DisplayName("Should resolve target price from percentage")
        void shouldResolveTargetPriceFromPercentage() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.PERCENTAGE)
                    .targetValue(BigDecimal.valueOf(20.00)) // 20% profit
                    .stopLossType(ObjectiveType.FIXED_PRICE)
                    .stopLossValue(BigDecimal.valueOf(140.00))
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act
            BigDecimal targetPrice = objective.resolveTargetPrice(entryPrice, testStock);

            // Assert
            // 150 + (150 * 0.20) = 180
            assertThat(targetPrice).isEqualByComparingTo(BigDecimal.valueOf(180.00));
        }

        @Test
        @DisplayName("Should resolve stop loss price from percentage")
        void shouldResolveStopLossPriceFromPercentage() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.FIXED_PRICE)
                    .targetValue(BigDecimal.valueOf(200.00))
                    .stopLossType(ObjectiveType.PERCENTAGE)
                    .stopLossValue(BigDecimal.valueOf(10.00)) // 10% loss
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act
            BigDecimal stopLossPrice = objective.resolveStopLossPrice(entryPrice, testStock);

            // Assert
            // 150 - (150 * 0.10) = 135
            assertThat(stopLossPrice).isEqualByComparingTo(BigDecimal.valueOf(135.00));
        }
    }

    @Nested
    @DisplayName("SMA Objective Tests")
    class SmaObjectiveTests {

        @Test
        @DisplayName("Should calculate R:R with SMA target and stop loss")
        void shouldCalculateRiskRewardWithSma() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.SMA)
                    .targetValue(BigDecimal.valueOf(200)) // SMA200
                    .stopLossType(ObjectiveType.SMA)
                    .stopLossValue(BigDecimal.valueOf(20)) // SMA20
                    .description("SMA-based objective")
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act & Assert
            // Target = SMA200 = 130 (below entry, will fail validation)
            assertThatThrownBy(() -> objective.calculateRiskRewardRatio(entryPrice, testStock))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("must be greater than entry price");
        }

        @Test
        @DisplayName("Should resolve target from SMA50")
        void shouldResolveTargetFromSma() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.SMA)
                    .targetValue(BigDecimal.valueOf(50)) // SMA50
                    .stopLossType(ObjectiveType.FIXED_PRICE)
                    .stopLossValue(BigDecimal.valueOf(130.00))
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(135.00);

            // Act
            BigDecimal targetPrice = objective.resolveTargetPrice(entryPrice, testStock);

            // Assert
            assertThat(targetPrice).isEqualByComparingTo(BigDecimal.valueOf(140.00)); // SMA50 from testStock
        }

        @Test
        @DisplayName("Should fail for unsupported SMA period")
        void shouldFailForUnsupportedSmaPeriod() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.SMA)
                    .targetValue(BigDecimal.valueOf(100)) // SMA100 not supported
                    .stopLossType(ObjectiveType.FIXED_PRICE)
                    .stopLossValue(BigDecimal.valueOf(140.00))
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act & Assert
            assertThatThrownBy(() -> objective.resolveTargetPrice(entryPrice, testStock))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("SMA period 100 not supported");
        }
    }

    @Nested
    @DisplayName("Mixed Objective Tests")
    class MixedObjectiveTests {

        @Test
        @DisplayName("Should calculate R:R with mixed target and stop loss types")
        void shouldCalculateRiskRewardWithMixedTypes() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.PERCENTAGE)
                    .targetValue(BigDecimal.valueOf(15.00)) // 15% profit
                    .stopLossType(ObjectiveType.SMA)
                    .stopLossValue(BigDecimal.valueOf(20)) // SMA20
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act
            BigDecimal riskReward = objective.calculateRiskRewardRatio(entryPrice, testStock);

            // Assert
            // Target = 150 + (150 * 0.15) = 172.50
            // Stop Loss = SMA20 = 145
            // Reward = 172.50 - 150 = 22.50
            // Risk = 150 - 145 = 5
            // R:R = 22.50 / 5 = 4.50
            assertThat(riskReward).isEqualByComparingTo(BigDecimal.valueOf(4.50));
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should fail when target type is null")
        void shouldFailWhenTargetTypeIsNull() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(null)
                    .targetValue(BigDecimal.valueOf(200.00))
                    .stopLossType(ObjectiveType.FIXED_PRICE)
                    .stopLossValue(BigDecimal.valueOf(140.00))
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> objective.validateConsistency())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Target type cannot be null");
        }

        @Test
        @DisplayName("Should fail when target value is negative")
        void shouldFailWhenTargetValueIsNegative() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.FIXED_PRICE)
                    .targetValue(BigDecimal.valueOf(-200.00))
                    .stopLossType(ObjectiveType.FIXED_PRICE)
                    .stopLossValue(BigDecimal.valueOf(140.00))
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> objective.validateConsistency())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Target value must be positive");
        }

        @Test
        @DisplayName("Should fail when resolved target is below entry")
        void shouldFailWhenTargetBelowEntry() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.FIXED_PRICE)
                    .targetValue(BigDecimal.valueOf(140.00))
                    .stopLossType(ObjectiveType.FIXED_PRICE)
                    .stopLossValue(BigDecimal.valueOf(130.00))
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act & Assert
            assertThatThrownBy(() -> objective.calculateRiskRewardRatio(entryPrice, testStock))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("must be greater than entry price");
        }

        @Test
        @DisplayName("Should fail when resolved stop loss is above entry")
        void shouldFailWhenStopLossAboveEntry() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.FIXED_PRICE)
                    .targetValue(BigDecimal.valueOf(200.00))
                    .stopLossType(ObjectiveType.FIXED_PRICE)
                    .stopLossValue(BigDecimal.valueOf(160.00))
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act & Assert
            assertThatThrownBy(() -> objective.calculateRiskRewardRatio(entryPrice, testStock))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("must be less than entry price");
        }
    }

    @Nested
    @DisplayName("Calculation Tests")
    class CalculationTests {

        @Test
        @DisplayName("Should calculate reward and risk percentages correctly")
        void shouldCalculatePercentagesCorrectly() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.FIXED_PRICE)
                    .targetValue(BigDecimal.valueOf(165.00)) // 10% gain
                    .stopLossType(ObjectiveType.FIXED_PRICE)
                    .stopLossValue(BigDecimal.valueOf(142.50)) // 5% loss
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act
            BigDecimal rewardPct = objective.calculateRewardPercentage(entryPrice, testStock);
            BigDecimal riskPct = objective.calculateRiskPercentage(entryPrice, testStock);

            // Assert
            assertThat(rewardPct).isEqualByComparingTo(BigDecimal.valueOf(10.00));
            assertThat(riskPct).isEqualByComparingTo(BigDecimal.valueOf(5.00));
        }

        @Test
        @DisplayName("Should round share quantity down to whole number")
        void shouldRoundShareQuantityDown() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.FIXED_PRICE)
                    .targetValue(BigDecimal.valueOf(200.00))
                    .stopLossType(ObjectiveType.FIXED_PRICE)
                    .stopLossValue(BigDecimal.valueOf(143.00))
                    .capitalToRisk(BigDecimal.valueOf(100.00))
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act
            Integer shareQuantity = objective.calculateShareQuantity(entryPrice, testStock);

            // Assert
            // Risk per share = 150 - 143 = 7
            // Shares = 100 / 7 = 14.28... → 14 (rounded down)
            assertThat(shareQuantity).isEqualTo(14);
        }
    }
}
