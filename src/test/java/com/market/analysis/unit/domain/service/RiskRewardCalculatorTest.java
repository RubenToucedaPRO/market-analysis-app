package com.market.analysis.unit.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.exception.DomainValidationException;
import com.market.analysis.domain.exception.MissingIndicatorException;
import com.market.analysis.domain.model.ObjectiveType;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StrategyObjective;
import com.market.analysis.domain.service.RiskRewardCalculator;

/**
 * Unit tests for RiskRewardCalculator domain service.
 * Tests cover deterministic risk-reward calculation logic for trading strategies.
 */
@DisplayName("RiskRewardCalculator Domain Service Tests")
class RiskRewardCalculatorTest {

    private RiskRewardCalculator calculator;
    private Stock testStock;

    @BeforeEach
    void setUp() {
        calculator = new RiskRewardCalculator();

        testStock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(BigDecimal.valueOf(150.00))
                .sma20(BigDecimal.valueOf(145.00))
                .sma50(BigDecimal.valueOf(140.00))
                .sma200(BigDecimal.valueOf(130.00))
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
            BigDecimal entryPrice = BigDecimal.valueOf(150.00);
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.SMA)
                    .targetValue(BigDecimal.valueOf(20))
                    .stopLossType(ObjectiveType.PERCENTAGE)
                    .stopLossValue(BigDecimal.valueOf(2.0))
                    .capitalToRisk(BigDecimal.valueOf(1000))
                    .description("Test objective")
                    .build();

            // Act
            BigDecimal targetPrice = calculator.calculateTargetPrice(entryPrice, objective, testStock);

            // Assert
            assertThat(targetPrice).isEqualByComparingTo(BigDecimal.valueOf(145.00));
        }

        @Test
        @DisplayName("Should calculate target price using SMA50")
        void shouldCalculateTargetPriceUsingSma50() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(150.00);
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.SMA)
                    .targetValue(BigDecimal.valueOf(50))
                    .stopLossType(ObjectiveType.PERCENTAGE)
                    .stopLossValue(BigDecimal.valueOf(2.0))
                    .capitalToRisk(BigDecimal.valueOf(1000))
                    .description("Test objective")
                    .build();

            // Act
            BigDecimal targetPrice = calculator.calculateTargetPrice(entryPrice, objective, testStock);

            // Assert
            assertThat(targetPrice).isEqualByComparingTo(BigDecimal.valueOf(140.00));
        }

        @Test
        @DisplayName("Should calculate target price using SMA200")
        void shouldCalculateTargetPriceUsingSma200() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(150.00);
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.SMA)
                    .targetValue(BigDecimal.valueOf(200))
                    .stopLossType(ObjectiveType.PERCENTAGE)
                    .stopLossValue(BigDecimal.valueOf(2.0))
                    .capitalToRisk(BigDecimal.valueOf(1000))
                    .description("Test objective")
                    .build();

            // Act
            BigDecimal targetPrice = calculator.calculateTargetPrice(entryPrice, objective, testStock);

            // Assert
            assertThat(targetPrice).isEqualByComparingTo(BigDecimal.valueOf(130.00));
        }

        @Test
        @DisplayName("Should throw exception for unsupported SMA period")
        void shouldThrowExceptionForUnsupportedSmaPeriod() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(150.00);
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.SMA)
                    .targetValue(BigDecimal.valueOf(100))
                    .stopLossType(ObjectiveType.PERCENTAGE)
                    .stopLossValue(BigDecimal.valueOf(2.0))
                    .capitalToRisk(BigDecimal.valueOf(1000))
                    .description("Test objective")
                    .build();

            // Act & Assert — message now references catalog-derived allowed periods
            assertThatThrownBy(() -> calculator.calculateTargetPrice(entryPrice, objective, testStock))
                    .isInstanceOf(DomainValidationException.class)
                    .satisfies(ex -> assertThat(((DomainValidationException) ex).getErrorCode())
                            .isEqualTo("validation.sma_period_unsupported"));
        }

        @Test
        @DisplayName("Should throw MissingIndicatorException when SMA20 is null")
        void shouldThrowMissingIndicatorExceptionWhenSma20IsNull() {
            // Arrange
            Stock stockWithoutSma20 = Stock.builder()
                    .ticker("AAPL")
                    .currentPrice(BigDecimal.valueOf(150.00))
                    .sma20(null)
                    .sma50(BigDecimal.valueOf(140.00))
                    .sma200(BigDecimal.valueOf(130.00))
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(150.00);
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.SMA)
                    .targetValue(BigDecimal.valueOf(20))
                    .stopLossType(ObjectiveType.PERCENTAGE)
                    .stopLossValue(BigDecimal.valueOf(2.0))
                    .capitalToRisk(BigDecimal.valueOf(1000))
                    .description("Test objective")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateTargetPrice(entryPrice, objective, stockWithoutSma20))
                    .isInstanceOf(MissingIndicatorException.class)
                    .satisfies(ex -> assertThat(((MissingIndicatorException) ex).getErrorCode())
                            .isEqualTo("rule.missing_indicator"));
        }

        @Test
        @DisplayName("Should calculate target price using percentage (5%)")
        void shouldCalculateTargetPriceUsingPercentage() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(100.00);
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.PERCENTAGE)
                    .targetValue(BigDecimal.valueOf(5.0))
                    .stopLossType(ObjectiveType.PERCENTAGE)
                    .stopLossValue(BigDecimal.valueOf(2.0))
                    .capitalToRisk(BigDecimal.valueOf(1000))
                    .description("Test objective")
                    .build();

            // Act
            BigDecimal targetPrice = calculator.calculateTargetPrice(entryPrice, objective, testStock);

            // Assert
            assertThat(targetPrice).isEqualByComparingTo(BigDecimal.valueOf(105.00));
        }

        @Test
        @DisplayName("Should calculate target price using fixed price")
        void shouldCalculateTargetPriceUsingFixedPrice() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(100.00);
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.FIXED_PRICE)
                    .targetValue(BigDecimal.valueOf(110.50))
                    .stopLossType(ObjectiveType.PERCENTAGE)
                    .stopLossValue(BigDecimal.valueOf(2.0))
                    .capitalToRisk(BigDecimal.valueOf(1000))
                    .description("Test objective")
                    .build();

            // Act
            BigDecimal targetPrice = calculator.calculateTargetPrice(entryPrice, objective, testStock);

            // Assert
            assertThat(targetPrice).isEqualByComparingTo(BigDecimal.valueOf(110.50));
        }

        @Test
        @DisplayName("Should throw exception when entry price is null")
        void shouldThrowExceptionWhenEntryPriceIsNull() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.PERCENTAGE)
                    .targetValue(BigDecimal.valueOf(5.0))
                    .stopLossType(ObjectiveType.PERCENTAGE)
                    .stopLossValue(BigDecimal.valueOf(2.0))
                    .capitalToRisk(BigDecimal.valueOf(1000))
                    .description("Test objective")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateTargetPrice(null, objective, testStock))
                    .isInstanceOf(DomainValidationException.class)
                    .satisfies(ex -> assertThat(((DomainValidationException) ex).getErrorCode())
                            .isEqualTo("validation.entry_price_null"));
        }

        @Test
        @DisplayName("Should throw exception when entry price is zero")
        void shouldThrowExceptionWhenEntryPriceIsZero() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.PERCENTAGE)
                    .targetValue(BigDecimal.valueOf(5.0))
                    .stopLossType(ObjectiveType.PERCENTAGE)
                    .stopLossValue(BigDecimal.valueOf(2.0))
                    .capitalToRisk(BigDecimal.valueOf(1000))
                    .description("Test objective")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateTargetPrice(BigDecimal.ZERO, objective, testStock))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Entry price must be greater than zero");
        }

        @Test
        @DisplayName("Should throw exception when entry price is negative")
        void shouldThrowExceptionWhenEntryPriceIsNegative() {
            // Arrange
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.PERCENTAGE)
                    .targetValue(BigDecimal.valueOf(5.0))
                    .stopLossType(ObjectiveType.PERCENTAGE)
                    .stopLossValue(BigDecimal.valueOf(2.0))
                    .capitalToRisk(BigDecimal.valueOf(1000))
                    .description("Test objective")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateTargetPrice(BigDecimal.valueOf(-100), objective, testStock))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Entry price must be greater than zero");
        }
    }

    @Nested
    @DisplayName("Calculate Stop Loss Price Tests")
    class CalculateStopLossPriceTests {

        @Test
        @DisplayName("Should calculate stop-loss price using SMA20")
        void shouldCalculateStopLossPriceUsingSma20() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(150.00);
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.PERCENTAGE)
                    .targetValue(BigDecimal.valueOf(5.0))
                    .stopLossType(ObjectiveType.SMA)
                    .stopLossValue(BigDecimal.valueOf(20))
                    .capitalToRisk(BigDecimal.valueOf(1000))
                    .description("Test objective")
                    .build();

            // Act
            BigDecimal stopLossPrice = calculator.calculateStopLossPrice(entryPrice, objective, testStock);

            // Assert
            assertThat(stopLossPrice).isEqualByComparingTo(BigDecimal.valueOf(145.00));
        }

        @Test
        @DisplayName("Should calculate stop-loss price using percentage (2%)")
        void shouldCalculateStopLossPriceUsingPercentage() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(100.00);
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.PERCENTAGE)
                    .targetValue(BigDecimal.valueOf(5.0))
                    .stopLossType(ObjectiveType.PERCENTAGE)
                    .stopLossValue(BigDecimal.valueOf(2.0))
                    .capitalToRisk(BigDecimal.valueOf(1000))
                    .description("Test objective")
                    .build();

            // Act
            BigDecimal stopLossPrice = calculator.calculateStopLossPrice(entryPrice, objective, testStock);

            // Assert
            assertThat(stopLossPrice).isEqualByComparingTo(BigDecimal.valueOf(98.00));
        }

        @Test
        @DisplayName("Should calculate stop-loss price using fixed price")
        void shouldCalculateStopLossPriceUsingFixedPrice() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(100.00);
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.PERCENTAGE)
                    .targetValue(BigDecimal.valueOf(5.0))
                    .stopLossType(ObjectiveType.FIXED_PRICE)
                    .stopLossValue(BigDecimal.valueOf(95.50))
                    .capitalToRisk(BigDecimal.valueOf(1000))
                    .description("Test objective")
                    .build();

            // Act
            BigDecimal stopLossPrice = calculator.calculateStopLossPrice(entryPrice, objective, testStock);

            // Assert
            assertThat(stopLossPrice).isEqualByComparingTo(BigDecimal.valueOf(95.50));
        }

        @Test
        @DisplayName("Should throw exception when stop-loss equals entry price")
        void shouldThrowExceptionWhenStopLossEqualsEntryPrice() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(100.00);
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.PERCENTAGE)
                    .targetValue(BigDecimal.valueOf(5.0))
                    .stopLossType(ObjectiveType.FIXED_PRICE)
                    .stopLossValue(BigDecimal.valueOf(100.00))
                    .capitalToRisk(BigDecimal.valueOf(1000))
                    .description("Test objective")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateStopLossPrice(entryPrice, objective, testStock))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Stop-loss price");
        }

        @Test
        @DisplayName("Should throw exception when stop-loss is greater than entry price")
        void shouldThrowExceptionWhenStopLossGreaterThanEntryPrice() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(100.00);
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.PERCENTAGE)
                    .targetValue(BigDecimal.valueOf(5.0))
                    .stopLossType(ObjectiveType.FIXED_PRICE)
                    .stopLossValue(BigDecimal.valueOf(105.00))
                    .capitalToRisk(BigDecimal.valueOf(1000))
                    .description("Test objective")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateStopLossPrice(entryPrice, objective, testStock))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Stop-loss price");
        }

        @Test
        @DisplayName("Should throw MissingIndicatorException when SMA50 is null")
        void shouldThrowMissingIndicatorExceptionWhenSma50IsNull() {
            // Arrange
            Stock stockWithoutSma50 = Stock.builder()
                    .ticker("MSFT")
                    .currentPrice(BigDecimal.valueOf(300.00))
                    .sma20(BigDecimal.valueOf(295.00))
                    .sma50(null)
                    .sma200(BigDecimal.valueOf(280.00))
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(300.00);
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.PERCENTAGE)
                    .targetValue(BigDecimal.valueOf(5.0))
                    .stopLossType(ObjectiveType.SMA)
                    .stopLossValue(BigDecimal.valueOf(50))
                    .capitalToRisk(BigDecimal.valueOf(1000))
                    .description("Test objective")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateStopLossPrice(entryPrice, objective, stockWithoutSma50))
                    .isInstanceOf(MissingIndicatorException.class)
                    .satisfies(ex -> assertThat(((MissingIndicatorException) ex).getErrorCode())
                            .isEqualTo("rule.missing_indicator"));
        }
    }

    @Nested
    @DisplayName("Calculate Risk Reward Ratio Tests")
    class CalculateRiskRewardRatioTests {

        @Test
        @DisplayName("Should calculate risk-reward ratio for 2:1 scenario")
        void shouldCalculateRiskRewardRatioFor2To1Scenario() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(100.00);
            BigDecimal targetPrice = BigDecimal.valueOf(110.00);
            BigDecimal stopPrice = BigDecimal.valueOf(95.00);

            // Act
            BigDecimal ratio = calculator.calculateRiskRewardRatio(entryPrice, targetPrice, stopPrice);

            // Assert
            // Reward: 110 - 100 = 10
            // Risk: 100 - 95 = 5
            // Ratio: 10 / 5 = 2.0
            assertThat(ratio).isEqualByComparingTo(BigDecimal.valueOf(2.0));
        }

        @Test
        @DisplayName("Should calculate risk-reward ratio for 3:1 scenario")
        void shouldCalculateRiskRewardRatioFor3To1Scenario() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(100.00);
            BigDecimal targetPrice = BigDecimal.valueOf(115.00);
            BigDecimal stopPrice = BigDecimal.valueOf(95.00);

            // Act
            BigDecimal ratio = calculator.calculateRiskRewardRatio(entryPrice, targetPrice, stopPrice);

            // Assert
            // Reward: 115 - 100 = 15
            // Risk: 100 - 95 = 5
            // Ratio: 15 / 5 = 3.0
            assertThat(ratio).isEqualByComparingTo(BigDecimal.valueOf(3.0));
        }

        @Test
        @DisplayName("Should calculate risk-reward ratio with decimal precision")
        void shouldCalculateRiskRewardRatioWithDecimalPrecision() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(150.25);
            BigDecimal targetPrice = BigDecimal.valueOf(157.75);
            BigDecimal stopPrice = BigDecimal.valueOf(147.00);

            // Act
            BigDecimal ratio = calculator.calculateRiskRewardRatio(entryPrice, targetPrice, stopPrice);

            // Assert
            // Reward: 157.75 - 150.25 = 7.50
            // Risk: 150.25 - 147.00 = 3.25
            // Ratio: 7.50 / 3.25 = 2.3077
            assertThat(ratio).isEqualByComparingTo(BigDecimal.valueOf(2.3077));
        }

        @Test
        @DisplayName("Should throw exception when entry price is null")
        void shouldThrowExceptionWhenEntryPriceIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateRiskRewardRatio(null, 
                    BigDecimal.valueOf(110), BigDecimal.valueOf(95)))
                    .isInstanceOf(DomainValidationException.class)
                    .satisfies(ex -> assertThat(((DomainValidationException) ex).getErrorCode())
                            .isEqualTo("validation.entry_price_null"));
        }

        @Test
        @DisplayName("Should throw exception when target price is null")
        void shouldThrowExceptionWhenTargetPriceIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateRiskRewardRatio(BigDecimal.valueOf(100), 
                    null, BigDecimal.valueOf(95)))
                    .isInstanceOf(DomainValidationException.class)
                    .satisfies(ex -> assertThat(((DomainValidationException) ex).getErrorCode())
                            .isEqualTo("validation.target_price_null"));
        }

        @Test
        @DisplayName("Should throw exception when target price is less than or equal to entry price")
        void shouldThrowExceptionWhenTargetPriceLessThanOrEqualToEntryPrice() {
            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateRiskRewardRatio(
                    BigDecimal.valueOf(100), BigDecimal.valueOf(100), BigDecimal.valueOf(95)))
                    .isInstanceOf(DomainValidationException.class)
                    .satisfies(ex -> assertThat(((DomainValidationException) ex).getErrorCode())
                            .isEqualTo("validation.target_below_entry"));
        }

        @Test
        @DisplayName("Should throw exception when stop price is greater than or equal to entry price")
        void shouldThrowExceptionWhenStopPriceGreaterThanOrEqualToEntryPrice() {
            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateRiskRewardRatio(
                    BigDecimal.valueOf(100), BigDecimal.valueOf(110), BigDecimal.valueOf(100)))
                    .isInstanceOf(DomainValidationException.class)
                    .satisfies(ex -> assertThat(((DomainValidationException) ex).getErrorCode())
                            .isEqualTo("validation.stop_above_entry"));
        }
    }

    @Nested
    @DisplayName("Calculate Position Size Tests")
    class CalculatePositionSizeTests {

        @Test
        @DisplayName("Should calculate position size correctly")
        void shouldCalculatePositionSizeCorrectly() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(100.00);
            BigDecimal stopPrice = BigDecimal.valueOf(95.00);
            BigDecimal capitalToRisk = BigDecimal.valueOf(1000.00);

            // Act
            BigDecimal positionSize = calculator.calculatePositionSize(entryPrice, stopPrice, capitalToRisk);

            // Assert
            // Risk per share: 100 - 95 = 5
            // Position size: 1000 / 5 = 200 shares
            assertThat(positionSize).isEqualByComparingTo(BigDecimal.valueOf(200));
        }

        @Test
        @DisplayName("Should round position size DOWN to avoid exceeding risk")
        void shouldRoundPositionSizeDown() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(100.00);
            BigDecimal stopPrice = BigDecimal.valueOf(97.00);
            BigDecimal capitalToRisk = BigDecimal.valueOf(1000.00);

            // Act
            BigDecimal positionSize = calculator.calculatePositionSize(entryPrice, stopPrice, capitalToRisk);

            // Assert
            // Risk per share: 100 - 97 = 3
            // Position size: 1000 / 3 = 333.333... -> rounded DOWN to 333
            assertThat(positionSize).isEqualByComparingTo(BigDecimal.valueOf(333));
        }

        @Test
        @DisplayName("Should calculate small position size for high-risk trades")
        void shouldCalculateSmallPositionSizeForHighRiskTrades() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(100.00);
            BigDecimal stopPrice = BigDecimal.valueOf(80.00);
            BigDecimal capitalToRisk = BigDecimal.valueOf(500.00);

            // Act
            BigDecimal positionSize = calculator.calculatePositionSize(entryPrice, stopPrice, capitalToRisk);

            // Assert
            // Risk per share: 100 - 80 = 20
            // Position size: 500 / 20 = 25 shares
            assertThat(positionSize).isEqualByComparingTo(BigDecimal.valueOf(25));
        }

        @Test
        @DisplayName("Should throw exception when entry price is null")
        void shouldThrowExceptionWhenEntryPriceIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> calculator.calculatePositionSize(null, 
                    BigDecimal.valueOf(95), BigDecimal.valueOf(1000)))
                    .isInstanceOf(DomainValidationException.class)
                    .satisfies(ex -> assertThat(((DomainValidationException) ex).getErrorCode())
                            .isEqualTo("validation.entry_price_null"));
        }

        @Test
        @DisplayName("Should throw exception when stop price is null")
        void shouldThrowExceptionWhenStopPriceIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> calculator.calculatePositionSize(BigDecimal.valueOf(100), 
                    null, BigDecimal.valueOf(1000)))
                    .isInstanceOf(DomainValidationException.class)
                    .satisfies(ex -> assertThat(((DomainValidationException) ex).getErrorCode())
                            .isEqualTo("validation.stop_price_null"));
        }

        @Test
        @DisplayName("Should throw exception when capital to risk is null")
        void shouldThrowExceptionWhenCapitalToRiskIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> calculator.calculatePositionSize(BigDecimal.valueOf(100), 
                    BigDecimal.valueOf(95), null))
                    .isInstanceOf(DomainValidationException.class)
                    .satisfies(ex -> assertThat(((DomainValidationException) ex).getErrorCode())
                            .isEqualTo("validation.capital_null"));
        }

        @Test
        @DisplayName("Should throw exception when stop price is greater than or equal to entry price")
        void shouldThrowExceptionWhenStopPriceGreaterThanOrEqualToEntryPrice() {
            // Act & Assert
            assertThatThrownBy(() -> calculator.calculatePositionSize(
                    BigDecimal.valueOf(100), BigDecimal.valueOf(100), BigDecimal.valueOf(1000)))
                    .isInstanceOf(DomainValidationException.class)
                    .satisfies(ex -> assertThat(((DomainValidationException) ex).getErrorCode())
                            .isEqualTo("validation.stop_above_entry"));
        }

        @Test
        @DisplayName("Should throw exception when capital to risk is zero")
        void shouldThrowExceptionWhenCapitalToRiskIsZero() {
            // Act & Assert
            assertThatThrownBy(() -> calculator.calculatePositionSize(
                    BigDecimal.valueOf(100), BigDecimal.valueOf(95), BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Capital to risk must be greater than zero");
        }

        @Test
        @DisplayName("Should throw exception when capital to risk is negative")
        void shouldThrowExceptionWhenCapitalToRiskIsNegative() {
            // Act & Assert
            assertThatThrownBy(() -> calculator.calculatePositionSize(
                    BigDecimal.valueOf(100), BigDecimal.valueOf(95), BigDecimal.valueOf(-1000)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Capital to risk must be greater than zero");
        }
    }

    @Nested
    @DisplayName("Integration Tests - Full Workflow")
    class IntegrationTests {

        @Test
        @DisplayName("Should calculate all metrics for a complete trading strategy")
        void shouldCalculateAllMetricsForCompleteTradingStrategy() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(150.00);
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.PERCENTAGE)
                    .targetValue(BigDecimal.valueOf(10.0))
                    .stopLossType(ObjectiveType.PERCENTAGE)
                    .stopLossValue(BigDecimal.valueOf(5.0))
                    .capitalToRisk(BigDecimal.valueOf(1000.00))
                    .description("Complete strategy test")
                    .build();

            // Act
            BigDecimal targetPrice = calculator.calculateTargetPrice(entryPrice, objective, testStock);
            BigDecimal stopLossPrice = calculator.calculateStopLossPrice(entryPrice, objective, testStock);
            BigDecimal riskRewardRatio = calculator.calculateRiskRewardRatio(entryPrice, targetPrice, stopLossPrice);
            BigDecimal positionSize = calculator.calculatePositionSize(entryPrice, stopLossPrice, objective.getCapitalToRisk());

            // Assert
            assertThat(targetPrice).isEqualByComparingTo(BigDecimal.valueOf(165.00)); // 150 + 10%
            assertThat(stopLossPrice).isEqualByComparingTo(BigDecimal.valueOf(142.50)); // 150 - 5%
            assertThat(riskRewardRatio).isEqualByComparingTo(BigDecimal.valueOf(2.0)); // (165-150)/(150-142.5) = 15/7.5 = 2
            assertThat(positionSize).isEqualByComparingTo(BigDecimal.valueOf(133)); // 1000/7.5 = 133.33 rounded down
        }

        @Test
        @DisplayName("Should validate SMA-based strategy with invalid target below entry")
        void shouldValidateSmaBasedStrategyWithInvalidTarget() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(150.00);
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.SMA)
                    .targetValue(BigDecimal.valueOf(200))
                    .stopLossType(ObjectiveType.SMA)
                    .stopLossValue(BigDecimal.valueOf(20))
                    .capitalToRisk(BigDecimal.valueOf(500.00))
                    .description("SMA-based strategy test")
                    .build();

            // Act
            BigDecimal targetPrice = calculator.calculateTargetPrice(entryPrice, objective, testStock);
            BigDecimal stopLossPrice = calculator.calculateStopLossPrice(entryPrice, objective, testStock);

            // Assert
            assertThat(targetPrice).isEqualByComparingTo(BigDecimal.valueOf(130.00)); // SMA200
            assertThat(stopLossPrice).isEqualByComparingTo(BigDecimal.valueOf(145.00)); // SMA20
            // Note: target (130) < entry (150), so this would fail in calculateRiskRewardRatio
            // This test shows validation is working - in real scenario, target should be above entry
            assertThatThrownBy(() -> calculator.calculateRiskRewardRatio(entryPrice, targetPrice, stopLossPrice))
                    .isInstanceOf(DomainValidationException.class)
                    .satisfies(ex -> assertThat(((DomainValidationException) ex).getErrorCode())
                            .isEqualTo("validation.target_below_entry"));
        }
    }

    @Nested
    @DisplayName("SMA Resolution via RuleCapabilityCatalog Tests")
    class SmaCatalogDelegationTests {

        @Test
        @DisplayName("Should resolve SMA20 target via catalog delegation")
        void shouldResolveSma20TargetViaCatalog() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(150.00);
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.SMA)
                    .targetValue(BigDecimal.valueOf(20))
                    .stopLossType(ObjectiveType.PERCENTAGE)
                    .stopLossValue(BigDecimal.valueOf(2.0))
                    .capitalToRisk(BigDecimal.valueOf(1000))
                    .description("Catalog SMA20 target test")
                    .build();

            // Act
            BigDecimal targetPrice = calculator.calculateTargetPrice(entryPrice, objective, testStock);

            // Assert — resolved via catalog, same result as direct access
            assertThat(targetPrice).isEqualByComparingTo(testStock.getSma20());
        }

        @Test
        @DisplayName("Should resolve SMA50 stop-loss via catalog delegation")
        void shouldResolveSma50StopLossViaCatalog() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(150.00);
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.PERCENTAGE)
                    .targetValue(BigDecimal.valueOf(5.0))
                    .stopLossType(ObjectiveType.SMA)
                    .stopLossValue(BigDecimal.valueOf(50))
                    .capitalToRisk(BigDecimal.valueOf(1000))
                    .description("Catalog SMA50 stop-loss test")
                    .build();

            // Act
            BigDecimal stopLossPrice = calculator.calculateStopLossPrice(entryPrice, objective, testStock);

            // Assert
            assertThat(stopLossPrice).isEqualByComparingTo(testStock.getSma50());
        }

        @Test
        @DisplayName("Should resolve SMA200 target via catalog delegation")
        void shouldResolveSma200TargetViaCatalog() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(150.00);
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.SMA)
                    .targetValue(BigDecimal.valueOf(200))
                    .stopLossType(ObjectiveType.PERCENTAGE)
                    .stopLossValue(BigDecimal.valueOf(2.0))
                    .capitalToRisk(BigDecimal.valueOf(1000))
                    .description("Catalog SMA200 target test")
                    .build();

            // Act
            BigDecimal targetPrice = calculator.calculateTargetPrice(entryPrice, objective, testStock);

            // Assert
            assertThat(targetPrice).isEqualByComparingTo(testStock.getSma200());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for SMA period not in catalog")
        void shouldThrowForSmaPeriodNotInCatalog() {
            // Arrange
            BigDecimal entryPrice = BigDecimal.valueOf(150.00);
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.SMA)
                    .targetValue(BigDecimal.valueOf(75))
                    .stopLossType(ObjectiveType.PERCENTAGE)
                    .stopLossValue(BigDecimal.valueOf(2.0))
                    .capitalToRisk(BigDecimal.valueOf(1000))
                    .description("Unsupported SMA test")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateTargetPrice(entryPrice, objective, testStock))
                    .isInstanceOf(DomainValidationException.class)
                    .satisfies(ex -> assertThat(((DomainValidationException) ex).getErrorCode())
                            .isEqualTo("validation.sma_period_unsupported"));
        }

        @Test
        @DisplayName("Should throw MissingIndicatorException when catalog-resolved SMA is null")
        void shouldThrowMissingIndicatorWhenCatalogResolvedSmaIsNull() {
            // Arrange — stock has null SMA200
            Stock stockWithNullSma200 = Stock.builder()
                    .ticker("TSLA")
                    .currentPrice(BigDecimal.valueOf(250.00))
                    .sma20(BigDecimal.valueOf(240.00))
                    .sma50(BigDecimal.valueOf(230.00))
                    .sma200(null)
                    .build();

            BigDecimal entryPrice = BigDecimal.valueOf(250.00);
            StrategyObjective objective = StrategyObjective.builder()
                    .targetType(ObjectiveType.SMA)
                    .targetValue(BigDecimal.valueOf(200))
                    .stopLossType(ObjectiveType.PERCENTAGE)
                    .stopLossValue(BigDecimal.valueOf(2.0))
                    .capitalToRisk(BigDecimal.valueOf(1000))
                    .description("Missing SMA200 test")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> calculator.calculateTargetPrice(entryPrice, objective, stockWithNullSma200))
                    .isInstanceOf(MissingIndicatorException.class)
                    .satisfies(ex -> assertThat(((MissingIndicatorException) ex).getErrorCode())
                            .isEqualTo("rule.missing_indicator"));
        }
    }
}
