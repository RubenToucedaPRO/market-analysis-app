package com.market.analysis.unit.presentation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import com.market.analysis.application.dto.RuleDTO;
import com.market.analysis.application.dto.SuggestTickersResponseDTO;
import com.market.analysis.application.dto.SuggestedTickerDTO;
import com.market.analysis.application.dto.StrategyDTO;
import com.market.analysis.application.dto.TickerSuitabilityStatus;
import com.market.analysis.application.mapper.RuleDefinitionDTOMapper;
import com.market.analysis.application.mapper.StrategyDTOMapper;
import com.market.analysis.domain.port.in.ManageRuleDefinitionUseCase;
import com.market.analysis.domain.port.in.ManageStrategyUseCase;
import com.market.analysis.domain.port.in.SuggestTickersUseCase;
import com.market.analysis.presentation.controller.StrategyController;
import com.market.analysis.presentation.dto.UiNotification;
import com.market.analysis.presentation.util.WebConstants;

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
    private SuggestTickersUseCase suggestTickersUseCase;

    @Mock
    private RuleDefinitionDTOMapper ruleDefinitionDTOMapper;

    @Mock
    private StrategyDTOMapper strategyDTOMapper;

    @Mock
    private Model model;

    @Mock
    private org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes;

    private StrategyController strategyController;

    private StrategyDTO testStrategyDTO;
    private RuleDTO testRuleDTO;

    @BeforeEach
    void setUp() {
        strategyController = new StrategyController(
                manageStrategyUseCase,
                manageRuleDefinitionUseCase,
                Optional.of(suggestTickersUseCase));

        testRuleDTO = RuleDTO.builder()
                .id(1L)
                .name("Test Rule")
                .subjectCode("PRICE")
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .description("Test")
                .build();

        testStrategyDTO = StrategyDTO.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Test Description")
                .rules(List.of(testRuleDTO))
                .build();
    }

    @Test
    @DisplayName("Should list all strategies")
    void testListStrategies() {
        // Arrange
        List<StrategyDTO> strategies = List.of(testStrategyDTO);
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
        verify(model, times(3)).addAttribute(any(String.class), any());
    }

    @Test
    @DisplayName("Should show edit form with existing strategy")
    void testShowEditForm() {
        // Arrange
        when(manageStrategyUseCase.getStrategyById(1L)).thenReturn(testStrategyDTO);

        // Act
        String viewName = strategyController.showEditForm(1L, model);

        // Assert
        assertEquals("strategies/create", viewName);
        verify(manageStrategyUseCase, times(1)).getStrategyById(1L);
        verify(manageRuleDefinitionUseCase, times(1)).getAllRuleDefinitions();
    }

    @Test
    @DisplayName("Should save strategy and redirect with success flash")
    void testSaveStrategy() {
        // Arrange
        StrategyDTO strategyDTO = StrategyDTO.builder()
                .name("Test Strategy")
                .description("Test Description")
                .rules(List.of())
                .build();

        // Act
        String viewName = strategyController.saveStrategy(strategyDTO, redirectAttributes);

        // Assert
        assertEquals("redirect:/strategies", viewName);
        verify(manageStrategyUseCase, times(1)).createStrategy(any(StrategyDTO.class));
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Estrategia creada correctamente."));
    }

    @Test
    @DisplayName("Should save existing strategy and redirect with update flash")
    void testSaveStrategyUpdate() {
        // Arrange – id != null triggers update message
        StrategyDTO strategyDTO = StrategyDTO.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Test Description")
                .rules(List.of())
                .build();

        // Act
        String viewName = strategyController.saveStrategy(strategyDTO, redirectAttributes);

        // Assert
        assertEquals("redirect:/strategies", viewName);
        verify(manageStrategyUseCase, times(1)).updateStrategy(any(StrategyDTO.class));
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Estrategia actualizada correctamente."));
    }

    @Test
    @DisplayName("Should delete strategy and redirect with success flash")
    void testDeleteStrategy() {
        // Act
        String viewName = strategyController.deleteStrategy(1L, redirectAttributes);

        // Assert
        assertEquals("redirect:/strategies", viewName);
        verify(manageStrategyUseCase, times(1)).deleteStrategy(1L);
        verify(redirectAttributes, times(1)).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Estrategia eliminada correctamente."));
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
        StrategyDTO strategy2 = StrategyDTO.builder()
                .id(2L)
                .name("Strategy 2")
                .description("Description 2")
                .rules(List.of(testRuleDTO))
                .build();

        List<StrategyDTO> strategies = List.of(testStrategyDTO, strategy2);
        when(manageStrategyUseCase.getAllStrategies()).thenReturn(strategies);

        // Act
        String viewName = strategyController.listStrategies(model);

        // Assert
        assertEquals("strategies/list", viewName);
        verify(manageStrategyUseCase, times(1)).getAllStrategies();
        verify(model, times(1)).addAttribute("strategies", strategies);
    }

    @Test
    @DisplayName("Should view strategy detail by id")
    void testViewStrategyDetail() {
        // Arrange
        when(manageStrategyUseCase.getStrategyById(1L)).thenReturn(testStrategyDTO);

        // Act
        String viewName = strategyController.viewStrategyDetail(1L, model);

        // Assert
        assertEquals("strategies/detail", viewName);
        verify(manageStrategyUseCase, times(1)).getStrategyById(1L);
        verify(model, times(1)).addAttribute("strategy", testStrategyDTO);
    }

    @Test
    @DisplayName("Should suggest tickers with success notification")
    void testSuggestTickersFromMarketSuccess() {
        SuggestTickersResponseDTO response = SuggestTickersResponseDTO.builder()
                .suggestedTickers(List.of(SuggestedTickerDTO.builder()
                        .ticker("AAPL")
                        .suitabilityStatus(TickerSuitabilityStatus.APTO)
                        .build()))
                .unmappableRules(List.of())
                .build();
        when(suggestTickersUseCase.suggestTickers(any())).thenReturn(response);

        String viewName = strategyController.suggestTickersFromMarket(1L, redirectAttributes);

        assertEquals("redirect:/strategies/1", viewName);
        verify(suggestTickersUseCase).suggestTickers(any());
        verify(redirectAttributes).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Sugerencias generadas correctamente desde mercado."));
    }

    @Test
    @DisplayName("Should suggest tickers with partial notification when there are discards")
    void testSuggestTickersFromMarketPartial() {
        SuggestTickersResponseDTO response = SuggestTickersResponseDTO.builder()
                .suggestedTickers(List.of(SuggestedTickerDTO.builder()
                        .ticker("TSLA")
                        .suitabilityStatus(TickerSuitabilityStatus.NO_APTO)
                        .build()))
                .unmappableRules(List.of())
                .build();
        when(suggestTickersUseCase.suggestTickers(any())).thenReturn(response);

        String viewName = strategyController.suggestTickersFromMarket(1L, redirectAttributes);

        assertEquals("redirect:/strategies/1", viewName);
        verify(redirectAttributes).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.warning("Sugerencia parcial: revisa trazabilidad de descartes o reglas no mapeables."));
    }

    @Test
    @DisplayName("Should return error notification when suggest use case fails")
    void testSuggestTickersFromMarketError() {
        when(suggestTickersUseCase.suggestTickers(any())).thenThrow(new RuntimeException("boom"));

        String viewName = strategyController.suggestTickersFromMarket(1L, redirectAttributes);

        assertEquals("redirect:/strategies/1", viewName);
        verify(redirectAttributes).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.error("No se pudo sugerir tickers desde mercado en este momento."));
    }

    @Test
    @DisplayName("Should return error notification when suggest use case is unavailable")
    void testSuggestTickersFromMarketUnavailable() {
        strategyController = new StrategyController(
                manageStrategyUseCase,
                manageRuleDefinitionUseCase,
                Optional.empty());

        String viewName = strategyController.suggestTickersFromMarket(1L, redirectAttributes);

        assertEquals("redirect:/strategies/1", viewName);
        verify(suggestTickersUseCase, never()).suggestTickers(any());
        verify(redirectAttributes).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.error("La sugerencia de tickers desde mercado no está disponible todavía."));
    }
}
