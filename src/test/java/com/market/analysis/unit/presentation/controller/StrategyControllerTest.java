package com.market.analysis.unit.presentation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.port.in.ManageRuleDefinitionUseCase;
import com.market.analysis.domain.port.in.ManageStrategyUseCase;
import com.market.analysis.presentation.controller.StrategyController;
import com.market.analysis.presentation.dto.StrategyDTO;
import com.market.analysis.presentation.mapper.RuleDefinitionDTOMapper;
import com.market.analysis.presentation.mapper.StrategyDTOMapper;

/**
 * Unit tests for StrategyController.
 */
@DisplayName("StrategyController Unit Tests")
@ExtendWith(MockitoExtension.class)
class StrategyControllerTest {

    @Mock
    private ManageStrategyUseCase manageStrategyUseCase;

    @Mock
    private ManageRuleDefinitionUseCase manageRuleDefinitionUseCase;

    @Mock
    private RuleDefinitionDTOMapper ruleDefinitionDTOMapper;

    @Mock
    private StrategyDTOMapper strategyDTOMapper;

    @Mock
    private Model model;

    @InjectMocks
    private StrategyController strategyController;

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
                .description("Test")
                .build();

        testStrategy = Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Test Description")
                .rules(List.of(testRule))
                .build();
    }

    @Test
    @DisplayName("Should list all strategies")
    void testListStrategies() {
        // Arrange
        List<Strategy> strategies = List.of(testStrategy);
        when(manageStrategyUseCase.getAllStrategies()).thenReturn(strategies);

        // Act
        String viewName = strategyController.listStrategies(model);

        // Assert
        assertEquals("strategies/list", viewName);
        verify(manageStrategyUseCase, times(1)).getAllStrategies();
        verify(model, times(1)).addAttribute("strategies", strategies);
    }

    @Test
    @DisplayName("Should show create form with empty strategy")
    void testShowCreateForm() {
        // Act
        String viewName = strategyController.showCreateForm(model);

        // Assert
        assertEquals("strategies/create", viewName);
        verify(manageRuleDefinitionUseCase, times(1)).getAllRuleDefinitions();
        verify(model, times(2)).addAttribute(any(String.class), any());
    }

    @Test
    @DisplayName("Should show edit form with existing strategy")
    void testShowEditForm() {
        // Arrange
        when(manageStrategyUseCase.getStrategyById(1L)).thenReturn(testStrategy);
        when(strategyDTOMapper.toDTO(testStrategy)).thenReturn(
                StrategyDTO.builder()
                        .id(1L)
                        .name("Test Strategy")
                        .description("Test Description")
                        .rules(List.of())
                        .build());

        // Act
        String viewName = strategyController.showEditForm(1L, model);

        // Assert
        assertEquals("strategies/create", viewName);
        verify(manageStrategyUseCase, times(1)).getStrategyById(1L);
        verify(manageRuleDefinitionUseCase, times(1)).getAllRuleDefinitions();
    }

    @Test
    @DisplayName("Should save strategy and redirect")
    void testSaveStrategy() {
        // Arrange
        StrategyDTO strategyDTO = StrategyDTO.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Test Description")
                .rules(List.of())
                .build();

        when(strategyDTOMapper.toDomain(any(StrategyDTO.class))).thenReturn(testStrategy);

        // Act
        String viewName = strategyController.saveStrategy(strategyDTO);

        // Assert
        assertEquals("redirect:/strategies", viewName);
        verify(manageStrategyUseCase, times(1)).createStrategy(any(Strategy.class));
    }

    @Test
    @DisplayName("Should delete strategy and redirect")
    void testDeleteStrategy() {
        // Act
        String viewName = strategyController.deleteStrategy(1L);

        // Assert
        assertEquals("redirect:/strategies", viewName);
        verify(manageStrategyUseCase, times(1)).deleteStrategy(1L);
    }

    @Test
    @DisplayName("Should handle list with empty strategies")
    void testListStrategiesEmpty() {
        // Arrange
        when(manageStrategyUseCase.getAllStrategies()).thenReturn(List.of());

        // Act
        String viewName = strategyController.listStrategies(model);

        // Assert
        assertEquals("strategies/list", viewName);
        verify(model, times(1)).addAttribute("strategies", List.of());
    }

    @Test
    @DisplayName("Should handle multiple strategies in list")
    void testListMultipleStrategies() {
        // Arrange
        Strategy strategy2 = Strategy.builder()
                .id(2L)
                .name("Strategy 2")
                .description("Description 2")
                .rules(List.of(testRule))
                .build();

        List<Strategy> strategies = List.of(testStrategy, strategy2);
        when(manageStrategyUseCase.getAllStrategies()).thenReturn(strategies);

        // Act
        String viewName = strategyController.listStrategies(model);

        // Assert
        assertEquals("strategies/list", viewName);
        verify(manageStrategyUseCase, times(1)).getAllStrategies();
        verify(model, times(1)).addAttribute("strategies", strategies);
    }
}
