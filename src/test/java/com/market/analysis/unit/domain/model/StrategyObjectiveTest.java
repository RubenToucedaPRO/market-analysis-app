package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.ObjectiveType;
import com.market.analysis.domain.model.StrategyObjective;

@DisplayName("StrategyObjective Domain Model Tests")
class StrategyObjectiveTest {

    @Test
    @DisplayName("Should create valid StrategyObjective with all fields")
    void shouldCreateValidStrategyObjectiveWithAllFields() {
        // Arrange & Act
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Conservative growth strategy with 5% target and 2% stop-loss")
                .build();

        // Assert
        assertNotNull(objective);
        assertEquals(ObjectiveType.PERCENTAGE, objective.getTargetType());
        assertEquals(ObjectiveType.PERCENTAGE, objective.getStopLossType());
        assertEquals(BigDecimal.valueOf(5.0), objective.getTargetValue());
        assertEquals(BigDecimal.valueOf(2.0), objective.getStopLossValue());
        assertEquals(BigDecimal.valueOf(0.02), objective.getCapitalToRisk());
        assertEquals("Conservative growth strategy with 5% target and 2% stop-loss", objective.getDescription());
        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should create StrategyObjective with SMA type")
    void shouldCreateStrategyObjectiveWithSMAType() {
        // Arrange & Act
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.SMA)
                .targetValue(BigDecimal.valueOf(20))
                .stopLossValue(BigDecimal.valueOf(50))
                .capitalToRisk(BigDecimal.valueOf(0.01))
                .description("SMA-based strategy with 20-period target and 50-period stop-loss")
                .build();

        // Assert
        assertEquals(ObjectiveType.SMA, objective.getTargetType());
        assertEquals(ObjectiveType.SMA, objective.getStopLossType());
        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should create StrategyObjective with FIXED_PRICE type")
    void shouldCreateStrategyObjectiveWithFixedPriceType() {
        // Arrange & Act
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.FIXED_PRICE)
                .stopLossType(ObjectiveType.FIXED_PRICE)
                .targetValue(BigDecimal.valueOf(150.00))
                .stopLossValue(BigDecimal.valueOf(140.00))
                .capitalToRisk(BigDecimal.valueOf(0.05))
                .description("Fixed price strategy with $150 target and $140 stop-loss")
                .build();

        // Assert
        assertEquals(ObjectiveType.FIXED_PRICE, objective.getTargetType());
        assertEquals(ObjectiveType.FIXED_PRICE, objective.getStopLossType());
        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should create StrategyObjective with mixed objective types")
    void shouldCreateStrategyObjectiveWithMixedObjectiveTypes() {
        // Arrange & Act
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.FIXED_PRICE)
                .targetValue(BigDecimal.valueOf(10.0))
                .stopLossValue(BigDecimal.valueOf(95.00))
                .capitalToRisk(BigDecimal.valueOf(0.03))
                .description("Mixed strategy: 10% target, $95 fixed stop-loss")
                .build();

        // Assert
        assertEquals(ObjectiveType.PERCENTAGE, objective.getTargetType());
        assertEquals(ObjectiveType.FIXED_PRICE, objective.getStopLossType());
        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should throw exception when targetType is null")
    void shouldThrowExceptionWhenTargetTypeIsNull() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(null)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("targetType cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when stopLossType is null")
    void shouldThrowExceptionWhenStopLossTypeIsNull() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(null)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("stopLossType cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when targetValue is null")
    void shouldThrowExceptionWhenTargetValueIsNull() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(null)
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("targetValue cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when stopLossValue is null")
    void shouldThrowExceptionWhenStopLossValueIsNull() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(null)
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("stopLossValue cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when capitalToRisk is null")
    void shouldThrowExceptionWhenCapitalToRiskIsNull() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(null)
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("capitalToRisk cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when description is null")
    void shouldThrowExceptionWhenDescriptionIsNull() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description(null)
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("description cannot be null or blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when description is blank")
    void shouldThrowExceptionWhenDescriptionIsBlank() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("   ")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("description cannot be null or blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when targetValue is zero")
    void shouldThrowExceptionWhenTargetValueIsZero() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.ZERO)
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("targetValue must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when targetValue is negative")
    void shouldThrowExceptionWhenTargetValueIsNegative() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(-5.0))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("targetValue must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when stopLossValue is zero")
    void shouldThrowExceptionWhenStopLossValueIsZero() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.ZERO)
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("stopLossValue must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when stopLossValue is negative")
    void shouldThrowExceptionWhenStopLossValueIsNegative() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(-2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("stopLossValue must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when capitalToRisk is zero")
    void shouldThrowExceptionWhenCapitalToRiskIsZero() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.ZERO)
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("capitalToRisk must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when capitalToRisk is negative")
    void shouldThrowExceptionWhenCapitalToRiskIsNegative() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(-0.02))
                .description("Test strategy")
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertEquals("capitalToRisk must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should maintain immutability of all fields")
    void shouldMaintainImmutabilityOfAllFields() {
        // Arrange
        ObjectiveType originalTargetType = ObjectiveType.PERCENTAGE;
        ObjectiveType originalStopLossType = ObjectiveType.FIXED_PRICE;
        BigDecimal originalTargetValue = BigDecimal.valueOf(10.0);
        BigDecimal originalStopLossValue = BigDecimal.valueOf(100.0);
        BigDecimal originalCapitalToRisk = BigDecimal.valueOf(0.03);
        String originalDescription = "Test strategy";

        // Act
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(originalTargetType)
                .stopLossType(originalStopLossType)
                .targetValue(originalTargetValue)
                .stopLossValue(originalStopLossValue)
                .capitalToRisk(originalCapitalToRisk)
                .description(originalDescription)
                .build();

        // Assert - Values should remain unchanged
        assertEquals(originalTargetType, objective.getTargetType());
        assertEquals(originalStopLossType, objective.getStopLossType());
        assertEquals(originalTargetValue, objective.getTargetValue());
        assertEquals(originalStopLossValue, objective.getStopLossValue());
        assertEquals(originalCapitalToRisk, objective.getCapitalToRisk());
        assertEquals(originalDescription, objective.getDescription());
    }

    @Test
    @DisplayName("Should have proper toString implementation")
    void shouldHaveProperToStringImplementation() {
        // Arrange & Act
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Test strategy")
                .build();

        String toString = objective.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("targetType"));
        assertTrue(toString.contains("PERCENTAGE"));
        assertTrue(toString.contains("targetValue"));
        assertTrue(toString.contains("5.0"));
    }

    @Test
    @DisplayName("Should accept very small positive BigDecimal values")
    void shouldAcceptVerySmallPositiveBigDecimalValues() {
        // Arrange & Act
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(0.0001))
                .stopLossValue(BigDecimal.valueOf(0.0001))
                .capitalToRisk(BigDecimal.valueOf(0.0001))
                .description("Micro-adjustment strategy")
                .build();

        // Assert
        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should accept very large BigDecimal values")
    void shouldAcceptVeryLargeBigDecimalValues() {
        // Arrange & Act
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.FIXED_PRICE)
                .stopLossType(ObjectiveType.FIXED_PRICE)
                .targetValue(BigDecimal.valueOf(1000000.0))
                .stopLossValue(BigDecimal.valueOf(999000.0))
                .capitalToRisk(BigDecimal.valueOf(100.0))
                .description("High-value strategy")
                .build();

        // Assert
        assertDoesNotThrow(objective::validate);
    }

    // ---- Phase 3: SMA period validation via RuleCapabilityCatalog ----

    @Test
    @DisplayName("Should validate SMA target with valid period 20")
    void shouldValidateSmaTargetWithValidPeriod20() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(20))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("SMA20 target strategy")
                .build();

        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should validate SMA target with valid period 50")
    void shouldValidateSmaTargetWithValidPeriod50() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(50))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("SMA50 target strategy")
                .build();

        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should validate SMA target with valid period 200")
    void shouldValidateSmaTargetWithValidPeriod200() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(200))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("SMA200 target strategy")
                .build();

        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should validate SMA stop-loss with valid period 200")
    void shouldValidateSmaStopLossWithValidPeriod200() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.SMA)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(200))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("SMA200 stop-loss strategy")
                .build();

        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should throw exception when SMA target period is invalid")
    void shouldThrowExceptionWhenSmaTargetPeriodIsInvalid() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(100))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Invalid SMA target")
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertTrue(exception.getMessage().contains("targetValue"));
        assertTrue(exception.getMessage().contains("100"));
        assertTrue(exception.getMessage().contains("not a valid SMA period"));
    }

    @Test
    @DisplayName("Should throw exception when SMA stop-loss period is invalid")
    void shouldThrowExceptionWhenSmaStopLossPeriodIsInvalid() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.SMA)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossValue(BigDecimal.valueOf(15))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Invalid SMA stop-loss")
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertTrue(exception.getMessage().contains("stopLossValue"));
        assertTrue(exception.getMessage().contains("15"));
        assertTrue(exception.getMessage().contains("not a valid SMA period"));
    }

    @Test
    @DisplayName("Should throw exception when both SMA target and stop-loss periods are invalid")
    void shouldThrowExceptionWhenBothSmaPeriodAreInvalid() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.SMA)
                .targetValue(BigDecimal.valueOf(99))
                .stopLossValue(BigDecimal.valueOf(300))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Both invalid SMA")
                .build();

        // Should fail on targetValue first
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, objective::validate);
        assertTrue(exception.getMessage().contains("targetValue"));
        assertTrue(exception.getMessage().contains("99"));
    }

    @Test
    @DisplayName("Should not validate SMA period for non-SMA types")
    void shouldNotValidateSmaPeriodForNonSmaTypes() {
        // PERCENTAGE type with value 100 should be valid (not treated as SMA period)
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.FIXED_PRICE)
                .targetValue(BigDecimal.valueOf(100))
                .stopLossValue(BigDecimal.valueOf(95))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Non-SMA strategy with value 100")
                .build();

        assertDoesNotThrow(objective::validate);
    }

    @Test
    @DisplayName("Should validate mixed SMA and non-SMA types correctly")
    void shouldValidateMixedSmaAndNonSmaTypes() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(50))
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Mixed valid strategy")
                .build();

        assertDoesNotThrow(objective::validate);
    }

    // ---- Phase 5: Advanced coherence warnings ----

    @Test
    @DisplayName("Should return no warnings for valid percentage strategy")
    void shouldReturnNoWarningsForValidPercentageStrategy() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(10.0))
                .stopLossValue(BigDecimal.valueOf(5.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Normal percentage strategy")
                .build();

        assertTrue(objective.collectWarnings().isEmpty());
    }

    @Test
    @DisplayName("Should warn when stop-loss percentage exceeds 20%")
    void shouldWarnWhenStopLossPercentageExceeds20() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(10.0))
                .stopLossValue(BigDecimal.valueOf(25.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("High stop-loss strategy")
                .build();

        var warnings = objective.collectWarnings();
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("Stop-loss percentage"));
        assertTrue(warnings.get(0).contains("25.00%"));
        assertTrue(warnings.get(0).contains("20%"));
    }

    @Test
    @DisplayName("Should not warn when stop-loss percentage is exactly 20%")
    void shouldNotWarnWhenStopLossPercentageIsExactly20() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(10.0))
                .stopLossValue(BigDecimal.valueOf(20.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Boundary stop-loss strategy")
                .build();

        assertTrue(objective.collectWarnings().isEmpty());
    }

    @Test
    @DisplayName("Should not warn about stop-loss when type is not PERCENTAGE")
    void shouldNotWarnAboutStopLossWhenTypeIsNotPercentage() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.FIXED_PRICE)
                .targetValue(BigDecimal.valueOf(10.0))
                .stopLossValue(BigDecimal.valueOf(25.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Fixed price stop-loss")
                .build();

        assertTrue(objective.collectWarnings().isEmpty());
    }

    @Test
    @DisplayName("Should warn when target percentage exceeds 100%")
    void shouldWarnWhenTargetPercentageExceeds100() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(150.0))
                .stopLossValue(BigDecimal.valueOf(5.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Long-term target strategy")
                .build();

        var warnings = objective.collectWarnings();
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("Target percentage"));
        assertTrue(warnings.get(0).contains("150.00%"));
        assertTrue(warnings.get(0).contains("100%"));
    }

    @Test
    @DisplayName("Should not warn when target percentage is exactly 100%")
    void shouldNotWarnWhenTargetPercentageIsExactly100() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(100.0))
                .stopLossValue(BigDecimal.valueOf(5.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("100% target strategy")
                .build();

        assertTrue(objective.collectWarnings().isEmpty());
    }

    @Test
    @DisplayName("Should not warn about target when type is not PERCENTAGE")
    void shouldNotWarnAboutTargetWhenTypeIsNotPercentage() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.FIXED_PRICE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(150.0))
                .stopLossValue(BigDecimal.valueOf(5.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Fixed price target")
                .build();

        assertTrue(objective.collectWarnings().isEmpty());
    }

    @Test
    @DisplayName("Should warn when both SMA types use the same period")
    void shouldWarnWhenBothSmaTypesUseSamePeriod() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.SMA)
                .targetValue(BigDecimal.valueOf(50))
                .stopLossValue(BigDecimal.valueOf(50))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Same SMA period strategy")
                .build();

        var warnings = objective.collectWarnings();
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("same SMA period"));
        assertTrue(warnings.get(0).contains("50"));
    }

    @Test
    @DisplayName("Should not warn when SMA types use different periods")
    void shouldNotWarnWhenSmaTypesUseDifferentPeriods() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.SMA)
                .targetValue(BigDecimal.valueOf(20))
                .stopLossValue(BigDecimal.valueOf(50))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Different SMA periods strategy")
                .build();

        assertTrue(objective.collectWarnings().isEmpty());
    }

    @Test
    @DisplayName("Should not warn about same SMA when only one type is SMA")
    void shouldNotWarnAboutSameSmaWhenOnlyOneTypeIsSma() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.SMA)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(50))
                .stopLossValue(BigDecimal.valueOf(5.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Mixed types same value")
                .build();

        assertTrue(objective.collectWarnings().isEmpty());
    }

    @Test
    @DisplayName("Should return multiple warnings when multiple conditions are met")
    void shouldReturnMultipleWarningsWhenMultipleConditionsMet() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(150.0))
                .stopLossValue(BigDecimal.valueOf(25.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Multiple warnings strategy")
                .build();

        var warnings = objective.collectWarnings();
        assertEquals(2, warnings.size());
        assertTrue(warnings.get(0).contains("Stop-loss percentage"));
        assertTrue(warnings.get(1).contains("Target percentage"));
    }

    @Test
    @DisplayName("Should return unmodifiable list from collectWarnings")
    void shouldReturnUnmodifiableListFromCollectWarnings() {
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .stopLossType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(150.0))
                .stopLossValue(BigDecimal.valueOf(25.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Unmodifiable test")
                .build();

        var warnings = objective.collectWarnings();
        assertThrows(UnsupportedOperationException.class, () -> warnings.add("illegal"));
    }
}
