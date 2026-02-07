package com.market.analysis.unit.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.infrastructure.persistence.entity.ProhibitedTickerEntity;
import com.market.analysis.infrastructure.persistence.mapper.ProhibitedTickerMapper;

/**
 * Unit tests for ProhibitedTickerMapper.
 */
@DisplayName("ProhibitedTickerMapper Unit Tests")
class ProhibitedTickerMapperTest {

    private ProhibitedTickerMapper prohibitedTickerMapper;

    @BeforeEach
    void setUp() {
        prohibitedTickerMapper = new ProhibitedTickerMapper();
    }

    @Test
    @DisplayName("Should map ProhibitedTicker domain to ProhibitedTickerEntity")
    void testToEntity() {
        // Arrange
        ProhibitedTicker prohibitedTicker = new ProhibitedTicker("AAPL");

        // Act
        ProhibitedTickerEntity entity = prohibitedTickerMapper.toEntity(prohibitedTicker);

        // Assert
        assertNotNull(entity);
        assertEquals("AAPL", entity.getTicker());
    }

    @Test
    @DisplayName("Should map ProhibitedTickerEntity to ProhibitedTicker domain")
    void testToDomain() {
        // Arrange
        ProhibitedTickerEntity entity = new ProhibitedTickerEntity();
        entity.setId(1L);
        entity.setTicker("GOOGL");
        entity.setReason("Test reason");

        // Act
        ProhibitedTicker prohibitedTicker = prohibitedTickerMapper.toDomain(entity);

        // Assert
        assertNotNull(prohibitedTicker);
        assertEquals("GOOGL", prohibitedTicker.getTicker());
    }

    @Test
    @DisplayName("Should return null when mapping null ProhibitedTicker to entity")
    void testToEntityWithNull() {
        // Act
        ProhibitedTickerEntity entity = prohibitedTickerMapper.toEntity(null);

        // Assert
        assertNull(entity);
    }

    @Test
    @DisplayName("Should return null when mapping null ProhibitedTickerEntity to domain")
    void testToDomainWithNull() {
        // Act
        ProhibitedTicker prohibitedTicker = prohibitedTickerMapper.toDomain(null);

        // Assert
        assertNull(prohibitedTicker);
    }

    @Test
    @DisplayName("Should correctly map ticker with special characters")
    void testToEntityWithSpecialCharacters() {
        // Arrange
        ProhibitedTicker prohibitedTicker = new ProhibitedTicker("BRK.B");

        // Act
        ProhibitedTickerEntity entity = prohibitedTickerMapper.toEntity(prohibitedTicker);

        // Assert
        assertNotNull(entity);
        assertEquals("BRK.B", entity.getTicker());
    }
}
