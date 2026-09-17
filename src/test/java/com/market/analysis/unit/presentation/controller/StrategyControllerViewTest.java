package com.market.analysis.unit.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.market.analysis.application.dto.RuleDTO;
import com.market.analysis.application.dto.RuleDefinitionDTO;
import com.market.analysis.application.dto.StrategyDTO;
import com.market.analysis.application.dto.StrategyObjectiveDTO;
import com.market.analysis.application.dto.UpdateStrategyResult;
import com.market.analysis.application.dto.SuggestTickersResponseDTO;
import com.market.analysis.application.dto.SuggestedTickerDTO;
import com.market.analysis.application.dto.TickerSuitabilityStatus;
import com.market.analysis.domain.port.in.ManageRuleDefinitionUseCase;
import com.market.analysis.domain.port.in.ManageStrategyUseCase;
import com.market.analysis.domain.port.in.SuggestTickersUseCase;
import com.market.analysis.presentation.controller.StrategyController;
import com.market.analysis.presentation.dto.UiNotification;
import com.market.analysis.presentation.util.WebConstants;

@WebMvcTest(StrategyController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("StrategyController View Tests")
class StrategyControllerViewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManageStrategyUseCase manageStrategyUseCase;

    @MockitoBean
    private ManageRuleDefinitionUseCase manageRuleDefinitionUseCase;

    @MockitoBean
    private SuggestTickersUseCase suggestTickersUseCase;

    @Test
    @DisplayName("Should render subject parameter select for empty strategy form")
    void shouldRenderSubjectParameterSelectInCreateForm() throws Exception {
        RuleDefinitionDTO smaDefinition = RuleDefinitionDTO.builder()
                .code("SMA")
                .name("Simple Moving Average")
                .requiresParam(true)
                .anyParamAllowed(false)
                .allowedParams(Set.of(20.0, 50.0, 200.0))
                .build();

        when(manageRuleDefinitionUseCase.getAllRuleDefinitions()).thenReturn(List.of(smaDefinition));

        mockMvc.perform(get("/strategies/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("strategies/create"))
                .andExpect(model().attributeExists("ruleDefinitions", "strategy"))
                .andExpect(content().string(containsString("subject-param-select-0")))
                .andExpect(content().string(containsString("subject-param-input-0")));

        verify(manageRuleDefinitionUseCase).getAllRuleDefinitions();
    }

    @Test
    @DisplayName("Should render SMA selectors with i18n hints in risk management fields")
    void shouldRenderSmaSelectorsWithI18nHintsInCreateForm() throws Exception {
        RuleDefinitionDTO smaDefinition = RuleDefinitionDTO.builder()
                .code("SMA")
                .name("Simple Moving Average")
                .requiresParam(true)
                .anyParamAllowed(false)
                .allowedParams(Set.of(20.0, 50.0, 200.0))
                .build();

        when(manageRuleDefinitionUseCase.getAllRuleDefinitions()).thenReturn(List.of(smaDefinition));

        mockMvc.perform(get("/strategies/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("strategies/create"))
                .andExpect(content().string(containsString("objectiveTargetSmaSelect")))
                .andExpect(content().string(containsString("objectiveStopLossSmaSelect")))
                .andExpect(content().string(containsString("objectiveTargetValueLabel")))
                .andExpect(content().string(containsString("objectiveStopLossValueLabel")))
                .andExpect(content().string(containsString("objectiveTargetSmaHelp")))
                .andExpect(content().string(containsString("data-label-sma")))
                .andExpect(content().string(containsString("Periodo SMA")));

        verify(manageRuleDefinitionUseCase).getAllRuleDefinitions();
    }

    @Test
    @DisplayName("Should render SMA target with horizon in strategy detail")
    void shouldRenderSmaTargetWithHorizonInDetail() throws Exception {
        StrategyDTO strategy = StrategyDTO.builder()
                .id(1L)
                .name("SMA Strategy")
                .description("Desc")
                .objective(StrategyObjectiveDTO.builder()
                        .targetType("SMA")
                        .targetValue(java.math.BigDecimal.valueOf(50))
                        .stopLossType("SMA")
                        .stopLossValue(java.math.BigDecimal.valueOf(20))
                        .capitalToRisk(java.math.BigDecimal.valueOf(1000))
                        .description("Risk desc")
                        .build())
                .rules(List.of())
                .build();
        when(manageStrategyUseCase.getStrategyById(1L)).thenReturn(strategy);
        when(suggestTickersUseCase.getLatestSuggestionSnapshot(1L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/strategies/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("strategies/detail"))
                .andExpect(content().string(containsString("SMA 50")))
                .andExpect(content().string(containsString("SMA 20")));

        verify(manageStrategyUseCase).getStrategyById(1L);
    }

    @Test
    @DisplayName("Should render saved subject and target parameters in edit form")
    void shouldRenderSavedParametersInEditForm() throws Exception {
        RuleDefinitionDTO priceDefinition = RuleDefinitionDTO.builder()
                .code("PRICE")
                .name("Price")
                .requiresParam(true)
                .anyParamAllowed(true)
                .allowedParams(Set.of())
                .build();

        RuleDefinitionDTO smaDefinition = RuleDefinitionDTO.builder()
                .code("SMA")
                .name("Simple Moving Average")
                .requiresParam(true)
                .anyParamAllowed(false)
                .allowedParams(Set.of(20.0, 50.0, 200.0))
                .build();

        RuleDTO rule = RuleDTO.builder()
                .id(1L)
                .name("Rule 1")
                .subjectCode("SMA")
                .subjectParam(50.0)
                .operator(">")
                .targetCode("PRICE")
                .targetParam(100.0)
                .build();

        StrategyDTO strategy = StrategyDTO.builder()
                .id(1L)
                .name("Editable Strategy")
                .description("Strategy description")
                .rules(new ArrayList<>(List.of(rule)))
                .build();

        when(manageStrategyUseCase.getStrategyById(1L)).thenReturn(strategy);
        when(manageRuleDefinitionUseCase.getAllRuleDefinitions()).thenReturn(List.of(priceDefinition, smaDefinition));

        mockMvc.perform(post("/strategies/edit").param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("strategies/create"))
                .andExpect(content().string(containsString("subject-param-input-0")))
                .andExpect(content().string(containsString("target-param-input-0")))
                .andExpect(content().string(containsString("value=\"50.0\"")))
                .andExpect(content().string(containsString("value=\"100.0\"")));

        verify(manageStrategyUseCase).getStrategyById(1L);
        verify(manageRuleDefinitionUseCase).getAllRuleDefinitions();
    }

    @Test
    @DisplayName("Should render suggest action in strategy detail")
    void shouldRenderSuggestActionInDetail() throws Exception {
        StrategyDTO strategy = StrategyDTO.builder()
                .id(1L)
                .name("Detail Strategy")
                .description("Desc")
                .rules(List.of())
                .build();
        when(manageStrategyUseCase.getStrategyById(1L)).thenReturn(strategy);
        when(suggestTickersUseCase.getLatestSuggestionSnapshot(1L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/strategies/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("strategies/detail"))
                .andExpect(content().string(containsString("Sugerir tickers desde mercado")))
                .andExpect(content().string(containsString("/strategies/1/suggest-tickers")));
    }

    @Test
    @DisplayName("Should add flash traceability attributes when suggesting tickers")
    void shouldAddTraceabilityFlashAttributesWhenSuggestingTickers() throws Exception {
        SuggestTickersResponseDTO response = SuggestTickersResponseDTO.builder()
                .suggestedTickers(List.of(
                        SuggestedTickerDTO.builder().ticker("AAPL").suitabilityStatus(TickerSuitabilityStatus.APTO).build(),
                        SuggestedTickerDTO.builder().ticker("TSLA").suitabilityStatus(TickerSuitabilityStatus.NO_APTO).build()))
                .unmappableRules(List.of("ATR(14)"))
                .build();
        when(suggestTickersUseCase.suggestTickers(any())).thenReturn(response);

        mockMvc.perform(post("/strategies/1/suggest-tickers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/strategies/1"))
                .andExpect(flash().attribute(WebConstants.UI_NOTIFICATION_KEY,
                        UiNotification.warning("Sugerencia parcial: revisa trazabilidad de descartes o reglas no mapeables.")));
    }

    @Test
    @DisplayName("Should render traceability block in strategy detail")
    void shouldRenderTraceabilityBlockInDetail() throws Exception {
        StrategyDTO strategy = StrategyDTO.builder()
                .id(1L)
                .name("Trace Strategy")
                .description("Desc")
                .rules(List.of())
                .build();
        SuggestTickersResponseDTO snapshot = SuggestTickersResponseDTO.builder()
                .strategyId(1L)
                .suggestedAt(Instant.parse("2026-04-18T12:00:00Z"))
                .suggestedTickers(List.of(
                        SuggestedTickerDTO.builder().ticker("AAPL").suitabilityStatus(TickerSuitabilityStatus.APTO).build(),
                        SuggestedTickerDTO.builder().ticker("TSLA").suitabilityStatus(TickerSuitabilityStatus.NO_APTO).build()))
                .unmappableRules(List.of("ATR(14)"))
                .build();
        when(manageStrategyUseCase.getStrategyById(1L)).thenReturn(strategy);
        when(suggestTickersUseCase.getLatestSuggestionSnapshot(1L)).thenReturn(java.util.Optional.of(snapshot));

        mockMvc.perform(get("/strategies/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Trazabilidad de sugerencias")))
                .andExpect(content().string(containsString("Última sugerencia")))
                .andExpect(content().string(containsString("2026-04-18T12:00:00Z")))
                .andExpect(content().string(containsString("suggested-tickers-traceability")))
                .andExpect(content().string(containsString("discarded-tickers-traceability")))
                .andExpect(content().string(containsString("unmappable-rules-traceability")))
                .andExpect(content().string(containsString("/strategies/1/add-suggested-tickers")))
                .andExpect(content().string(containsString("A\u00f1adir sugeridos a an\u00e1lisis")))
                .andExpect(content().string(containsString("AAPL")))
                .andExpect(content().string(containsString("TSLA")))
                .andExpect(content().string(containsString("ATR(14)")));
    }

    @Test
    @DisplayName("Should post add suggested tickers from snapshot and redirect to analysis")
    void shouldPostAddSuggestedTickersFromSnapshot() throws Exception {
        mockMvc.perform(post("/strategies/1/add-suggested-tickers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/analysis"))
                .andExpect(flash().attribute(WebConstants.UI_NOTIFICATION_KEY,
                        UiNotification.warning("No hay sugerencias aptas en snapshot para añadir.")));
    }

    @Test
    @DisplayName("Should not route POST-only actions to detail view on GET")
    void shouldNotRoutePostOnlyActionsToDetailView() throws Exception {
        mockMvc.perform(get("/strategies/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/strategies"));

        mockMvc.perform(get("/strategies/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/strategies"));
    }

    @Test
    @DisplayName("Should bind threshold and rule weight on strategy create")
    void shouldBindThresholdAndWeightOnCreate() throws Exception {
        mockMvc.perform(post("/strategies")
                        .param("name", "Scored Strategy")
                        .param("description", "With threshold and weights")
                        .param("threshold", "80")
                        .param("rules[0].name", "Heavy Rule")
                        .param("rules[0].subjectCode", "PRICE")
                        .param("rules[0].operator", ">")
                        .param("rules[0].targetCode", "CONSTANT")
                        .param("rules[0].targetParam", "100.0")
                        .param("rules[0].weight", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/strategies"));

        ArgumentCaptor<StrategyDTO> captor = ArgumentCaptor.forClass(StrategyDTO.class);
        verify(manageStrategyUseCase).createStrategy(captor.capture());
        assertThat(captor.getValue().getThreshold()).isEqualTo(80);
        assertThat(captor.getValue().getRules()).hasSize(1);
        assertThat(captor.getValue().getRules().get(0).getWeight()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should bind threshold and rule weight on strategy update")
    void shouldBindThresholdAndWeightOnUpdate() throws Exception {
        StrategyDTO saved = StrategyDTO.builder().id(1L).name("Scored Strategy").build();
        when(manageStrategyUseCase.updateStrategy(any(StrategyDTO.class)))
                .thenReturn(UpdateStrategyResult.builder()
                        .strategy(saved)
                        .degradedTickers(List.of())
                        .build());

        mockMvc.perform(post("/strategies")
                        .param("id", "1")
                        .param("name", "Scored Strategy")
                        .param("description", "With threshold and weights")
                        .param("threshold", "60")
                        .param("rules[0].name", "Light Rule")
                        .param("rules[0].subjectCode", "PRICE")
                        .param("rules[0].operator", ">")
                        .param("rules[0].targetCode", "CONSTANT")
                        .param("rules[0].targetParam", "100.0")
                        .param("rules[0].weight", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/strategies"));

        ArgumentCaptor<StrategyDTO> captor = ArgumentCaptor.forClass(StrategyDTO.class);
        verify(manageStrategyUseCase).updateStrategy(captor.capture());
        assertThat(captor.getValue().getThreshold()).isEqualTo(60);
        assertThat(captor.getValue().getRules()).hasSize(1);
        assertThat(captor.getValue().getRules().get(0).getWeight()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should render threshold and weight inputs in create form")
    void shouldRenderThresholdAndWeightInputsInCreateForm() throws Exception {
        when(manageRuleDefinitionUseCase.getAllRuleDefinitions()).thenReturn(List.of());

        mockMvc.perform(get("/strategies/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"threshold\"")))
                .andExpect(content().string(containsString("rules[0].weight")))
                .andExpect(content().string(containsString("Umbral de Aprobación")));
    }

    @Test
    @DisplayName("Should render threshold and rule weights in detail")
    void shouldRenderThresholdAndWeightsInDetail() throws Exception {
        RuleDTO rule = RuleDTO.builder()
                .id(1L)
                .name("Heavy Rule")
                .subjectCode("PRICE")
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .weight(3)
                .build();
        StrategyDTO strategy = StrategyDTO.builder()
                .id(1L)
                .name("Scored Strategy")
                .description("Desc")
                .threshold(75)
                .rules(List.of(rule))
                .build();
        when(manageStrategyUseCase.getStrategyById(1L)).thenReturn(strategy);
        when(suggestTickersUseCase.getLatestSuggestionSnapshot(1L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/strategies/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("75%")))
                .andExpect(content().string(containsString(">3<")));
    }

}
