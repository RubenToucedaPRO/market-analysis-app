package com.market.analysis.unit.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.domain.model.ObjectiveType;
import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.StrategyObjective;
import com.market.analysis.infrastructure.persistence.entity.RuleEntity;
import com.market.analysis.infrastructure.persistence.entity.StrategyEntity;
import com.market.analysis.infrastructure.persistence.entity.StrategyObjectiveEntity;
import com.market.analysis.infrastructure.persistence.mapper.RuleMapper;
import com.market.analysis.infrastructure.persistence.mapper.StrategyMapper;

/**
 * Unit tests for StrategyMapper.
 */
@DisplayName("StrategyMapper Unit Tests")
@ExtendWith(MockitoExtension.class)
class StrategyMapperTest {

    @Mock
    private RuleMapper ruleMapper;

    @InjectMocks
    private StrategyMapper strategyMapper;

    private Rule testRule;
    private RuleEntity testRuleEntity;

    @BeforeEach
    void setUp() {
        testRule = Rule.builder()
                .id(1L)
                .name("Test Rule")
                .subjectCode("PRICE")
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .build();

        testRuleEntity = new RuleEntity();
        testRuleEntity.setId(1L);
        testRuleEntity.setName("Test Rule");
        testRuleEntity.setSubjectCode("PRICE");
        testRuleEntity.setOperator(">");
        testRuleEntity.setTargetCode("CONSTANT");
        testRuleEntity.setTargetParam(100.0);
    }

    @Test
    @DisplayName("Should map Strategy domain to StrategyEntity")
    void testToEntity() {
        // Arrange
        when(ruleMapper.toEntity(any(Rule.class))).thenReturn(testRuleEntity);

        Strategy strategy = Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Test Description")
                .rules(List.of(testRule))
                .build();

        // Act
        StrategyEntity entity = strategyMapper.toEntity(strategy);

        // Assert
        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals("Test Strategy", entity.getName());
        assertEquals("Test Description", entity.getDescription());
        assertNotNull(entity.getRules());
        assertEquals(1, entity.getRules().size());
    }

    @Test
    @DisplayName("Should map StrategyEntity to Strategy domain")
    void testToDomain() {
        // Arrange
        when(ruleMapper.toDomain(any(RuleEntity.class))).thenReturn(testRule);

        StrategyEntity entity = new StrategyEntity();
        entity.setId(2L);
        entity.setName("RSI Strategy");
        entity.setDescription("RSI based strategy");
        entity.setRules(new ArrayList<>(List.of(testRuleEntity)));

        // Act
        Strategy strategy = strategyMapper.toDomain(entity);

        // Assert
        assertNotNull(strategy);
        assertEquals(2L, strategy.getId());
        assertEquals("RSI Strategy", strategy.getName());
        assertEquals("RSI based strategy", strategy.getDescription());
        assertNotNull(strategy.getRules());
        assertEquals(1, strategy.getRules().size());
    }

    @Test
    @DisplayName("Should handle null when converting domain to entity")
    void testToEntityWithNull() {
        // Act
        StrategyEntity entity = strategyMapper.toEntity(null);

        // Assert
        assertNull(entity);
    }

    @Test
    @DisplayName("Should handle null when converting entity to domain")
    void testToDomainWithNull() {
        // Act
        Strategy strategy = strategyMapper.toDomain(null);

        // Assert
        assertNull(strategy);
    }

    @Test
    @DisplayName("Should map strategy with multiple rules")
    void testMapStrategyWithMultipleRules() {
        // Arrange
        when(ruleMapper.toEntity(any(Rule.class))).thenReturn(testRuleEntity);

        Rule rule1 = Rule.builder()
                .id(1L)
                .name("Rule 1")
                .subjectCode("PRICE")
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .build();

        Rule rule2 = Rule.builder()
                .id(2L)
                .name("Rule 2")
                .subjectCode("RSI")
                .subjectParam(14.0)
                .operator("<")
                .targetCode("CONSTANT")
                .targetParam(30.0)
                .build();

        Strategy strategy = Strategy.builder()
                .id(1L)
                .name("Multi-Rule Strategy")
                .description("Strategy with multiple rules")
                .rules(List.of(rule1, rule2))
                .build();

        // Act
        StrategyEntity entity = strategyMapper.toEntity(strategy);

        // Assert
        assertNotNull(entity);
        assertEquals(2, entity.getRules().size());
    }

    @Test
    @DisplayName("Should map Strategy with StrategyObjective to StrategyEntity")
    void testToEntityWithObjective() {
        // Arrange
        when(ruleMapper.toEntity(any(Rule.class))).thenReturn(testRuleEntity);

        StrategyObjective objective = StrategyObjective.builder()
                .targetType(ObjectiveType.PERCENTAGE)
                .targetValue(BigDecimal.valueOf(5.0))
                .stopLossType(ObjectiveType.PERCENTAGE)
                .stopLossValue(BigDecimal.valueOf(2.0))
                .capitalToRisk(BigDecimal.valueOf(0.02))
                .description("Conservative objective")
                .build();

        Strategy strategy = Strategy.builder()
                .id(1L)
                .name("Strategy With Objective")
                .description("Description")
                .rules(List.of(testRule))
                .objective(objective)
                .build();

        // Act
        StrategyEntity entity = strategyMapper.toEntity(strategy);

        // Assert
        assertNotNull(entity);
        assertNotNull(entity.getObjective());
        assertEquals("PERCENTAGE", entity.getObjective().getTargetType());
        assertEquals(0, BigDecimal.valueOf(5.0).compareTo(entity.getObjective().getTargetValue()));
        assertEquals("PERCENTAGE", entity.getObjective().getStopLossType());
        assertEquals(0, BigDecimal.valueOf(2.0).compareTo(entity.getObjective().getStopLossValue()));
        assertEquals(0, BigDecimal.valueOf(0.02).compareTo(entity.getObjective().getCapitalToRisk()));
        assertEquals("Conservative objective", entity.getObjective().getDescription());
    }

    @Test
    @DisplayName("Should map StrategyEntity with StrategyObjectiveEntity to Strategy domain")
    void testToDomainWithObjective() {
        // Arrange
        when(ruleMapper.toDomain(any(RuleEntity.class))).thenReturn(testRule);

        StrategyObjectiveEntity objectiveEntity = new StrategyObjectiveEntity();
        objectiveEntity.setTargetType("FIXED_PRICE");
        objectiveEntity.setTargetValue(BigDecimal.valueOf(200.0));
        objectiveEntity.setStopLossType("SMA");
        objectiveEntity.setStopLossValue(BigDecimal.valueOf(50.0));
        objectiveEntity.setCapitalToRisk(BigDecimal.valueOf(0.01));
        objectiveEntity.setDescription("Fixed price objective");

        StrategyEntity entity = new StrategyEntity();
        entity.setId(3L);
        entity.setName("Strategy From Entity");
        entity.setDescription("Entity description");
        entity.setObjective(objectiveEntity);
        entity.setRules(new ArrayList<>(List.of(testRuleEntity)));

        // Act
        Strategy strategy = strategyMapper.toDomain(entity);

        // Assert
        assertNotNull(strategy);
        assertNotNull(strategy.getObjective());
        assertEquals(ObjectiveType.FIXED_PRICE, strategy.getObjective().getTargetType());
        assertEquals(0, BigDecimal.valueOf(200.0).compareTo(strategy.getObjective().getTargetValue()));
        assertEquals(ObjectiveType.SMA, strategy.getObjective().getStopLossType());
        assertEquals(0, BigDecimal.valueOf(50.0).compareTo(strategy.getObjective().getStopLossValue()));
        assertEquals(0, BigDecimal.valueOf(0.01).compareTo(strategy.getObjective().getCapitalToRisk()));
        assertEquals("Fixed price objective", strategy.getObjective().getDescription());
    }

    @Test
    @DisplayName("Should handle null objective in entity to domain conversion")
    void testToDomainWithNullObjective() {
        // Arrange
        when(ruleMapper.toDomain(any(RuleEntity.class))).thenReturn(testRule);

        StrategyEntity entity = new StrategyEntity();
        entity.setId(4L);
        entity.setName("No Objective Strategy");
        entity.setDescription("No objective");
        entity.setObjective(null);
        entity.setRules(new ArrayList<>(List.of(testRuleEntity)));

        // Act
        Strategy strategy = strategyMapper.toDomain(entity);

        // Assert
        assertNotNull(strategy);
        assertNull(strategy.getObjective());
    }

    @Test
    @DisplayName("Should handle null objective in domain to entity conversion")
    void testToEntityWithNullObjective() {
        // Arrange
        when(ruleMapper.toEntity(any(Rule.class))).thenReturn(testRuleEntity);

        Strategy strategy = Strategy.builder()
                .id(5L)
                .name("No Objective Strategy")
                .description("No objective")
                .rules(List.of(testRule))
                .objective(null)
                .build();

        // Act
        StrategyEntity entity = strategyMapper.toEntity(strategy);

        // Assert
        assertNotNull(entity);
        assertNull(entity.getObjective());
    }
}
