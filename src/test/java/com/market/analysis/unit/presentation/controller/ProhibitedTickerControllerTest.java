package com.market.analysis.unit.presentation.controller;

import static org.mockito.ArgumentMatchers.anyInt;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.market.analysis.application.dto.ProhibitedKeywordDTO;
import com.market.analysis.application.dto.ProhibitedTickerDTO;
import com.market.analysis.application.mapper.ProhibitedTickerDTOMapper;
import com.market.analysis.domain.exception.DomainErrorCodes;
import com.market.analysis.domain.exception.DomainValidationException;
import com.market.analysis.domain.model.PageResult;
import com.market.analysis.domain.port.in.ManageProhibitedKeywordUseCase;
import com.market.analysis.domain.port.in.ManageProhibitedTickerUseCase;
import com.market.analysis.presentation.controller.ProhibitedTickerController;
import com.market.analysis.presentation.dto.UiNotification;
import com.market.analysis.presentation.util.WebConstants;

/**
 * Integration tests for ProhibitedTickerController using MockMvc.
 */
@WebMvcTest(ProhibitedTickerController.class)
@AutoConfigureMockMvc(addFilters = false)
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

    private ProhibitedTickerDTO testDTO1;
    private ProhibitedTickerDTO testDTO2;
    private ProhibitedKeywordDTO testKeywordDTO1;
    private ProhibitedKeywordDTO testKeywordDTO2;

    @BeforeEach
    void setUp() {
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
    @DisplayName("Should list all prohibited tickers with pagination")
    void testListProhibitedTickers() throws Exception {
        List<ProhibitedTickerDTO> tickers = Arrays.asList(testDTO1, testDTO2);
        List<ProhibitedKeywordDTO> keywords = Arrays.asList(testKeywordDTO1, testKeywordDTO2);
        PageResult<ProhibitedTickerDTO> tickerPage = new PageResult<>(tickers, 0, 10, 2, 1);
        PageResult<ProhibitedKeywordDTO> keywordPage = new PageResult<>(keywords, 0, 10, 2, 1);

        when(manageProhibitedTickerUseCase.getProhibitedTickers(anyInt(), anyInt())).thenReturn(tickerPage);
        when(manageProhibitedKeywordUseCase.getProhibitedKeywords(anyInt(), anyInt())).thenReturn(keywordPage);

        mockMvc.perform(get("/prohibited-tickers"))
                .andExpect(status().isOk())
                .andExpect(view().name("prohibited-tickers/list"))
                .andExpect(model().attributeExists("tickerPage"))
                .andExpect(model().attributeExists("keywordPage"));

        verify(manageProhibitedTickerUseCase, times(1)).getProhibitedTickers(0, WebConstants.DEFAULT_PAGE_SIZE);
        verify(manageProhibitedKeywordUseCase, times(1)).getProhibitedKeywords(0, WebConstants.DEFAULT_PAGE_SIZE);
    }

    @Test
    @DisplayName("Should return empty page when no prohibited tickers exist")
    void testListProhibitedTickersEmpty() throws Exception {
        PageResult<ProhibitedTickerDTO> emptyTickerPage = new PageResult<>(List.of(), 0, 10, 0, 0);
        PageResult<ProhibitedKeywordDTO> emptyKeywordPage = new PageResult<>(List.of(), 0, 10, 0, 0);

        when(manageProhibitedTickerUseCase.getProhibitedTickers(anyInt(), anyInt())).thenReturn(emptyTickerPage);
        when(manageProhibitedKeywordUseCase.getProhibitedKeywords(anyInt(), anyInt())).thenReturn(emptyKeywordPage);

        mockMvc.perform(get("/prohibited-tickers"))
                .andExpect(status().isOk())
                .andExpect(view().name("prohibited-tickers/list"))
                .andExpect(model().attributeExists("tickerPage"))
                .andExpect(model().attributeExists("keywordPage"));

        verify(manageProhibitedTickerUseCase, times(1)).getProhibitedTickers(0, WebConstants.DEFAULT_PAGE_SIZE);
        verify(manageProhibitedKeywordUseCase, times(1)).getProhibitedKeywords(0, WebConstants.DEFAULT_PAGE_SIZE);
    }

    @Test
    @DisplayName("Should navigate to specific page")
    void testListProhibitedTickersPageNavigation() throws Exception {
        List<ProhibitedTickerDTO> tickers = Arrays.asList(testDTO1);
        List<ProhibitedKeywordDTO> keywords = Arrays.asList(testKeywordDTO1);
        PageResult<ProhibitedTickerDTO> tickerPage = new PageResult<>(tickers, 2, 10, 25, 3);
        PageResult<ProhibitedKeywordDTO> keywordPage = new PageResult<>(keywords, 0, 10, 5, 1);

        when(manageProhibitedTickerUseCase.getProhibitedTickers(2, 10)).thenReturn(tickerPage);
        when(manageProhibitedKeywordUseCase.getProhibitedKeywords(0, 10)).thenReturn(keywordPage);

        mockMvc.perform(get("/prohibited-tickers").param("tickerPage", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("prohibited-tickers/list"))
                .andExpect(model().attributeExists("tickerPage"))
                .andExpect(model().attributeExists("keywordPage"));

        verify(manageProhibitedTickerUseCase, times(1)).getProhibitedTickers(2, 10);
        verify(manageProhibitedKeywordUseCase, times(1)).getProhibitedKeywords(0, 10);
    }

    @Test
    @DisplayName("Should delete prohibited ticker and redirect with page params")
    void testDeleteProhibitedTicker() throws Exception {
        mockMvc.perform(post("/prohibited-tickers/delete")
                .param("ticker", "AAPL")
                .param("tickerPage", "0")
                .param("keywordPage", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prohibited-tickers?tickerPage=0&keywordPage=1"))
                .andExpect(flash().attribute(WebConstants.UI_NOTIFICATION_KEY,
                        UiNotification.success("Ticker 'AAPL' desbloqueado y eliminado correctamente.")));

        verify(manageProhibitedTickerUseCase, times(1)).removeProhibitedTicker("AAPL");
    }

    @Test
    @DisplayName("Should handle delete with different ticker id")
    void testDeleteProhibitedTickerWithDifferentId() throws Exception {
        mockMvc.perform(post("/prohibited-tickers/delete")
                .param("ticker", "999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prohibited-tickers?tickerPage=0&keywordPage=0"))
                .andExpect(flash().attribute(WebConstants.UI_NOTIFICATION_KEY,
                        UiNotification.success("Ticker '999' desbloqueado y eliminado correctamente.")));

        verify(manageProhibitedTickerUseCase, times(1)).removeProhibitedTicker("999");
    }

    @Test
    @DisplayName("Should add prohibited keyword and redirect with page params")
    void testAddProhibitedKeyword() throws Exception {
        mockMvc.perform(post("/prohibited-tickers/keywords")
                .param("keyword", "ETF")
                .param("tickerPage", "0")
                .param("keywordPage", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prohibited-tickers?tickerPage=0&keywordPage=2"))
                .andExpect(flash().attribute(WebConstants.UI_NOTIFICATION_KEY,
                        UiNotification.success("Keyword 'ETF' añadida correctamente.")));

        verify(manageProhibitedKeywordUseCase, times(1)).addProhibitedKeyword(ProhibitedKeywordDTO.builder()
                .keyword("ETF")
                .build());
    }

    @Test
    @DisplayName("Should show error flash when adding prohibited keyword fails validation")
    void testAddProhibitedKeywordValidationError() throws Exception {
        doThrow(new DomainValidationException(DomainErrorCodes.KEYWORD_BLANK))
                .when(manageProhibitedKeywordUseCase)
                .addProhibitedKeyword(ProhibitedKeywordDTO.builder().keyword(" ").build());

        mockMvc.perform(post("/prohibited-tickers/keywords")
                .header("Referer", "/prohibited-tickers?tickerPage=0&keywordPage=0")
                .param("keyword", " "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prohibited-tickers?tickerPage=0&keywordPage=0"))
                .andExpect(flash().attribute(WebConstants.UI_NOTIFICATION_KEY,
                        UiNotification.error("Keyword cannot be null or blank")));

        verify(manageProhibitedKeywordUseCase, times(1))
                .addProhibitedKeyword(ProhibitedKeywordDTO.builder().keyword(" ").build());
    }

    @Test
    @DisplayName("Should delete prohibited keyword and redirect with page params")
    void testDeleteProhibitedKeyword() throws Exception {
        mockMvc.perform(post("/prohibited-tickers/keywords/delete")
                .param("keyword", "ETF")
                .param("tickerPage", "1")
                .param("keywordPage", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prohibited-tickers?tickerPage=1&keywordPage=3"))
                .andExpect(flash().attribute(WebConstants.UI_NOTIFICATION_KEY,
                        UiNotification.success("Keyword 'ETF' eliminada correctamente.")));

        verify(manageProhibitedKeywordUseCase, times(1)).removeProhibitedKeyword("ETF");
    }
}
