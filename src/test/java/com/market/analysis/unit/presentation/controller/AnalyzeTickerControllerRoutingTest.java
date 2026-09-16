package com.market.analysis.unit.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;
import com.market.analysis.domain.port.in.ManageStrategyUseCase;
import com.market.analysis.presentation.controller.AnalyzeTickerController;
import com.market.analysis.presentation.util.WebConstants;

/**
 * Routing tests for AnalyzeTickerController.
 * Non-numeric ticker ids must not reach the detail handlers: the request
 * falls through to the generic error page without invoking business logic
 * (no type-conversion crash inside a handler).
 */
@WebMvcTest(AnalyzeTickerController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AnalyzeTickerController Routing Tests")
class AnalyzeTickerControllerRoutingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManageAnalyzeTickerUseCase manageAnalyzeTickerUseCase;

    @MockitoBean
    private ManageStrategyUseCase manageStrategyUseCase;

    @Test
    @DisplayName("Should fall back to error page without business call for non-numeric ticker ids")
    void shouldFallBackToErrorPageForNonNumericTickerIds() throws Exception {
        mockMvc.perform(get("/analysis/ticker/abc"))
                .andExpect(status().isOk())
                .andExpect(view().name(WebConstants.TEMPLATE_ERROR))
                .andExpect(model().attribute(
                        WebConstants.ATTR_ERROR_DETAILS, Matchers.containsString("No static resource")));

        mockMvc.perform(get("/analysis/ticker/abc/chart"))
                .andExpect(status().isOk())
                .andExpect(view().name(WebConstants.TEMPLATE_ERROR));

        mockMvc.perform(get("/analysis/ticker/abc/candles"))
                .andExpect(status().isOk())
                .andExpect(view().name(WebConstants.TEMPLATE_ERROR));

        verify(manageAnalyzeTickerUseCase, never()).findStockDataById(any());
        verify(manageAnalyzeTickerUseCase, never()).findCandlesByStockId(any());
    }
}
