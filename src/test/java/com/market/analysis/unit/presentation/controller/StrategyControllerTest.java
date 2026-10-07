package com.market.analysis.unit.presentation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import com.market.analysis.application.dto.RuleDTO;
import com.market.analysis.application.dto.StrategyDTO;
import com.market.analysis.application.dto.SuggestJobStatusDTO;
import com.market.analysis.application.dto.UpdateStrategyResult;
import com.market.analysis.application.dto.SuggestTickersResponseDTO;
import com.market.analysis.application.dto.SuggestedTickerDTO;
import com.market.analysis.application.dto.TickerSuitabilityStatus;
import com.market.analysis.application.job.SuggestJobRejectedException;
import com.market.analysis.application.job.SuggestJobStatus;
import com.market.analysis.application.job.SuggestTickerJob;
import com.market.analysis.application.job.SuggestTickerJobService;
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
    private SuggestTickerJobService suggestTickerJobService;

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
                suggestTickerJobService,
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
        when(manageStrategyUseCase.updateStrategy(any(StrategyDTO.class)))
                .thenReturn(UpdateStrategyResult.builder()
                        .strategy(strategyDTO)
                        .degradedTickers(List.of())
                        .build());
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
    @DisplayName("Should warn with degraded tickers when risk plan is missing")
    void testSaveStrategyUpdateWarnsDegradedTickers() {
        StrategyDTO strategyDTO = StrategyDTO.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Test Description")
                .rules(List.of())
                .build();
        when(manageStrategyUseCase.updateStrategy(any(StrategyDTO.class)))
                .thenReturn(UpdateStrategyResult.builder()
                        .strategy(strategyDTO)
                        .degradedTickers(List.of("TSLA", "AAPL"))
                        .build());
        when(messageSource.getMessage(eq("strategy.updated.partial"), any(), eq(Locale.getDefault())))
                .thenReturn("Estrategia actualizada. Plan de riesgo no calculable en: TSLA, AAPL.");

        String viewName = strategyController.saveStrategy(strategyDTO, redirectAttributes);

        assertEquals("redirect:/strategies", viewName);
        verify(messageSource).getMessage(eq("strategy.updated.partial"), any(), eq(Locale.getDefault()));
        verify(redirectAttributes).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.warning("Estrategia actualizada. Plan de riesgo no calculable en: TSLA, AAPL."));
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
                .warnings(List.of("Las reglas 'A' y 'B' son incompatibles."))
                .build();
        when(suggestTickersUseCase.getLatestSuggestionSnapshot(1L)).thenReturn(Optional.of(snapshot));

        String viewName = strategyController.viewStrategyDetail(1L, null, model);

        assertEquals("strategies/detail", viewName);
        verify(manageStrategyUseCase).getStrategyById(1L);
        verify(model).addAttribute("strategy", testStrategyDTO);
        verify(model).addAttribute("suggestedTickers", List.of(
                SuggestedTickerDTO.builder().ticker("AAPL").suitabilityStatus(TickerSuitabilityStatus.APTO).build()));
        verify(model).addAttribute("discardedTickers", List.of(
                SuggestedTickerDTO.builder().ticker("TSLA").suitabilityStatus(TickerSuitabilityStatus.NO_APTO).build()));
        verify(model).addAttribute("unmappableRules", List.of("ATR(14)"));
        verify(model).addAttribute("snapshotWarnings", List.of("Las reglas 'A' y 'B' son incompatibles."));
        verify(model).addAttribute("suggestedAt", Instant.parse("2026-04-18T12:00:00Z"));
    }

    @Test
    @DisplayName("Should submit suggestion job and redirect with job id without blocking")
    void testSuggestTickersFromMarketSubmitsJob() {
        when(suggestTickerJobService.submitSuggestionJob(1L)).thenReturn("job-123");
        when(messageSource.getMessage("strategy.suggestion.job.started", null, Locale.getDefault()))
                .thenReturn("Sugerencia en curso.");

        String viewName = strategyController.suggestTickersFromMarket(1L, redirectAttributes);

        assertEquals("redirect:/strategies/1?jobId=job-123", viewName);
        verify(suggestTickerJobService).submitSuggestionJob(1L);
        verify(suggestTickersUseCase, never()).suggestTickers(any());
        verify(redirectAttributes).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Sugerencia en curso."));
    }

    @Test
    @DisplayName("Should warn when suggestion queue rejects the job")
    void testSuggestTickersFromMarketBusy() {
        when(suggestTickerJobService.submitSuggestionJob(1L))
                .thenThrow(new SuggestJobRejectedException("full", new RuntimeException("full")));
        when(messageSource.getMessage("strategy.suggestion.job.busy", null, Locale.getDefault()))
                .thenReturn("Cola llena.");

        String viewName = strategyController.suggestTickersFromMarket(1L, redirectAttributes);

        assertEquals("redirect:/strategies/1", viewName);
        verify(redirectAttributes).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.warning("Cola llena."));
    }

    @Test
    @DisplayName("Should error when suggestion use case is unavailable")
    void testSuggestTickersFromMarketUnavailable() {
        StrategyController controllerWithoutUseCase = new StrategyController(
                manageStrategyUseCase,
                manageRuleDefinitionUseCase,
                Optional.empty(),
                suggestTickerJobService,
                messageSource);
        when(messageSource.getMessage("strategy.suggestion.unavailable", null, Locale.getDefault()))
                .thenReturn("No disponible.");

        String viewName = controllerWithoutUseCase.suggestTickersFromMarket(1L, redirectAttributes);

        assertEquals("redirect:/strategies/1", viewName);
        verify(redirectAttributes).addFlashAttribute(
                WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.error("No disponible."));
    }

    @Test
    @DisplayName("Should return job status payload when job exists")
    void testGetSuggestJobStatusRunning() {
        SuggestTickerJob job = new SuggestTickerJob("job-123", 1L, Instant.now());
        job.markRunning();
        when(suggestTickerJobService.getJob("job-123")).thenReturn(Optional.of(job));

        ResponseEntity<SuggestJobStatusDTO> response = strategyController.getSuggestJobStatus("job-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("job-123", response.getBody().getJobId());
        assertEquals(SuggestJobStatus.RUNNING.name(), response.getBody().getStatus());
    }

    @Test
    @DisplayName("Should resolve localized message for failed job")
    void testGetSuggestJobStatusFailed() {
        SuggestTickerJob job = new SuggestTickerJob("job-123", 1L, Instant.now());
        job.markRunning();
        job.fail("boom");
        when(suggestTickerJobService.getJob("job-123")).thenReturn(Optional.of(job));
        when(messageSource.getMessage("strategy.suggestion.job.failed", null, Locale.getDefault()))
                .thenReturn("Falló.");

        ResponseEntity<SuggestJobStatusDTO> response = strategyController.getSuggestJobStatus("job-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Falló.", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should return 404 for unknown job")
    void testGetSuggestJobStatusNotFound() {
        when(suggestTickerJobService.getJob("unknown")).thenReturn(Optional.empty());

        ResponseEntity<SuggestJobStatusDTO> response = strategyController.getSuggestJobStatus("unknown");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should expose active job id from URL param in detail model")
    void testViewDetailExposesJobIdFromParam() {
        SuggestTickerJob job = new SuggestTickerJob("job-123", 1L, Instant.now());
        job.markRunning();
        when(manageStrategyUseCase.getStrategyById(1L)).thenReturn(testStrategyDTO);
        when(suggestTickerJobService.getJob("job-123")).thenReturn(Optional.of(job));

        String viewName = strategyController.viewStrategyDetail(1L, "job-123", model);

        assertEquals("strategies/detail", viewName);
        verify(model).addAttribute("suggestJobId", "job-123");
        verify(model).addAttribute(eq("suggestJobStartedAt"), any());
    }

    @Test
    @DisplayName("Should expose active job id without URL param when job is running")
    void testViewDetailExposesActiveJobWithoutParam() {
        SuggestTickerJob job = new SuggestTickerJob("job-abc", 1L, Instant.now());
        job.markRunning();
        when(manageStrategyUseCase.getStrategyById(1L)).thenReturn(testStrategyDTO);
        when(suggestTickerJobService.findActiveJobIdByStrategyId(1L)).thenReturn(Optional.of("job-abc"));
        when(suggestTickerJobService.getJob("job-abc")).thenReturn(Optional.of(job));

        String viewName = strategyController.viewStrategyDetail(1L, null, model);

        assertEquals("strategies/detail", viewName);
        verify(model).addAttribute("suggestJobId", "job-abc");
        verify(model).addAttribute(eq("suggestJobStartedAt"), any());
    }

    @Test
    @DisplayName("Should not expose job id when no job is active")
    void testViewDetailWithoutActiveJob() {
        when(manageStrategyUseCase.getStrategyById(1L)).thenReturn(testStrategyDTO);
        when(suggestTickerJobService.findActiveJobIdByStrategyId(1L)).thenReturn(Optional.empty());

        String viewName = strategyController.viewStrategyDetail(1L, null, model);

        assertEquals("strategies/detail", viewName);
        verify(model, never()).addAttribute(eq("suggestJobId"), any());
    }

    @Test
    @DisplayName("Should ignore stale job id param when job is no longer active")
    void testViewDetailIgnoresStaleJobParam() {
        SuggestTickerJob job = new SuggestTickerJob("job-old", 1L, Instant.now());
        job.markRunning();
        job.complete(1, 0);
        when(manageStrategyUseCase.getStrategyById(1L)).thenReturn(testStrategyDTO);
        when(suggestTickerJobService.getJob("job-old")).thenReturn(Optional.of(job));
        when(suggestTickerJobService.findActiveJobIdByStrategyId(1L)).thenReturn(Optional.empty());

        String viewName = strategyController.viewStrategyDetail(1L, "job-old", model);

        assertEquals("strategies/detail", viewName);
        verify(model, never()).addAttribute(eq("suggestJobId"), any());
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
