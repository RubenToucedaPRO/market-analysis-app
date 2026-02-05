package com.market.analysis.unit.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.infrastructure.persistence.entity.RuleEntity;
import com.market.analysis.infrastructure.persistence.entity.StrategyEntity;
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
                .description("Test")
                .build();

        testRuleEntity = new RuleEntity();
        testRuleEntity.setId(1L);
        testRuleEntity.setName("Test Rule");
        testRuleEntity.setSubjectCode("PRICE");
        testRuleEntity.setOperator(">");
        testRuleEntity.setTargetCode("CONSTANT");
        testRuleEntity.setTargetParam(100.0);
        testRuleEntity.setDescription("Test");
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
}
