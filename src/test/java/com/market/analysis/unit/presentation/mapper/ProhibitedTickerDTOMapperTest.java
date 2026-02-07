package com.market.analysis.unit.presentation.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.presentation.dto.ProhibitedTickerDTO;
import com.market.analysis.presentation.mapper.ProhibitedTickerDTOMapper;

/**
 * Unit tests for ProhibitedTickerDTOMapper.
 */
@DisplayName("ProhibitedTickerDTOMapper Unit Tests")
class ProhibitedTickerDTOMapperTest {

    private ProhibitedTickerDTOMapper prohibitedTickerDTOMapper;

    @BeforeEach
    void setUp() {
        prohibitedTickerDTOMapper = new ProhibitedTickerDTOMapper();
    }

    @Test
    @DisplayName("Should map ProhibitedTicker domain to ProhibitedTickerDTO")
    void testToDTO() {
        // Arrange
        ProhibitedTicker prohibitedTicker = new ProhibitedTicker("AAPL");

        // Act
        ProhibitedTickerDTO dto = prohibitedTickerDTOMapper.toDTO(prohibitedTicker);

        // Assert
        assertNotNull(dto);
        assertEquals("AAPL", dto.getTicker());
    }

    @Test
    @DisplayName("Should map ProhibitedTickerDTO to ProhibitedTicker domain")
    void testToDomain() {
        // Arrange
        ProhibitedTickerDTO dto = ProhibitedTickerDTO.builder()
                .id(1L)
                .ticker("GOOGL")
                .reason("Test reason")
                .createdAt(LocalDateTime.now())
                .build();

        // Act
        ProhibitedTicker prohibitedTicker = prohibitedTickerDTOMapper.toDomain(dto);

        // Assert
        assertNotNull(prohibitedTicker);
        assertEquals("GOOGL", prohibitedTicker.getTicker());
    }

    @Test
    @DisplayName("Should return null when mapping null ProhibitedTicker to DTO")
    void testToDTOWithNull() {
        // Act
        ProhibitedTickerDTO dto = prohibitedTickerDTOMapper.toDTO(null);

        // Assert
        assertNull(dto);
    }

    @Test
    @DisplayName("Should return null when mapping null ProhibitedTickerDTO to domain")
    void testToDomainWithNull() {
        // Act
        ProhibitedTicker prohibitedTicker = prohibitedTickerDTOMapper.toDomain(null);

        // Assert
        assertNull(prohibitedTicker);
    }

    @Test
    @DisplayName("Should correctly map ticker with special characters")
    void testToDTOWithSpecialCharacters() {
        // Arrange
        ProhibitedTicker prohibitedTicker = new ProhibitedTicker("BRK.B");

        // Act
        ProhibitedTickerDTO dto = prohibitedTickerDTOMapper.toDTO(prohibitedTicker);

        // Assert
        assertNotNull(dto);
        assertEquals("BRK.B", dto.getTicker());
    }
}
