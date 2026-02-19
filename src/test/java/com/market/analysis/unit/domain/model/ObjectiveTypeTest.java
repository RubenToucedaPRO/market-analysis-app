package com.market.analysis.unit.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.ObjectiveType;

/**
 * Unit tests for ObjectiveType enum.
 */
@DisplayName("ObjectiveType Enum Tests")
class ObjectiveTypeTest {

    @Test
    @DisplayName("Should have SMA type")
    void shouldHaveSmaType() {
        // Act & Assert
        assertThat(ObjectiveType.SMA).isNotNull();
        assertThat(ObjectiveType.SMA.name()).isEqualTo("SMA");
    }

    @Test
    @DisplayName("Should have PERCENTAGE type")
    void shouldHavePercentageType() {
        // Act & Assert
        assertThat(ObjectiveType.PERCENTAGE).isNotNull();
        assertThat(ObjectiveType.PERCENTAGE.name()).isEqualTo("PERCENTAGE");
    }

    @Test
    @DisplayName("Should have FIXED_PRICE type")
    void shouldHaveFixedPriceType() {
        // Act & Assert
        assertThat(ObjectiveType.FIXED_PRICE).isNotNull();
        assertThat(ObjectiveType.FIXED_PRICE.name()).isEqualTo("FIXED_PRICE");
    }

    @Test
    @DisplayName("Should have exactly three types")
    void shouldHaveExactlyThreeTypes() {
        // Act
        ObjectiveType[] types = ObjectiveType.values();

        // Assert
        assertThat(types).hasSize(3);
        assertThat(types).containsExactlyInAnyOrder(
                ObjectiveType.SMA,
                ObjectiveType.PERCENTAGE,
                ObjectiveType.FIXED_PRICE
        );
    }

    @Test
    @DisplayName("Should support valueOf for all types")
    void shouldSupportValueOfForAllTypes() {
        // Act & Assert
        assertThat(ObjectiveType.valueOf("SMA")).isEqualTo(ObjectiveType.SMA);
        assertThat(ObjectiveType.valueOf("PERCENTAGE")).isEqualTo(ObjectiveType.PERCENTAGE);
        assertThat(ObjectiveType.valueOf("FIXED_PRICE")).isEqualTo(ObjectiveType.FIXED_PRICE);
    }
}
