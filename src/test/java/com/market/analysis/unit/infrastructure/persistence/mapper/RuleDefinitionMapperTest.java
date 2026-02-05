package com.market.analysis.unit.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.infrastructure.persistence.entity.RuleDefinitionEntity;
import com.market.analysis.infrastructure.persistence.mapper.RuleDefinitionMapper;

/**
 * Unit tests for RuleDefinitionMapper.
 */
@DisplayName("RuleDefinitionMapper Unit Tests")
class RuleDefinitionMapperTest {

    private RuleDefinitionMapper ruleDefinitionMapper;

    @BeforeEach
    void setUp() {
        ruleDefinitionMapper = new RuleDefinitionMapper();
    }

    @Test
    @DisplayName("Should map RuleDefinition domain to RuleDefinitionEntity")
    void testToEntity() {
        // Arrange
        RuleDefinition ruleDefinition = RuleDefinition.builder()
                .id(1L)
                .code("SMA")
                .name("Simple Moving Average")
                .requiresParam(true)
                .description("Moving average over a period")
                .build();

        // Act
        RuleDefinitionEntity entity = ruleDefinitionMapper.toEntity(ruleDefinition);

        // Assert
        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals("SMA", entity.getCode());
        assertEquals("Simple Moving Average", entity.getName());
        assertTrue(entity.isRequiresParam());
        assertEquals("Moving average over a period", entity.getDescription());
    }

    @Test
    @DisplayName("Should map RuleDefinitionEntity to RuleDefinition domain")
    void testToDomain() {
        // Arrange
        RuleDefinitionEntity entity = new RuleDefinitionEntity();
        entity.setId(2L);
        entity.setCode("RSI");
        entity.setName("Relative Strength Index");
        entity.setRequiresParam(true);
        entity.setDescription("Momentum oscillator");

        // Act
        RuleDefinition ruleDefinition = ruleDefinitionMapper.toDomain(entity);

        // Assert
        assertNotNull(ruleDefinition);
        assertEquals(2L, ruleDefinition.getId());
        assertEquals("RSI", ruleDefinition.getCode());
        assertEquals("Relative Strength Index", ruleDefinition.getName());
        assertTrue(ruleDefinition.isRequiresParam());
        assertEquals("Momentum oscillator", ruleDefinition.getDescription());
    }

    @Test
    @DisplayName("Should return null when mapping null RuleDefinition to entity")
    void testToEntityWithNull() {
        // Act
        RuleDefinitionEntity entity = ruleDefinitionMapper.toEntity(null);

        // Assert
        assertNull(entity);
    }

    @Test
    @DisplayName("Should return null when mapping null RuleDefinitionEntity to domain")
    void testToDomainWithNull() {
        // Act
        RuleDefinition ruleDefinition = ruleDefinitionMapper.toDomain(null);

        // Assert
        assertNull(ruleDefinition);
    }

    @Test
    @DisplayName("Should correctly map requiresParam false value")
    void testToEntityWithRequiresParamFalse() {
        // Arrange
        RuleDefinition ruleDefinition = RuleDefinition.builder()
                .id(3L)
                .code("PRICE")
                .name("Current Price")
                .requiresParam(false)
                .description("The current market price")
                .build();

        // Act
        RuleDefinitionEntity entity = ruleDefinitionMapper.toEntity(ruleDefinition);

        // Assert
        assertNotNull(entity);
        assertFalse(entity.isRequiresParam());
    }
}
