package com.market.analysis.unit.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.application.usecase.ManageStrategyService;
import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.port.out.RuleDefinitionRepository;
import com.market.analysis.domain.port.out.StrategyRepository;

/**
 * Unit tests for ManageStrategyService.
 */
@DisplayName("ManageStrategyService Unit Tests")
@ExtendWith(MockitoExtension.class)
class ManageStrategyServiceTest {

    @Mock
    private StrategyRepository strategyRepository;

    @Mock
    private RuleDefinitionRepository ruleDefinitionRepository;

    @InjectMocks
    private ManageStrategyService manageStrategyService;

    private Strategy testStrategy;
    private Rule testRule;

    @BeforeEach
    void setUp() {
        testRule = Rule.builder()
                .id(1L)
                .name("Test Rule")
                .subjectCode("PRICE")
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .description("Price above 100")
                .build();

        testStrategy = Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Test Description")
                .rules(List.of(testRule))
                .build();
    }

    @Test
    @DisplayName("Should create strategy successfully")
    void testCreateStrategy() {
        // Arrange
        when(strategyRepository.save(any(Strategy.class))).thenReturn(testStrategy);

        // Act
        Strategy result = manageStrategyService.createStrategy(testStrategy);

        // Assert
        assertNotNull(result);
        assertEquals(testStrategy.getId(), result.getId());
        assertEquals(testStrategy.getName(), result.getName());
        verify(strategyRepository, times(1)).save(testStrategy);
    }

    @Test
    @DisplayName("Should get all strategies")
    void testGetAllStrategies() {
        // Arrange
        List<Strategy> strategies = List.of(testStrategy);
        when(strategyRepository.findAll()).thenReturn(strategies);

        // Act
        List<Strategy> result = manageStrategyService.getAllStrategies();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testStrategy.getId(), result.get(0).getId());
        verify(strategyRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get strategy by id")
    void testGetStrategyById() {
        // Arrange
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(testStrategy));

        // Act
        Strategy result = manageStrategyService.getStrategyById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Strategy", result.getName());
        verify(strategyRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when strategy not found")
    void testGetStrategyByIdNotFound() {
        // Arrange
        when(strategyRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> manageStrategyService.getStrategyById(999L));
        
        assertEquals("Strategy not found with id: 999", exception.getMessage());
        verify(strategyRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should get available rule definitions")
    void testGetAvailableRuleDefinitions() {
        // Arrange
        RuleDefinition ruleDefinition = RuleDefinition.builder()
                .id(1L)
                .code("SMA")
                .name("Simple Moving Average")
                .requiresParam(true)
                .description("Moving average")
                .build();
        when(ruleDefinitionRepository.findAll()).thenReturn(List.of(ruleDefinition));

        // Act
        List<RuleDefinition> result = manageStrategyService.getAvailableRuleDefinitions();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SMA", result.get(0).getCode());
        verify(ruleDefinitionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should delete strategy")
    void testDeleteStrategy() {
        // Act
        manageStrategyService.deleteStrategy(1L);

        // Assert
        verify(strategyRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should validate strategy before creating")
    void testCreateStrategyValidation() {
        // Arrange
        Strategy invalidStrategy = Strategy.builder()
                .id(2L)
                .name("Invalid Strategy")
                .description("No rules")
                .rules(List.of()) // Empty rules list - invalid
                .build();

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> manageStrategyService.createStrategy(invalidStrategy));
    }
}
