package com.market.analysis.unit.application.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.application.dto.RuleDTO;
import com.market.analysis.application.dto.StrategyDTO;
import com.market.analysis.application.dto.StrategyObjectiveDTO;
import com.market.analysis.application.mapper.RuleDTOMapper;
import com.market.analysis.application.mapper.StrategyDTOMapper;
import com.market.analysis.domain.model.ObjectiveType;
import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.StrategyObjective;

/**
 * Unit tests for StrategyDTOMapper.
 */
@DisplayName("StrategyDTOMapper Unit Tests")
class StrategyDTOMapperTest {

    private final RuleDTOMapper ruleDTOMapper = new RuleDTOMapper();
    private final StrategyDTOMapper mapper = new StrategyDTOMapper(ruleDTOMapper);

    @Test
    @DisplayName("Should convert Strategy domain model to StrategyDTO")
    void testStrategyToDTOConversion() {
        // Arrange
        Rule rule1 = Rule.builder()
                .id(1L)
                .name("SMA Rule")
                .subjectCode("SMA")
                .subjectParam(50.0)
                .operator(">")
                .targetCode("SMA")
                .targetParam(200.0)
                .build();

        Strategy strategy = Strategy.builder()
                .id(10L)
                .name("Trend Strategy")
                .description("A trend following strategy")
                .rules(List.of(rule1))
                .build();

        // Act
        StrategyDTO dto = mapper.toDTO(strategy);

        // Assert
        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("Trend Strategy", dto.getName());
        assertEquals("A trend following strategy", dto.getDescription());
        assertNotNull(dto.getRules());
        assertEquals(1, dto.getRules().size());
        assertEquals("SMA Rule", dto.getRules().get(0).getName());
    }

    @Test
    @DisplayName("Should convert StrategyDTO to Strategy domain model")
    void testDTOToStrategyDomainConversion() {
        // Arrange
        RuleDTO ruleDTO1 = RuleDTO.builder()
                .id(1L)
                .name("RSI Rule")
                .subjectCode("RSI")
                .subjectParam(14.0)
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(70.0)
                .build();

        StrategyDTO dto = StrategyDTO.builder()
                .id(10L)
                .name("Momentum Strategy")
                .description("A momentum based strategy")
                .rules(List.of(ruleDTO1))
                .build();

        // Act
        Strategy strategy = mapper.toDomain(dto);

        // Assert
        assertNotNull(strategy);
        assertEquals(10L, strategy.getId());
        assertEquals("Momentum Strategy", strategy.getName());
        assertEquals("A momentum based strategy", strategy.getDescription());
        assertNotNull(strategy.getRules());
        assertEquals(1, strategy.getRules().size());
        assertEquals("RSI Rule", strategy.getRules().get(0).getName());
    }

    @Test
    @DisplayName("Should handle null Strategy in toDTO")
    void testStrategyToDTOWithNull() {
        // Act
        StrategyDTO dto = mapper.toDTO(null);

        // Assert
        assertNull(dto);
    }

    @Test
    @DisplayName("Should handle null StrategyDTO in toDomain")
    void testDTOToStrategyWithNull() {
        // Act
        Strategy strategy = mapper.toDomain(null);

        // Assert
        assertNull(strategy);
    }

    @Test
    @DisplayName("Should convert Strategy with empty rules list")
    void testStrategyWithEmptyRules() {
        // Arrange
        Strategy strategy = Strategy.builder()
                .id(10L)
                .name("Empty Strategy")
                .description("Strategy with no rules")
                .rules(List.of())
                .build();

        // Act
        StrategyDTO dto = mapper.toDTO(strategy);

        // Assert
        assertNotNull(dto);
        assertNotNull(dto.getRules());
        assertEquals(0, dto.getRules().size());
    }

    @Test
    @DisplayName("Should convert Strategy with multiple rules")
    void testStrategyWithMultipleRules() {
        // Arrange
        Rule rule1 = Rule.builder()
                .id(1L)
                .name("Rule 1")
                .subjectCode("SMA")
                .operator(">")
                .targetCode("SMA")
                .build();

        Rule rule2 = Rule.builder()
                .id(2L)
                .name("Rule 2")
                .subjectCode("RSI")
                .operator("<")
                .targetCode("CONSTANT")
                .build();

        Strategy strategy = Strategy.builder()
                .id(10L)
                .name("Multi Rule Strategy")
                .rules(List.of(rule1, rule2))
                .build();

        // Act
        StrategyDTO dto = mapper.toDTO(strategy);

        // Assert
        assertNotNull(dto);
        assertEquals(2, dto.getRules().size());
        assertEquals("Rule 1", dto.getRules().get(0).getName());
        assertEquals("Rule 2", dto.getRules().get(1).getName());
    }

    @Test
    @DisplayName("Should convert StrategyDTO with null rules list")
    void testDTOWithNullRulesToDomain() {
        // Arrange
        StrategyDTO dto = StrategyDTO.builder()
                .id(10L)
                .name("Null Rules Strategy")
                .description("Strategy with null rules")
                .rules(null)
                .build();

        // Act
        Strategy strategy = mapper.toDomain(dto);

        // Assert
        assertNotNull(strategy);
        assertEquals(10L, strategy.getId());
        assertEquals("Null Rules Strategy", strategy.getName());
        assertNotNull(strategy.getRules());
        assertEquals(0, strategy.getRules().size());
    }

    @Test
    @DisplayName("Should convert Strategy with null rules list")
    void testStrategyWithNullRulesToDTO() {
        // Arrange - Create a Strategy with null rules using builder
        // Note: Strategy.builder().rules(null) converts null to empty list internally,
        // but we test this to ensure the mapper handles whatever the domain returns
        Strategy strategy = Strategy.builder()
                .id(10L)
                .name("Null Rules Strategy")
                .description("Strategy with null rules")
                .rules(null)
                .build();

        // Act
        StrategyDTO dto = mapper.toDTO(strategy);

        // Assert
        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("Null Rules Strategy", dto.getName());
        assertNotNull(dto.getRules());
        assertEquals(0, dto.getRules().size());
    }

    @Test
    @DisplayName("Should map Strategy with objective to StrategyDTO with objective")
    void testStrategyWithObjectiveToDTOConversion() {
        // Arrange
        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .targetValue(new BigDecimal("5.00"))
                .stopLossType(ObjectiveType.PERCENTAGE)
                .stopLossValue(new BigDecimal("2.00"))
                .capitalToRisk(new BigDecimal("1000.00"))
                .description("Objective description")
                .build();

        Strategy strategy = Strategy.builder()
                .id(10L)
                .name("Strategy with objective")
                .description("Description")
                .rules(List.of())
                .objective(objective)
                .build();

        // Act
        StrategyDTO dto = mapper.toDTO(strategy);

        // Assert
        assertNotNull(dto);
        assertNotNull(dto.getObjective());
        assertEquals("PERCENTAGE", dto.getObjective().getTargetType());
        assertEquals(new BigDecimal("5.00"), dto.getObjective().getTargetValue());
        assertEquals("PERCENTAGE", dto.getObjective().getStopLossType());
        assertEquals(new BigDecimal("2.00"), dto.getObjective().getStopLossValue());
        assertEquals(new BigDecimal("1000.00"), dto.getObjective().getCapitalToRisk());
        assertEquals("Objective description", dto.getObjective().getDescription());
    }

    @Test
    @DisplayName("Should map StrategyDTO with objective to Strategy domain model")
    void testDTOWithObjectiveToStrategyDomainConversion() {
        // Arrange
        StrategyObjectiveDTO objectiveDTO = StrategyObjectiveDTO.builder()
                .targetType("FIXED_PRICE")
                .targetValue(new BigDecimal("200.00"))
                .stopLossType("FIXED_PRICE")
                .stopLossValue(new BigDecimal("180.00"))
                .capitalToRisk(new BigDecimal("500.00"))
                .description("Fixed price objective")
                .build();

        StrategyDTO dto = StrategyDTO.builder()
                .id(10L)
                .name("Strategy")
                .description("Description")
                .rules(List.of())
                .objective(objectiveDTO)
                .build();

        // Act
        Strategy strategy = mapper.toDomain(dto);

        // Assert
        assertNotNull(strategy);
        assertNotNull(strategy.getObjective());
        assertEquals(ObjectiveType.FIXED_PRICE, strategy.getObjective().getTargetType());
        assertEquals(new BigDecimal("200.00"), strategy.getObjective().getTargetValue());
        assertEquals(ObjectiveType.FIXED_PRICE, strategy.getObjective().getStopLossType());
        assertEquals(new BigDecimal("180.00"), strategy.getObjective().getStopLossValue());
        assertEquals(new BigDecimal("500.00"), strategy.getObjective().getCapitalToRisk());
        assertEquals("Fixed price objective", strategy.getObjective().getDescription());
    }

    @Test
    @DisplayName("Should handle null objective in Strategy toDTO")
    void testStrategyWithNullObjectiveToDTO() {
        // Arrange
        Strategy strategy = Strategy.builder()
                .id(10L)
                .name("Strategy without objective")
                .description("Description")
                .rules(List.of())
                .objective(null)
                .build();

        // Act
        StrategyDTO dto = mapper.toDTO(strategy);

        // Assert
        assertNotNull(dto);
        assertNull(dto.getObjective());
    }

    @Test
    @DisplayName("Should handle null objective in StrategyDTO toDomain")
    void testDTOWithNullObjectiveToDomain() {
        // Arrange
        StrategyDTO dto = StrategyDTO.builder()
                .id(10L)
                .name("Strategy")
                .description("Description")
                .rules(List.of())
                .objective(null)
                .build();

        // Act
        Strategy strategy = mapper.toDomain(dto);

        // Assert
        assertNotNull(strategy);
        assertNull(strategy.getObjective());
    }
}
