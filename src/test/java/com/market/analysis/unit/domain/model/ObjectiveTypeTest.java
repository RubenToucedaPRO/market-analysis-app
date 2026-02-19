package com.market.analysis.unit.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.ObjectiveType;

@DisplayName("ObjectiveType Enum Tests")
class ObjectiveTypeTest {

    @Test
    @DisplayName("Should have SMA value")
    void shouldHaveSMAValue() {
        // Act
        ObjectiveType type = ObjectiveType.SMA;

        // Assert
        assertNotNull(type);
        assertEquals("SMA", type.name());
    }

    @Test
    @DisplayName("Should have PERCENTAGE value")
    void shouldHavePercentageValue() {
        // Act
        ObjectiveType type = ObjectiveType.PERCENTAGE;

        // Assert
        assertNotNull(type);
        assertEquals("PERCENTAGE", type.name());
    }

    @Test
    @DisplayName("Should have FIXED_PRICE value")
    void shouldHaveFixedPriceValue() {
        // Act
        ObjectiveType type = ObjectiveType.FIXED_PRICE;

        // Assert
        assertNotNull(type);
        assertEquals("FIXED_PRICE", type.name());
    }

    @Test
    @DisplayName("Should return all enum values")
    void shouldReturnAllEnumValues() {
        // Act
        ObjectiveType[] values = ObjectiveType.values();

        // Assert
        assertEquals(3, values.length);
        assertArrayEquals(new ObjectiveType[] {
            ObjectiveType.SMA,
            ObjectiveType.PERCENTAGE,
            ObjectiveType.FIXED_PRICE
        }, values);
    }

    @Test
    @DisplayName("Should parse enum from string")
    void shouldParseEnumFromString() {
        // Act
        ObjectiveType sma = ObjectiveType.valueOf("SMA");
        ObjectiveType percentage = ObjectiveType.valueOf("PERCENTAGE");
        ObjectiveType fixedPrice = ObjectiveType.valueOf("FIXED_PRICE");

        // Assert
        assertEquals(ObjectiveType.SMA, sma);
        assertEquals(ObjectiveType.PERCENTAGE, percentage);
        assertEquals(ObjectiveType.FIXED_PRICE, fixedPrice);
    }
}
