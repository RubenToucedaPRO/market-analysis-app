package com.market.analysis.unit.presentation.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.market.analysis.application.dto.RuleDTO;
import com.market.analysis.application.dto.RuleDefinitionDTO;
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

}
