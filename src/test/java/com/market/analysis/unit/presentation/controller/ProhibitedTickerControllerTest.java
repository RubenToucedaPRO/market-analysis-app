package com.market.analysis.unit.presentation.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.market.analysis.application.dto.ProhibitedKeywordDTO;
import com.market.analysis.application.dto.ProhibitedTickerDTO;
import com.market.analysis.application.mapper.ProhibitedTickerDTOMapper;
import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.domain.port.in.ManageProhibitedKeywordUseCase;
import com.market.analysis.domain.port.in.ManageProhibitedTickerUseCase;
import com.market.analysis.presentation.controller.ProhibitedTickerController;
import com.market.analysis.presentation.dto.UiNotification;
import com.market.analysis.presentation.util.WebConstants;

/**
 * Integration tests for ProhibitedTickerController using MockMvc.
 */
@WebMvcTest(ProhibitedTickerController.class)
@DisplayName("ProhibitedTickerController Integration Tests")
class ProhibitedTickerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManageProhibitedTickerUseCase manageProhibitedTickerUseCase;

    @MockitoBean
    private ManageProhibitedKeywordUseCase manageProhibitedKeywordUseCase;

    @MockitoBean
    private ProhibitedTickerDTOMapper mapper;

    private ProhibitedTicker testProhibitedTicker1;
    private ProhibitedTicker testProhibitedTicker2;
    private ProhibitedTickerDTO testDTO1;
    private ProhibitedTickerDTO testDTO2;
    private ProhibitedKeywordDTO testKeywordDTO1;
    private ProhibitedKeywordDTO testKeywordDTO2;

    @BeforeEach
    void setUp() {
        testProhibitedTicker1 = new ProhibitedTicker("AAPL", "Test reason 1", Instant.now());
        testProhibitedTicker2 = new ProhibitedTicker("GOOGL", "Test reason 2", Instant.now());

        testDTO1 = ProhibitedTickerDTO.builder()
                .id(1L)
                .ticker("AAPL")
                .reason("Test reason 1")
                .createdAt(Instant.now())
                .build();

        testDTO2 = ProhibitedTickerDTO.builder()
                .id(2L)
                .ticker("GOOGL")
                .reason("Test reason 2")
                .createdAt(Instant.now())
                .build();

        testKeywordDTO1 = ProhibitedKeywordDTO.builder()
                .keyword("ETF")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        testKeywordDTO2 = ProhibitedKeywordDTO.builder()
                .keyword("BANK")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Should list all prohibited tickers")
    void testListProhibitedTickers() throws Exception {
        // Arrange
        List<ProhibitedTickerDTO> prohibitedTickers = Arrays.asList(testDTO1, testDTO2);
        List<ProhibitedKeywordDTO> prohibitedKeywords = Arrays.asList(testKeywordDTO1, testKeywordDTO2);
        when(manageProhibitedTickerUseCase.getAllProhibitedTickers()).thenReturn(prohibitedTickers);
        when(manageProhibitedKeywordUseCase.getAllProhibitedKeywords()).thenReturn(prohibitedKeywords);
        when(mapper.toDTO(testProhibitedTicker1)).thenReturn(testDTO1);
        when(mapper.toDTO(testProhibitedTicker2)).thenReturn(testDTO2);

        // Act & Assert
        mockMvc.perform(get("/prohibited-tickers"))
                .andExpect(status().isOk())
                .andExpect(view().name("prohibited-tickers/list"))
                .andExpect(model().attributeExists("prohibitedTickers"))
                .andExpect(model().attribute("prohibitedTickers", hasSize(2)))
                .andExpect(model().attributeExists("prohibitedKeywords"))
                .andExpect(model().attribute("prohibitedKeywords", hasSize(2)));

        verify(manageProhibitedTickerUseCase, times(1)).getAllProhibitedTickers();
        verify(manageProhibitedKeywordUseCase, times(1)).getAllProhibitedKeywords();
    }

    @Test
    @DisplayName("Should return empty list when no prohibited tickers exist")
    void testListProhibitedTickersEmpty() throws Exception {
        // Arrange
        when(manageProhibitedTickerUseCase.getAllProhibitedTickers()).thenReturn(Arrays.asList());
        when(manageProhibitedKeywordUseCase.getAllProhibitedKeywords()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/prohibited-tickers"))
                .andExpect(status().isOk())
                .andExpect(view().name("prohibited-tickers/list"))
                .andExpect(model().attributeExists("prohibitedTickers"))
                .andExpect(model().attribute("prohibitedTickers", hasSize(0)))
                .andExpect(model().attributeExists("prohibitedKeywords"))
                .andExpect(model().attribute("prohibitedKeywords", hasSize(0)));

        verify(manageProhibitedTickerUseCase, times(1)).getAllProhibitedTickers();
        verify(manageProhibitedKeywordUseCase, times(1)).getAllProhibitedKeywords();
    }

    @Test
    @DisplayName("Should delete prohibited ticker and redirect to list with success flash")
    void testDeleteProhibitedTicker() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/prohibited-tickers/delete")
                .param("ticker", "AAPL"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prohibited-tickers"))
                .andExpect(flash().attribute(WebConstants.UI_NOTIFICATION_KEY,
                        UiNotification.success("Ticker 'AAPL' desbloqueado y eliminado correctamente.")));

        verify(manageProhibitedTickerUseCase, times(1)).removeProhibitedTicker("AAPL");
    }

    @Test
    @DisplayName("Should handle delete with different ticker id")
    void testDeleteProhibitedTickerWithDifferentId() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/prohibited-tickers/delete")
                .param("ticker", "999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prohibited-tickers"))
                .andExpect(flash().attribute(WebConstants.UI_NOTIFICATION_KEY,
                        UiNotification.success("Ticker '999' desbloqueado y eliminado correctamente.")));

        verify(manageProhibitedTickerUseCase, times(1)).removeProhibitedTicker("999");
    }

    @Test
    @DisplayName("Should add prohibited keyword and redirect with success flash")
    void testAddProhibitedKeyword() throws Exception {
        mockMvc.perform(post("/prohibited-tickers/keywords")
                .param("keyword", "ETF"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prohibited-tickers"))
                .andExpect(flash().attribute(WebConstants.UI_NOTIFICATION_KEY,
                        UiNotification.success("Keyword 'ETF' añadida correctamente.")));

        verify(manageProhibitedKeywordUseCase, times(1)).addProhibitedKeyword(ProhibitedKeywordDTO.builder()
                .keyword("ETF")
                .build());
    }

    @Test
    @DisplayName("Should show error flash when adding prohibited keyword fails validation")
    void testAddProhibitedKeywordValidationError() throws Exception {
        doThrow(new IllegalArgumentException("Keyword cannot be null or blank"))
                .when(manageProhibitedKeywordUseCase)
                .addProhibitedKeyword(ProhibitedKeywordDTO.builder().keyword("").build());

        mockMvc.perform(post("/prohibited-tickers/keywords")
                .param("keyword", " "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prohibited-tickers"))
                .andExpect(flash().attribute(WebConstants.UI_NOTIFICATION_KEY,
                        UiNotification.error("Keyword cannot be null or blank")));

        verify(manageProhibitedKeywordUseCase, times(1))
                .addProhibitedKeyword(ProhibitedKeywordDTO.builder().keyword("").build());
    }

    @Test
    @DisplayName("Should delete prohibited keyword and redirect with success flash")
    void testDeleteProhibitedKeyword() throws Exception {
        mockMvc.perform(post("/prohibited-tickers/keywords/delete")
                .param("keyword", "ETF"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prohibited-tickers"))
                .andExpect(flash().attribute(WebConstants.UI_NOTIFICATION_KEY,
                        UiNotification.success("Keyword 'ETF' eliminada correctamente.")));

        verify(manageProhibitedKeywordUseCase, times(1)).removeProhibitedKeyword("ETF");
    }
}
