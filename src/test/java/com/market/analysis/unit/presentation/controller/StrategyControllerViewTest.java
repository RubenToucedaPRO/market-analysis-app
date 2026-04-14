package com.market.analysis.unit.presentation.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.market.analysis.application.dto.RuleDTO;
import com.market.analysis.application.dto.RuleDefinitionDTO;
import com.market.analysis.application.dto.StrategyDTO;
import com.market.analysis.domain.port.in.ManageRuleDefinitionUseCase;
import com.market.analysis.domain.port.in.ManageStrategyUseCase;
import com.market.analysis.presentation.controller.StrategyController;

@WebMvcTest(StrategyController.class)
@DisplayName("StrategyController View Tests")
class StrategyControllerViewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManageStrategyUseCase manageStrategyUseCase;

    @MockitoBean
    private ManageRuleDefinitionUseCase manageRuleDefinitionUseCase;

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
}