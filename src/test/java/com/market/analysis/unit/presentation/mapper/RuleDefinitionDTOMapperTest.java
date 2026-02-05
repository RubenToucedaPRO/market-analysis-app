package com.market.analysis.unit.presentation.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.presentation.dto.RuleDefinitionDTO;
import com.market.analysis.presentation.mapper.RuleDefinitionDTOMapper;

/**
 * Unit tests for RuleDefinitionDTOMapper.
 */
@DisplayName("RuleDefinitionDTOMapper Unit Tests")
class RuleDefinitionDTOMapperTest {

    private RuleDefinitionDTOMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RuleDefinitionDTOMapper();
    }

    @Test
    @DisplayName("Should map RuleDefinition domain to RuleDefinitionDTO")
    void testToDTO() {
        // Arrange
        RuleDefinition ruleDefinition = RuleDefinition.builder()
                .id(1L)
                .code("SMA")
                .name("Simple Moving Average")
                .requiresParam(true)
                .description("Moving average over a period")
                .build();

        // Act
        RuleDefinitionDTO dto = mapper.toDTO(ruleDefinition);

        // Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("SMA", dto.getCode());
        assertEquals("Simple Moving Average", dto.getName());
        assertTrue(dto.isRequiresParam());
        assertEquals("Moving average over a period", dto.getDescription());
    }

    @Test
    @DisplayName("Should map RuleDefinitionDTO to RuleDefinition domain")
    void testToDomain() {
        // Arrange
        RuleDefinitionDTO dto = RuleDefinitionDTO.builder()
                .id(2L)
                .code("RSI")
                .name("Relative Strength Index")
                .requiresParam(true)
                .description("Momentum oscillator")
                .build();

        // Act
        RuleDefinition ruleDefinition = mapper.toDomain(dto);

        // Assert
        assertNotNull(ruleDefinition);
        assertEquals(2L, ruleDefinition.getId());
        assertEquals("RSI", ruleDefinition.getCode());
        assertEquals("Relative Strength Index", ruleDefinition.getName());
        assertTrue(ruleDefinition.isRequiresParam());
        assertEquals("Momentum oscillator", ruleDefinition.getDescription());
    }

    @Test
    @DisplayName("Should return null when mapping null RuleDefinition to DTO")
    void testToDTOWithNull() {
        // Act
        RuleDefinitionDTO dto = mapper.toDTO(null);

        // Assert
        assertNull(dto);
    }

    @Test
    @DisplayName("Should return null when mapping null RuleDefinitionDTO to domain")
    void testToDomainWithNull() {
        // Act
        RuleDefinition ruleDefinition = mapper.toDomain(null);

        // Assert
        assertNull(ruleDefinition);
    }

    @Test
    @DisplayName("Should correctly map requiresParam false value")
    void testToDTOWithRequiresParamFalse() {
        // Arrange
        RuleDefinition ruleDefinition = RuleDefinition.builder()
                .id(3L)
                .code("PRICE")
                .name("Current Price")
                .requiresParam(false)
                .description("The current market price")
                .build();

        // Act
        RuleDefinitionDTO dto = mapper.toDTO(ruleDefinition);

        // Assert
        assertNotNull(dto);
        assertEquals(false, dto.isRequiresParam());
    }
}
