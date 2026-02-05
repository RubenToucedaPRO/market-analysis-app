package com.market.analysis.unit.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.Rule;
import com.market.analysis.infrastructure.persistence.entity.RuleEntity;
import com.market.analysis.infrastructure.persistence.mapper.RuleMapper;

/**
 * Unit tests for RuleMapper.
 */
@DisplayName("RuleMapper Unit Tests")
class RuleMapperTest {

    private RuleMapper ruleMapper;

    @BeforeEach
    void setUp() {
        ruleMapper = new RuleMapper();
    }

    @Test
    @DisplayName("Should map Rule domain to RuleEntity")
    void testToDomainToEntity() {
        // Arrange
        Rule rule = Rule.builder()
                .id(1L)
                .name("SMA Crossover")
                .subjectCode("SMA")
                .subjectParam(50.0)
                .operator(">")
                .targetCode("SMA")
                .targetParam(200.0)
                .description("50-day SMA crosses above 200-day SMA")
                .build();

        // Act
        RuleEntity entity = ruleMapper.toEntity(rule);

        // Assert
        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals("SMA Crossover", entity.getName());
        assertEquals("SMA", entity.getSubjectCode());
        assertEquals(50.0, entity.getSubjectParam());
        assertEquals(">", entity.getOperator());
        assertEquals("SMA", entity.getTargetCode());
        assertEquals(200.0, entity.getTargetParam());
        assertEquals("50-day SMA crosses above 200-day SMA", entity.getDescription());
    }

    @Test
    @DisplayName("Should map RuleEntity to Rule domain")
    void testEntityToDomain() {
        // Arrange
        RuleEntity entity = new RuleEntity();
        entity.setId(2L);
        entity.setName("RSI Overbought");
        entity.setSubjectCode("RSI");
        entity.setSubjectParam(14.0);
        entity.setOperator(">");
        entity.setTargetCode("CONSTANT");
        entity.setTargetParam(70.0);
        entity.setDescription("RSI above 70 indicates overbought");

        // Act
        Rule rule = ruleMapper.toDomain(entity);

        // Assert
        assertNotNull(rule);
        assertEquals(2L, rule.getId());
        assertEquals("RSI Overbought", rule.getName());
        assertEquals("RSI", rule.getSubjectCode());
        assertEquals(14.0, rule.getSubjectParam());
        assertEquals(">", rule.getOperator());
        assertEquals("CONSTANT", rule.getTargetCode());
        assertEquals(70.0, rule.getTargetParam());
        assertEquals("RSI above 70 indicates overbought", rule.getDescription());
    }

    @Test
    @DisplayName("Should handle null when converting domain to entity")
    void testToEntityWithNull() {
        // Act
        RuleEntity entity = ruleMapper.toEntity(null);

        // Assert
        assertNull(entity);
    }

    @Test
    @DisplayName("Should handle null when converting entity to domain")
    void testToDomainWithNull() {
        // Act
        Rule rule = ruleMapper.toDomain(null);

        // Assert
        assertNull(rule);
    }

    @Test
    @DisplayName("Should map rule with null parameters")
    void testMapRuleWithNullParams() {
        // Arrange
        Rule rule = Rule.builder()
                .id(3L)
                .name("Price Check")
                .subjectCode("PRICE")
                .subjectParam(null)
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .description("Price above 100")
                .build();

        // Act
        RuleEntity entity = ruleMapper.toEntity(rule);
        Rule mappedBack = ruleMapper.toDomain(entity);

        // Assert
        assertNotNull(entity);
        assertNull(entity.getSubjectParam());
        assertNotNull(mappedBack);
        assertNull(mappedBack.getSubjectParam());
    }

    @Test
    @DisplayName("Should preserve all fields during round-trip conversion")
    void testRoundTripConversion() {
        // Arrange
        Rule originalRule = Rule.builder()
                .id(4L)
                .name("Volume Rule")
                .subjectCode("VOLUME")
                .subjectParam(null)
                .operator(">")
                .targetCode("AVG_VOLUME")
                .targetParam(20.0)
                .description("Volume above 20-day average")
                .build();

        // Act
        RuleEntity entity = ruleMapper.toEntity(originalRule);
        Rule convertedRule = ruleMapper.toDomain(entity);

        // Assert
        assertEquals(originalRule.getId(), convertedRule.getId());
        assertEquals(originalRule.getName(), convertedRule.getName());
        assertEquals(originalRule.getSubjectCode(), convertedRule.getSubjectCode());
        assertEquals(originalRule.getSubjectParam(), convertedRule.getSubjectParam());
        assertEquals(originalRule.getOperator(), convertedRule.getOperator());
        assertEquals(originalRule.getTargetCode(), convertedRule.getTargetCode());
        assertEquals(originalRule.getTargetParam(), convertedRule.getTargetParam());
        assertEquals(originalRule.getDescription(), convertedRule.getDescription());
    }
}
