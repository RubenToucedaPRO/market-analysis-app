package com.market.analysis.unit.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.exception.InvalidRiskRewardException;
import com.market.analysis.domain.exception.MissingIndicatorException;
import com.market.analysis.domain.model.ObjectiveType;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StrategyObjective;
import com.market.analysis.domain.service.RiskRewardCalculator;

/**
 * Unit tests for RiskRewardCalculator domain service.
 * Tests cover calculation logic for target prices, stop-loss prices,
 * risk/reward ratios, and position sizing.
 */
@DisplayName("RiskRewardCalculator Domain Service Tests")
class RiskRewardCalculatorTest {

    private RiskRewardCalculator calculator;
    private Stock testStock;
    private BigDecimal entryPrice;

    @BeforeEach
    void setUp() {
        calculator = new RiskRewardCalculator();
        entryPrice = BigDecimal.valueOf(150.00);

        testStock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.00))
                .openPrice(BigDecimal.valueOf(148.00))
                .highOfDay(BigDecimal.valueOf(152.00))
                .lowOfDay(BigDecimal.valueOf(147.00))
                .previousClose(BigDecimal.valueOf(149.00))
                .sma20(BigDecimal.valueOf(145.00))
                .sma50(BigDecimal.valueOf(140.00))
                .sma200(BigDecimal.valueOf(130.00))
                .volume(10000000L)
                .averageVolume(8000000L)
                .lastUpdated(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Should create RiskRewardCalculator instance")
    void testRiskRewardCalculatorInstantiation() {
        assertThat(calculator).isNotNull();
    }

    @Nested
    @DisplayName("Calculate Target Price Tests")
    class CalculateTargetPriceTests {

        @Test
        @DisplayName("Should calculate target price using SMA20")
        void shouldCalculateTargetPriceUsingSma20() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .type(ObjectiveType.SMA)
                    .value(BigDecimal.valueOf(20))
                    .build();

            // Act
            BigDecimal targetPrice = calculator.calculateTargetPrice(entryPrice, objective, testStock);

            // Assert
            assertThat(targetPrice).isEqualByComparingTo("145.00");
        }

        @Test
        @DisplayName("Should calculate target price using SMA50")
        void shouldCalculateTargetPriceUsingSma50() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .type(ObjectiveType.SMA)
                    .value(BigDecimal.valueOf(50))
                    .build();

            // Act
            BigDecimal targetPrice = calculator.calculateTargetPrice(entryPrice, objective, testStock);

            // Assert
            assertThat(targetPrice).isEqualByComparingTo("140.00");
        }

        @Test
        @DisplayName("Should calculate target price using SMA200")
        void shouldCalculateTargetPriceUsingSma200() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .type(ObjectiveType.SMA)
                    .value(BigDecimal.valueOf(200))
                    .build();

            // Act
            BigDecimal targetPrice = calculator.calculateTargetPrice(entryPrice, objective, testStock);

            // Assert
            assertThat(targetPrice).isEqualByComparingTo("130.00");
        }

        @Test
        @DisplayName("Should throw exception for unsupported SMA period")
        void shouldThrowExceptionForUnsupportedSmaPeriod() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .type(ObjectiveType.SMA)
                    .value(BigDecimal.valueOf(100))
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateTargetPrice(entryPrice, objective, testStock))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Only SMA periods of 20, 50, and 200 are supported");
        }

        @Test
        @DisplayName("Should throw exception when SMA value is null")
        void shouldThrowExceptionWhenSmaValueIsNull() {
            // Arrange
            Stock stockWithoutSma = Stock.builder()
                    .ticker("TEST")
                    .currentPrice(BigDecimal.valueOf(100.00))
                    .build();

            StrategyObjective objective = StrategyObjective.builder()
                    .type(ObjectiveType.SMA)
                    .value(BigDecimal.valueOf(20))
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateTargetPrice(entryPrice, objective, stockWithoutSma))
                    .isInstanceOf(MissingIndicatorException.class)
                    .hasMessageContaining("SMA20 is not available for ticker TEST");
        }

        @Test
        @DisplayName("Should calculate target price using percentage")
        void shouldCalculateTargetPriceUsingPercentage() {
            // Arrange - 10% above entry price
            StrategyObjective objective = StrategyObjective.builder()
                    .type(ObjectiveType.PERCENTAGE)
                    .value(BigDecimal.valueOf(10.0))
                    .build();

            // Act
            BigDecimal targetPrice = calculator.calculateTargetPrice(entryPrice, objective, testStock);

            // Assert
            assertThat(targetPrice).isEqualByComparingTo("165.00");
        }

        @Test
        @DisplayName("Should calculate target price using fixed price")
        void shouldCalculateTargetPriceUsingFixedPrice() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .type(ObjectiveType.FIXED_PRICE)
                    .value(BigDecimal.valueOf(175.50))
                    .build();

            // Act
            BigDecimal targetPrice = calculator.calculateTargetPrice(entryPrice, objective, testStock);

            // Assert
            assertThat(targetPrice).isEqualByComparingTo("175.50");
        }

        @Test
        @DisplayName("Should throw exception when entry price is null")
        void shouldThrowExceptionWhenEntryPriceIsNull() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .type(ObjectiveType.FIXED_PRICE)
                    .value(BigDecimal.valueOf(175.50))
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateTargetPrice(null, objective, testStock))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Entry price cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when objective is null")
        void shouldThrowExceptionWhenObjectiveIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateTargetPrice(entryPrice, null, testStock))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Strategy objective cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when stock is null")
        void shouldThrowExceptionWhenStockIsNull() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .type(ObjectiveType.FIXED_PRICE)
                    .value(BigDecimal.valueOf(175.50))
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateTargetPrice(entryPrice, objective, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Stock cannot be null");
        }
    }

    @Nested
    @DisplayName("Calculate Stop Loss Price Tests")
    class CalculateStopLossPriceTests {

        @Test
        @DisplayName("Should calculate stop-loss price using SMA20")
        void shouldCalculateStopLossPriceUsingSma20() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .type(ObjectiveType.SMA)
                    .value(BigDecimal.valueOf(20))
                    .build();

            // Act
            BigDecimal stopPrice = calculator.calculateStopLossPrice(entryPrice, objective, testStock);

            // Assert
            assertThat(stopPrice).isEqualByComparingTo("145.00");
        }

        @Test
        @DisplayName("Should calculate stop-loss price using percentage")
        void shouldCalculateStopLossPriceUsingPercentage() {
            // Arrange - 5% below entry price
            StrategyObjective objective = StrategyObjective.builder()
                    .type(ObjectiveType.PERCENTAGE)
                    .value(BigDecimal.valueOf(5.0))
                    .build();

            // Act
            BigDecimal stopPrice = calculator.calculateStopLossPrice(entryPrice, objective, testStock);

            // Assert
            assertThat(stopPrice).isEqualByComparingTo("142.50");
        }

        @Test
        @DisplayName("Should calculate stop-loss price using fixed price")
        void shouldCalculateStopLossPriceUsingFixedPrice() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .type(ObjectiveType.FIXED_PRICE)
                    .value(BigDecimal.valueOf(140.00))
                    .build();

            // Act
            BigDecimal stopPrice = calculator.calculateStopLossPrice(entryPrice, objective, testStock);

            // Assert
            assertThat(stopPrice).isEqualByComparingTo("140.00");
        }

        @Test
        @DisplayName("Should throw exception when stop-loss equals entry price")
        void shouldThrowExceptionWhenStopLossEqualsEntryPrice() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .type(ObjectiveType.FIXED_PRICE)
                    .value(BigDecimal.valueOf(150.00))
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateStopLossPrice(entryPrice, objective, testStock))
                    .isInstanceOf(InvalidRiskRewardException.class)
                    .hasMessageContaining("Stop-loss price")
                    .hasMessageContaining("must be less than entry price");
        }

        @Test
        @DisplayName("Should throw exception when stop-loss is greater than entry price")
        void shouldThrowExceptionWhenStopLossGreaterThanEntryPrice() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .type(ObjectiveType.FIXED_PRICE)
                    .value(BigDecimal.valueOf(155.00))
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateStopLossPrice(entryPrice, objective, testStock))
                    .isInstanceOf(InvalidRiskRewardException.class)
                    .hasMessageContaining("Stop-loss price")
                    .hasMessageContaining("must be less than entry price");
        }

        @Test
        @DisplayName("Should throw exception when entry price is null")
        void shouldThrowExceptionWhenEntryPriceIsNull() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .type(ObjectiveType.FIXED_PRICE)
                    .value(BigDecimal.valueOf(140.00))
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateStopLossPrice(null, objective, testStock))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Entry price cannot be null");
        }
    }

    @Nested
    @DisplayName("Calculate Risk Reward Ratio Tests")
    class CalculateRiskRewardRatioTests {

        @Test
        @DisplayName("Should calculate risk/reward ratio correctly")
        void shouldCalculateRiskRewardRatioCorrectly() {
            // Arrange
            BigDecimal entry = BigDecimal.valueOf(100.00);
            BigDecimal target = BigDecimal.valueOf(120.00);
            BigDecimal stop = BigDecimal.valueOf(95.00);

            // Act
            BigDecimal ratio = calculator.calculateRiskRewardRatio(entry, target, stop);

            // Assert
            // Reward = 120 - 100 = 20
            // Risk = 100 - 95 = 5
            // Ratio = 20/5 = 4.00
            assertThat(ratio).isEqualByComparingTo("4.00");
        }

        @Test
        @DisplayName("Should calculate risk/reward ratio with decimals")
        void shouldCalculateRiskRewardRatioWithDecimals() {
            // Arrange
            BigDecimal entry = BigDecimal.valueOf(150.00);
            BigDecimal target = BigDecimal.valueOf(165.00);
            BigDecimal stop = BigDecimal.valueOf(145.00);

            // Act
            BigDecimal ratio = calculator.calculateRiskRewardRatio(entry, target, stop);

            // Assert
            // Reward = 165 - 150 = 15
            // Risk = 150 - 145 = 5
            // Ratio = 15/5 = 3.00
            assertThat(ratio).isEqualByComparingTo("3.00");
        }

        @Test
        @DisplayName("Should calculate ratio when reward is less than risk")
        void shouldCalculateRatioWhenRewardLessThanRisk() {
            // Arrange
            BigDecimal entry = BigDecimal.valueOf(100.00);
            BigDecimal target = BigDecimal.valueOf(105.00);
            BigDecimal stop = BigDecimal.valueOf(90.00);

            // Act
            BigDecimal ratio = calculator.calculateRiskRewardRatio(entry, target, stop);

            // Assert
            // Reward = 105 - 100 = 5
            // Risk = 100 - 90 = 10
            // Ratio = 5/10 = 0.50
            assertThat(ratio).isEqualByComparingTo("0.50");
        }

        @Test
        @DisplayName("Should throw exception when stop is greater than or equal to entry")
        void shouldThrowExceptionWhenStopGreaterThanOrEqualToEntry() {
            // Arrange
            BigDecimal entry = BigDecimal.valueOf(100.00);
            BigDecimal target = BigDecimal.valueOf(120.00);
            BigDecimal stop = BigDecimal.valueOf(105.00);

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateRiskRewardRatio(entry, target, stop))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Stop-loss price must be less than entry price");
        }

        @Test
        @DisplayName("Should throw exception when entry price is null")
        void shouldThrowExceptionWhenEntryPriceIsNull() {
            // Arrange
            BigDecimal target = BigDecimal.valueOf(120.00);
            BigDecimal stop = BigDecimal.valueOf(95.00);

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateRiskRewardRatio(null, target, stop))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Entry price cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when target is null")
        void shouldThrowExceptionWhenTargetIsNull() {
            // Arrange
            BigDecimal entry = BigDecimal.valueOf(100.00);
            BigDecimal stop = BigDecimal.valueOf(95.00);

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateRiskRewardRatio(entry, null, stop))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Target price cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when stop is null")
        void shouldThrowExceptionWhenStopIsNull() {
            // Arrange
            BigDecimal entry = BigDecimal.valueOf(100.00);
            BigDecimal target = BigDecimal.valueOf(120.00);

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateRiskRewardRatio(entry, target, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Stop-loss price cannot be null");
        }
    }

    @Nested
    @DisplayName("Calculate Position Size Tests")
    class CalculatePositionSizeTests {

        @Test
        @DisplayName("Should calculate position size correctly")
        void shouldCalculatePositionSizeCorrectly() {
            // Arrange
            BigDecimal entry = BigDecimal.valueOf(100.00);
            BigDecimal stop = BigDecimal.valueOf(95.00);
            BigDecimal capitalToRisk = BigDecimal.valueOf(500.00);

            // Act
            BigDecimal positionSize = calculator.calculatePositionSize(entry, stop, capitalToRisk);

            // Assert
            // Risk per share = 100 - 95 = 5
            // Position size = 500 / 5 = 100 shares
            assertThat(positionSize).isEqualByComparingTo("100");
        }

        @Test
        @DisplayName("Should round down position size to avoid exceeding risk")
        void shouldRoundDownPositionSize() {
            // Arrange
            BigDecimal entry = BigDecimal.valueOf(100.00);
            BigDecimal stop = BigDecimal.valueOf(97.00);
            BigDecimal capitalToRisk = BigDecimal.valueOf(500.00);

            // Act
            BigDecimal positionSize = calculator.calculatePositionSize(entry, stop, capitalToRisk);

            // Assert
            // Risk per share = 100 - 97 = 3
            // Position size = 500 / 3 = 166.666... -> 166 shares (rounded down)
            assertThat(positionSize).isEqualByComparingTo("166");
        }

        @Test
        @DisplayName("Should calculate position size with small risk per share")
        void shouldCalculatePositionSizeWithSmallRiskPerShare() {
            // Arrange
            BigDecimal entry = BigDecimal.valueOf(50.00);
            BigDecimal stop = BigDecimal.valueOf(49.50);
            BigDecimal capitalToRisk = BigDecimal.valueOf(100.00);

            // Act
            BigDecimal positionSize = calculator.calculatePositionSize(entry, stop, capitalToRisk);

            // Assert
            // Risk per share = 50 - 49.50 = 0.50
            // Position size = 100 / 0.50 = 200 shares
            assertThat(positionSize).isEqualByComparingTo("200");
        }

        @Test
        @DisplayName("Should throw exception when stop is greater than or equal to entry")
        void shouldThrowExceptionWhenStopGreaterThanOrEqualToEntry() {
            // Arrange
            BigDecimal entry = BigDecimal.valueOf(100.00);
            BigDecimal stop = BigDecimal.valueOf(105.00);
            BigDecimal capitalToRisk = BigDecimal.valueOf(500.00);

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculatePositionSize(entry, stop, capitalToRisk))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Stop-loss price must be less than entry price");
        }

        @Test
        @DisplayName("Should throw exception when capital to risk is zero")
        void shouldThrowExceptionWhenCapitalToRiskIsZero() {
            // Arrange
            BigDecimal entry = BigDecimal.valueOf(100.00);
            BigDecimal stop = BigDecimal.valueOf(95.00);
            BigDecimal capitalToRisk = BigDecimal.ZERO;

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculatePositionSize(entry, stop, capitalToRisk))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Capital to risk must be positive");
        }

        @Test
        @DisplayName("Should throw exception when capital to risk is negative")
        void shouldThrowExceptionWhenCapitalToRiskIsNegative() {
            // Arrange
            BigDecimal entry = BigDecimal.valueOf(100.00);
            BigDecimal stop = BigDecimal.valueOf(95.00);
            BigDecimal capitalToRisk = BigDecimal.valueOf(-500.00);

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculatePositionSize(entry, stop, capitalToRisk))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Capital to risk must be positive");
        }

        @Test
        @DisplayName("Should throw exception when entry price is null")
        void shouldThrowExceptionWhenEntryPriceIsNull() {
            // Arrange
            BigDecimal stop = BigDecimal.valueOf(95.00);
            BigDecimal capitalToRisk = BigDecimal.valueOf(500.00);

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculatePositionSize(null, stop, capitalToRisk))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Entry price cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when stop price is null")
        void shouldThrowExceptionWhenStopPriceIsNull() {
            // Arrange
            BigDecimal entry = BigDecimal.valueOf(100.00);
            BigDecimal capitalToRisk = BigDecimal.valueOf(500.00);

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculatePositionSize(entry, null, capitalToRisk))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Stop-loss price cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when capital to risk is null")
        void shouldThrowExceptionWhenCapitalToRiskIsNull() {
            // Arrange
            BigDecimal entry = BigDecimal.valueOf(100.00);
            BigDecimal stop = BigDecimal.valueOf(95.00);

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculatePositionSize(entry, stop, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Capital to risk cannot be null");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    class EdgeCasesAndIntegrationTests {

        @Test
        @DisplayName("Should handle complete trade calculation workflow")
        void shouldHandleCompleteTradeCalculationWorkflow() {
            // Arrange
            BigDecimal entry = BigDecimal.valueOf(150.00);
            
            StrategyObjective targetObjective = StrategyObjective.builder()
                    .type(ObjectiveType.PERCENTAGE)
                    .value(BigDecimal.valueOf(10.0))
                    .build();
                    
            StrategyObjective stopObjective = StrategyObjective.builder()
                    .type(ObjectiveType.PERCENTAGE)
                    .value(BigDecimal.valueOf(5.0))
                    .build();
                    
            BigDecimal capitalToRisk = BigDecimal.valueOf(1000.00);

            // Act
            BigDecimal target = calculator.calculateTargetPrice(entry, targetObjective, testStock);
            BigDecimal stop = calculator.calculateStopLossPrice(entry, stopObjective, testStock);
            BigDecimal ratio = calculator.calculateRiskRewardRatio(entry, target, stop);
            BigDecimal positionSize = calculator.calculatePositionSize(entry, stop, capitalToRisk);

            // Assert
            assertThat(target).isEqualByComparingTo("165.00"); // 150 + 10% = 165
            assertThat(stop).isEqualByComparingTo("142.50"); // 150 - 5% = 142.50
            assertThat(ratio).isEqualByComparingTo("2.00"); // (165-150)/(150-142.50) = 15/7.5 = 2
            assertThat(positionSize).isEqualByComparingTo("133"); // 1000/7.5 = 133.33 -> 133
        }

        @Test
        @DisplayName("Should handle very small percentages")
        void shouldHandleVerySmallPercentages() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .type(ObjectiveType.PERCENTAGE)
                    .value(BigDecimal.valueOf(0.1))
                    .build();

            // Act
            BigDecimal targetPrice = calculator.calculateTargetPrice(entryPrice, objective, testStock);

            // Assert
            assertThat(targetPrice).isEqualByComparingTo("150.15");
        }

        @Test
        @DisplayName("Should handle large position sizes")
        void shouldHandleLargePositionSizes() {
            // Arrange
            BigDecimal entry = BigDecimal.valueOf(10.00);
            BigDecimal stop = BigDecimal.valueOf(9.95);
            BigDecimal capitalToRisk = BigDecimal.valueOf(10000.00);

            // Act
            BigDecimal positionSize = calculator.calculatePositionSize(entry, stop, capitalToRisk);

            // Assert
            // Risk per share = 0.05
            // Position size = 10000 / 0.05 = 200,000 shares
            assertThat(positionSize).isEqualByComparingTo("200000");
        }
    }
}
