package com.market.analysis.unit.presentation.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.Rule;
import com.market.analysis.presentation.dto.RuleDTO;
import com.market.analysis.presentation.mapper.RuleDTOMapper;

/**
 * Unit tests for RuleDTOMapper.
 */
@DisplayName("RuleDTOMapper Unit Tests")
class RuleDTOMapperTest {

    private RuleDTOMapper mapper = new RuleDTOMapper();

    @Test
    @DisplayName("Should convert Rule domain model to RuleDTO")
    void testRuleToDTOConversion() {
        // Arrange
        Rule rule = Rule.builder()
                .id(1L)
                .name("SMA Crossover")
                .subjectCode("SMA")
                .subjectParam(50.0)
                .operator(">")
                .targetCode("SMA")
                .targetParam(200.0)
                .description("SMA 50 crosses above SMA 200")
                .build();

        // Act
        RuleDTO dto = mapper.toDTO(rule);

        // Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("SMA Crossover", dto.getName());
        assertEquals("SMA", dto.getSubjectCode());
        assertEquals(50.0, dto.getSubjectParam());
        assertEquals(">", dto.getOperator());
        assertEquals("SMA", dto.getTargetCode());
        assertEquals(200.0, dto.getTargetParam());
        assertEquals("SMA 50 crosses above SMA 200", dto.getDescription());
    }

    @Test
    @DisplayName("Should convert RuleDTO to Rule domain model")
    void testDTOToRuleDomainConversion() {
        // Arrange
        RuleDTO dto = RuleDTO.builder()
                .id(1L)
                .name("RSI Overbought")
                .subjectCode("RSI")
                .subjectParam(14.0)
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(70.0)
                .description("RSI 14 above 70")
                .build();

        // Act
        Rule rule = mapper.toDomain(dto);

        // Assert
        assertNotNull(rule);
        assertEquals(1L, rule.getId());
        assertEquals("RSI Overbought", rule.getName());
        assertEquals("RSI", rule.getSubjectCode());
        assertEquals(14.0, rule.getSubjectParam());
        assertEquals(">", rule.getOperator());
        assertEquals("CONSTANT", rule.getTargetCode());
        assertEquals(70.0, rule.getTargetParam());
        assertEquals("RSI 14 above 70", rule.getDescription());
    }

    @Test
    @DisplayName("Should handle null Rule in toDTO")
    void testRuleToDTOWithNull() {
        // Act
        RuleDTO dto = mapper.toDTO(null);

        // Assert
        assertNull(dto);
    }

    @Test
    @DisplayName("Should handle null RuleDTO in toDomain")
    void testDTOToRuleWithNull() {
        // Act
        Rule rule = mapper.toDomain(null);

        // Assert
        assertNull(rule);
    }

    @Test
    @DisplayName("Should convert Rule with null parameters")
    void testRuleWithNullParameters() {
        // Arrange
        Rule rule = Rule.builder()
                .id(2L)
                .name("Price Check")
                .subjectCode("PRICE")
                .subjectParam(null)
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .description("Price above 100")
                .build();

        // Act
        RuleDTO dto = mapper.toDTO(rule);

        // Assert
        assertNotNull(dto);
        assertNull(dto.getSubjectParam());
        assertEquals(100.0, dto.getTargetParam());
    }
}
