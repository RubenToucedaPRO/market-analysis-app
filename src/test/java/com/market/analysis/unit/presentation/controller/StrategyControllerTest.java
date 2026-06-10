package com.market.analysis.unit.presentation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;

import com.market.analysis.application.dto.RuleDTO;
import com.market.analysis.application.dto.StrategyDTO;
import com.market.analysis.application.dto.SuggestTickersResponseDTO;
import com.market.analysis.application.dto.SuggestedTickerDTO;
import com.market.analysis.application.dto.TickerSuitabilityStatus;
import com.market.analysis.domain.port.in.ManageRuleDefinitionUseCase;
import com.market.analysis.domain.port.in.ManageStrategyUseCase;
import com.market.analysis.domain.port.in.SuggestTickersUseCase;
import com.market.analysis.presentation.controller.StrategyController;
import com.market.analysis.presentation.dto.UiNotification;
import com.market.analysis.presentation.util.WebConstants;

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
    private MessageSource messageSource;

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
                Optional.of(suggestTickersUseCase),
                messageSource);

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
        List<StrategyDTO> strategies = List.of(testStrategyDTO);
        when(manageStrategyUseCase.getAllStrategies()).thenReturn(strategies);

        String viewName = strategyController.listStrategies(model);

        assertEquals("strategies/list", viewName);
        verify(manageStrategyUseCase).getAllStrategies();
        verify(model).addAttribute("strategies", strategies);
    }

    @Test
    @DisplayName("Should show create form with empty strategy")
    void testShowCreateForm() {
        String viewName = strategyController.showCreateForm(model);

        assertEquals("strategies/create", viewName);
        verify(manageRuleDefinitionUseCase).getAllRuleDefinitions();
                verify(model, org.mockito.Mockito.times(3)).addAttribute(any(String.class), any());
    }

    @Test
    @DisplayName("Should show edit form with existing strategy")
    void testShowEditForm() {
        when(manageStrategyUseCase.getStrategyById(1L)).thenReturn(testStrategyDTO);

        String viewName = strategyController.showEditForm(1L, model);

        assertEquals("strategies/create", viewName);
        verify(manageStrategyUseCase).getStrategyById(1L);
        verify(manageRuleDefinitionUseCase).getAllRuleDefinitions();
    }

    @Test
    @DisplayName("Should save strategy and redirect with success flash")
    void testSaveStrategy() {
        StrategyDTO strategyDTO = StrategyDTO.builder()
                .name("Test Strategy")
                .description("Test Description")
                .rules(List.of())
                .build();
        when(messageSource.getMessage("strategy.created", null, Locale.getDefault()))
                .thenReturn("Estrategia creada correctamente.");

        String viewName = strategyController.saveStrategy(strategyDTO, redirectAttributes);

        assertEquals("redirect:/strategies", viewName);
        verify(manageStrategyUseCase).createStrategy(any(StrategyDTO.class));
        verify(redirectAttributes).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Estrategia creada correctamente."));
    }

    @Test
    @DisplayName("Should save existing strategy and redirect with update flash")
    void testSaveStrategyUpdate() {
        StrategyDTO strategyDTO = StrategyDTO.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Test Description")
                .rules(List.of())
                .build();
        when(messageSource.getMessage("strategy.updated", null, Locale.getDefault()))
                .thenReturn("Estrategia actualizada correctamente.");

        String viewName = strategyController.saveStrategy(strategyDTO, redirectAttributes);

        assertEquals("redirect:/strategies", viewName);
        verify(manageStrategyUseCase).updateStrategy(any(StrategyDTO.class));
        verify(redirectAttributes).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Estrategia actualizada correctamente."));
    }

    @Test
    @DisplayName("Should delete strategy and redirect with success flash")
    void testDeleteStrategy() {
        when(messageSource.getMessage("strategy.deleted", null, Locale.getDefault()))
                .thenReturn("Estrategia eliminada correctamente.");

        String viewName = strategyController.deleteStrategy(1L, redirectAttributes);

        assertEquals("redirect:/strategies", viewName);
        verify(manageStrategyUseCase).deleteStrategy(1L);
        verify(redirectAttributes).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Estrategia eliminada correctamente."));
    }

    @Test
    @DisplayName("Should view strategy detail by id")
    void testViewStrategyDetail() {
        when(manageStrategyUseCase.getStrategyById(1L)).thenReturn(testStrategyDTO);
        SuggestTickersResponseDTO snapshot = SuggestTickersResponseDTO.builder()
                .strategyId(1L)
                .suggestedAt(Instant.parse("2026-04-18T12:00:00Z"))
                .suggestedTickers(List.of(
                        SuggestedTickerDTO.builder().ticker("AAPL").suitabilityStatus(TickerSuitabilityStatus.APTO).build(),
                        SuggestedTickerDTO.builder().ticker("TSLA").suitabilityStatus(TickerSuitabilityStatus.NO_APTO).build()))
                .unmappableRules(List.of("ATR(14)"))
                .build();
        when(suggestTickersUseCase.getLatestSuggestionSnapshot(1L)).thenReturn(Optional.of(snapshot));

        String viewName = strategyController.viewStrategyDetail(1L, model);

        assertEquals("strategies/detail", viewName);
        verify(manageStrategyUseCase).getStrategyById(1L);
        verify(model).addAttribute("strategy", testStrategyDTO);
        verify(model).addAttribute("suggestedTickers", List.of(
                SuggestedTickerDTO.builder().ticker("AAPL").suitabilityStatus(TickerSuitabilityStatus.APTO).build()));
        verify(model).addAttribute("discardedTickers", List.of(
                SuggestedTickerDTO.builder().ticker("TSLA").suitabilityStatus(TickerSuitabilityStatus.NO_APTO).build()));
        verify(model).addAttribute("unmappableRules", List.of("ATR(14)"));
        verify(model).addAttribute("suggestedAt", Instant.parse("2026-04-18T12:00:00Z"));
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
        when(messageSource.getMessage("strategy.suggestion.success", null, Locale.getDefault()))
                .thenReturn("Sugerencias generadas correctamente desde mercado.");

        String viewName = strategyController.suggestTickersFromMarket(1L, redirectAttributes);

        assertEquals("redirect:/strategies/1", viewName);
        verify(suggestTickersUseCase).suggestTickers(any());
        verify(redirectAttributes).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Sugerencias generadas correctamente desde mercado."));
    }

    @Test
    @DisplayName("Should switch suggested tickers to analysis origin and redirect")
    void testAddSuggestedTickersToAnalysisSuccess() {
        when(suggestTickersUseCase.convertSuggestedTickersToAnalysis(1L)).thenReturn(2);
        when(messageSource.getMessage("strategy.tickers.switched", new Object[] { 2 }, Locale.getDefault()))
                .thenReturn("Ticker(s) cambiados a origen análisis: 2.");

        String viewName = strategyController.addSuggestedTickersToAnalysis(1L, redirectAttributes);

        assertEquals("redirect:/analysis", viewName);
        verify(suggestTickersUseCase).convertSuggestedTickersToAnalysis(1L);
        verify(redirectAttributes).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Ticker(s) cambiados a origen análisis: 2."));
    }


    @Test
    @DisplayName("Should warn when there are no eligible tickers to switch")
    void testAddSuggestedTickersToAnalysisNoSnapshotData() {
        when(suggestTickersUseCase.convertSuggestedTickersToAnalysis(1L)).thenReturn(0);
        when(messageSource.getMessage("strategy.suggestion.none_added", null, Locale.getDefault()))
                .thenReturn("No hay sugerencias aptas en snapshot para añadir.");

        String viewName = strategyController.addSuggestedTickersToAnalysis(1L, redirectAttributes);

        assertEquals("redirect:/analysis", viewName);
        verify(redirectAttributes).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.warning("No hay sugerencias aptas en snapshot para añadir."));
    }
}
