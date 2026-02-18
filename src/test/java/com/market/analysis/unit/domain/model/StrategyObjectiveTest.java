package com.market.analysis.unit.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.StrategyObjective;
import com.market.analysis.domain.model.StrategyObjective.PositionType;

/**
 * Unit tests for StrategyObjective value object.
 */
@DisplayName("StrategyObjective Tests")
class StrategyObjectiveTest {

    @Nested
    @DisplayName("LONG Position Tests")
    class LongPositionTests {

        @Test
        @DisplayName("Should create valid LONG objective")
        void shouldCreateValidLongObjective() {
            // Arrange & Act
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(200.00))
                    .stopLossPrice(BigDecimal.valueOf(140.00))
                    .positionType(PositionType.LONG)
                    .description("Buy signal with 2:1 R:R")
                    .build();

            // Assert
            assertThat(objective).isNotNull();
            assertThat(objective.getTargetPrice()).isEqualByComparingTo(BigDecimal.valueOf(200.00));
            assertThat(objective.getStopLossPrice()).isEqualByComparingTo(BigDecimal.valueOf(140.00));
            assertThat(objective.getPositionType()).isEqualTo(PositionType.LONG);
            assertThat(objective.getDescription()).isEqualTo("Buy signal with 2:1 R:R");
        }

        @Test
        @DisplayName("Should calculate R:R correctly for LONG position")
        void shouldCalculateRiskRewardForLong() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(200.00))
                    .stopLossPrice(BigDecimal.valueOf(140.00))
                    .positionType(PositionType.LONG)
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act
            BigDecimal riskReward = objective.calculateRiskRewardRatio(entryPrice);

            // Assert
            // Reward = 200 - 150 = 50
            // Risk = 150 - 140 = 10
            // R:R = 50 / 10 = 5.00
            assertThat(riskReward).isEqualByComparingTo(BigDecimal.valueOf(5.00));
        }

        @Test
        @DisplayName("Should calculate reward percentage for LONG position")
        void shouldCalculateRewardPercentageForLong() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(165.00))
                    .stopLossPrice(BigDecimal.valueOf(145.00))
                    .positionType(PositionType.LONG)
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act
            BigDecimal rewardPercentage = objective.calculateRewardPercentage(entryPrice);

            // Assert
            // Reward = 165 - 150 = 15
            // Percentage = (15 / 150) * 100 = 10%
            assertThat(rewardPercentage).isEqualByComparingTo(BigDecimal.valueOf(10.00));
        }

        @Test
        @DisplayName("Should calculate risk percentage for LONG position")
        void shouldCalculateRiskPercentageForLong() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(165.00))
                    .stopLossPrice(BigDecimal.valueOf(142.50))
                    .positionType(PositionType.LONG)
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act
            BigDecimal riskPercentage = objective.calculateRiskPercentage(entryPrice);

            // Assert
            // Risk = 150 - 142.50 = 7.50
            // Percentage = (7.50 / 150) * 100 = 5%
            assertThat(riskPercentage).isEqualByComparingTo(BigDecimal.valueOf(5.00));
        }

        @Test
        @DisplayName("Should fail validation when target price is less than entry for LONG")
        void shouldFailValidationWhenTargetBelowEntry() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(140.00)) // Below entry
                    .stopLossPrice(BigDecimal.valueOf(130.00))
                    .positionType(PositionType.LONG)
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act & Assert
            assertThatThrownBy(() -> objective.validateConsistency(entryPrice))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("target price")
                    .hasMessageContaining("must be greater than entry price");
        }

        @Test
        @DisplayName("Should fail validation when stop loss is above entry for LONG")
        void shouldFailValidationWhenStopLossAboveEntry() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(200.00))
                    .stopLossPrice(BigDecimal.valueOf(160.00)) // Above entry
                    .positionType(PositionType.LONG)
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act & Assert
            assertThatThrownBy(() -> objective.validateConsistency(entryPrice))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("stop loss price")
                    .hasMessageContaining("must be less than entry price");
        }
    }

    @Nested
    @DisplayName("SHORT Position Tests")
    class ShortPositionTests {

        @Test
        @DisplayName("Should create valid SHORT objective")
        void shouldCreateValidShortObjective() {
            // Arrange & Act
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(100.00))
                    .stopLossPrice(BigDecimal.valueOf(160.00))
                    .positionType(PositionType.SHORT)
                    .description("Sell signal with 2:1 R:R")
                    .build();

            // Assert
            assertThat(objective).isNotNull();
            assertThat(objective.getTargetPrice()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
            assertThat(objective.getStopLossPrice()).isEqualByComparingTo(BigDecimal.valueOf(160.00));
            assertThat(objective.getPositionType()).isEqualTo(PositionType.SHORT);
        }

        @Test
        @DisplayName("Should calculate R:R correctly for SHORT position")
        void shouldCalculateRiskRewardForShort() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(100.00))
                    .stopLossPrice(BigDecimal.valueOf(160.00))
                    .positionType(PositionType.SHORT)
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act
            BigDecimal riskReward = objective.calculateRiskRewardRatio(entryPrice);

            // Assert
            // Reward = 150 - 100 = 50
            // Risk = 160 - 150 = 10
            // R:R = 50 / 10 = 5.00
            assertThat(riskReward).isEqualByComparingTo(BigDecimal.valueOf(5.00));
        }

        @Test
        @DisplayName("Should calculate reward percentage for SHORT position")
        void shouldCalculateRewardPercentageForShort() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(135.00))
                    .stopLossPrice(BigDecimal.valueOf(155.00))
                    .positionType(PositionType.SHORT)
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act
            BigDecimal rewardPercentage = objective.calculateRewardPercentage(entryPrice);

            // Assert
            // Reward = 150 - 135 = 15
            // Percentage = (15 / 150) * 100 = 10%
            assertThat(rewardPercentage).isEqualByComparingTo(BigDecimal.valueOf(10.00));
        }

        @Test
        @DisplayName("Should calculate risk percentage for SHORT position")
        void shouldCalculateRiskPercentageForShort() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(135.00))
                    .stopLossPrice(BigDecimal.valueOf(157.50))
                    .positionType(PositionType.SHORT)
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act
            BigDecimal riskPercentage = objective.calculateRiskPercentage(entryPrice);

            // Assert
            // Risk = 157.50 - 150 = 7.50
            // Percentage = (7.50 / 150) * 100 = 5%
            assertThat(riskPercentage).isEqualByComparingTo(BigDecimal.valueOf(5.00));
        }

        @Test
        @DisplayName("Should fail validation when target price is above entry for SHORT")
        void shouldFailValidationWhenTargetAboveEntry() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(160.00)) // Above entry
                    .stopLossPrice(BigDecimal.valueOf(170.00))
                    .positionType(PositionType.SHORT)
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act & Assert
            assertThatThrownBy(() -> objective.validateConsistency(entryPrice))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("target price")
                    .hasMessageContaining("must be less than entry price");
        }

        @Test
        @DisplayName("Should fail validation when stop loss is below entry for SHORT")
        void shouldFailValidationWhenStopLossBelowEntry() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(100.00))
                    .stopLossPrice(BigDecimal.valueOf(140.00)) // Below entry
                    .positionType(PositionType.SHORT)
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act & Assert
            assertThatThrownBy(() -> objective.validateConsistency(entryPrice))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("stop loss price")
                    .hasMessageContaining("must be greater than entry price");
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should fail when target price is null")
        void shouldFailWhenTargetPriceIsNull() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(null)
                    .stopLossPrice(BigDecimal.valueOf(140.00))
                    .positionType(PositionType.LONG)
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> objective.validateConsistency(BigDecimal.valueOf(150.00)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Target price cannot be null");
        }

        @Test
        @DisplayName("Should fail when stop loss price is null")
        void shouldFailWhenStopLossPriceIsNull() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(200.00))
                    .stopLossPrice(null)
                    .positionType(PositionType.LONG)
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> objective.validateConsistency(BigDecimal.valueOf(150.00)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Stop loss price cannot be null");
        }

        @Test
        @DisplayName("Should fail when entry price is null")
        void shouldFailWhenEntryPriceIsNull() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(200.00))
                    .stopLossPrice(BigDecimal.valueOf(140.00))
                    .positionType(PositionType.LONG)
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> objective.validateConsistency(null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Entry price cannot be null");
        }

        @Test
        @DisplayName("Should fail when position type is null")
        void shouldFailWhenPositionTypeIsNull() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(200.00))
                    .stopLossPrice(BigDecimal.valueOf(140.00))
                    .positionType(null)
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> objective.validateConsistency(BigDecimal.valueOf(150.00)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Position type cannot be null");
        }

        @Test
        @DisplayName("Should fail when target and stop loss are the same")
        void shouldFailWhenTargetEqualsStopLoss() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(150.00))
                    .stopLossPrice(BigDecimal.valueOf(150.00))
                    .positionType(PositionType.LONG)
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> objective.validateConsistency(BigDecimal.valueOf(150.00)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Target price and stop loss price cannot be the same");
        }

        @Test
        @DisplayName("Should fail R:R calculation when entry price is zero")
        void shouldFailRiskRewardCalculationWhenEntryPriceIsZero() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(200.00))
                    .stopLossPrice(BigDecimal.valueOf(140.00))
                    .positionType(PositionType.LONG)
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> objective.calculateRewardPercentage(BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Entry price must be greater than zero");
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when all fields match")
        void shouldBeEqualWhenFieldsMatch() {
            // Arrange
            StrategyObjective objective1 = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(200.00))
                    .stopLossPrice(BigDecimal.valueOf(140.00))
                    .positionType(PositionType.LONG)
                    .description("Test")
                    .build();

            StrategyObjective objective2 = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(200.00))
                    .stopLossPrice(BigDecimal.valueOf(140.00))
                    .positionType(PositionType.LONG)
                    .description("Different description")
                    .build();

            // Act & Assert
            assertThat(objective1).isEqualTo(objective2);
            assertThat(objective1.hashCode()).isEqualTo(objective2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when target price differs")
        void shouldNotBeEqualWhenTargetPriceDiffers() {
            // Arrange
            StrategyObjective objective1 = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(200.00))
                    .stopLossPrice(BigDecimal.valueOf(140.00))
                    .positionType(PositionType.LONG)
                    .build();

            StrategyObjective objective2 = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(210.00))
                    .stopLossPrice(BigDecimal.valueOf(140.00))
                    .positionType(PositionType.LONG)
                    .build();

            // Act & Assert
            assertThat(objective1).isNotEqualTo(objective2);
        }

        @Test
        @DisplayName("Should not be equal when stop loss differs")
        void shouldNotBeEqualWhenStopLossDiffers() {
            // Arrange
            StrategyObjective objective1 = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(200.00))
                    .stopLossPrice(BigDecimal.valueOf(140.00))
                    .positionType(PositionType.LONG)
                    .build();

            StrategyObjective objective2 = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(200.00))
                    .stopLossPrice(BigDecimal.valueOf(130.00))
                    .positionType(PositionType.LONG)
                    .build();

            // Act & Assert
            assertThat(objective1).isNotEqualTo(objective2);
        }

        @Test
        @DisplayName("Should not be equal when position type differs")
        void shouldNotBeEqualWhenPositionTypeDiffers() {
            // Arrange
            StrategyObjective objective1 = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(200.00))
                    .stopLossPrice(BigDecimal.valueOf(140.00))
                    .positionType(PositionType.LONG)
                    .build();

            StrategyObjective objective2 = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(200.00))
                    .stopLossPrice(BigDecimal.valueOf(140.00))
                    .positionType(PositionType.SHORT)
                    .build();

            // Act & Assert
            assertThat(objective1).isNotEqualTo(objective2);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very small R:R ratio")
        void shouldHandleVerySmallRiskReward() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(151.00))
                    .stopLossPrice(BigDecimal.valueOf(140.00))
                    .positionType(PositionType.LONG)
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act
            BigDecimal riskReward = objective.calculateRiskRewardRatio(entryPrice);

            // Assert
            // Reward = 151 - 150 = 1
            // Risk = 150 - 140 = 10
            // R:R = 1 / 10 = 0.10
            assertThat(riskReward).isEqualByComparingTo(BigDecimal.valueOf(0.10));
        }

        @Test
        @DisplayName("Should handle large R:R ratio")
        void shouldHandleLargeRiskReward() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(250.00))
                    .stopLossPrice(BigDecimal.valueOf(149.00))
                    .positionType(PositionType.LONG)
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);

            // Act
            BigDecimal riskReward = objective.calculateRiskRewardRatio(entryPrice);

            // Assert
            // Reward = 250 - 150 = 100
            // Risk = 150 - 149 = 1
            // R:R = 100 / 1 = 100.00
            assertThat(riskReward).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        }

        @Test
        @DisplayName("Should handle decimal prices correctly")
        void shouldHandleDecimalPrices() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetPrice(BigDecimal.valueOf(153.75))
                    .stopLossPrice(BigDecimal.valueOf(148.25))
                    .positionType(PositionType.LONG)
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(151.00);

            // Act
            BigDecimal riskReward = objective.calculateRiskRewardRatio(entryPrice);

            // Assert
            // Reward = 153.75 - 151.00 = 2.75
            // Risk = 151.00 - 148.25 = 2.75
            // R:R = 2.75 / 2.75 = 1.00
            assertThat(riskReward).isEqualByComparingTo(BigDecimal.valueOf(1.00));
        }
    }
}
