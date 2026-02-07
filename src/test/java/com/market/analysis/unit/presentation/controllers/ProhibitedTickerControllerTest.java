package com.market.analysis.unit.presentation.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.domain.port.in.ManageProhibitedTickerUseCase;
import com.market.analysis.presentation.dto.ProhibitedTickerDTO;
import com.market.analysis.presentation.mapper.ProhibitedTickerDTOMapper;

/**
 * Integration tests for ProhibitedTickerController using MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ProhibitedTickerController Integration Tests")
class ProhibitedTickerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManageProhibitedTickerUseCase manageProhibitedTickerUseCase;

    @MockitoBean
    private ProhibitedTickerDTOMapper mapper;

    private ProhibitedTicker testProhibitedTicker1;
    private ProhibitedTicker testProhibitedTicker2;
    private ProhibitedTickerDTO testDTO1;
    private ProhibitedTickerDTO testDTO2;

    @BeforeEach
    void setUp() {
        testProhibitedTicker1 = new ProhibitedTicker("AAPL");
        testProhibitedTicker2 = new ProhibitedTicker("GOOGL");

        testDTO1 = ProhibitedTickerDTO.builder()
                .id(1L)
                .ticker("AAPL")
                .reason("Test reason 1")
                .createdAt(LocalDateTime.now())
                .build();

        testDTO2 = ProhibitedTickerDTO.builder()
                .id(2L)
                .ticker("GOOGL")
                .reason("Test reason 2")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should list all prohibited tickers")
    void testListProhibitedTickers() throws Exception {
        // Arrange
        List<ProhibitedTicker> prohibitedTickers = Arrays.asList(testProhibitedTicker1, testProhibitedTicker2);
        when(manageProhibitedTickerUseCase.getAllProhibitedTickers()).thenReturn(prohibitedTickers);
        when(mapper.toDTO(testProhibitedTicker1)).thenReturn(testDTO1);
        when(mapper.toDTO(testProhibitedTicker2)).thenReturn(testDTO2);

        // Act & Assert
        mockMvc.perform(get("/prohibited-tickers"))
                .andExpect(status().isOk())
                .andExpect(view().name("prohibited-tickers/list"))
                .andExpect(model().attributeExists("prohibitedTickers"))
                .andExpect(model().attribute("prohibitedTickers", hasSize(2)));

        verify(manageProhibitedTickerUseCase, times(1)).getAllProhibitedTickers();
        verify(mapper, times(2)).toDTO(any(ProhibitedTicker.class));
    }

    @Test
    @DisplayName("Should return empty list when no prohibited tickers exist")
    void testListProhibitedTickersEmpty() throws Exception {
        // Arrange
        when(manageProhibitedTickerUseCase.getAllProhibitedTickers()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/prohibited-tickers"))
                .andExpect(status().isOk())
                .andExpect(view().name("prohibited-tickers/list"))
                .andExpect(model().attributeExists("prohibitedTickers"))
                .andExpect(model().attribute("prohibitedTickers", hasSize(0)));

        verify(manageProhibitedTickerUseCase, times(1)).getAllProhibitedTickers();
    }

    @Test
    @DisplayName("Should delete prohibited ticker and redirect to list")
    void testDeleteProhibitedTicker() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/prohibited-tickers/delete")
                .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prohibited-tickers"));

        verify(manageProhibitedTickerUseCase, times(1)).removeProhibitedTicker(1L);
    }

    @Test
    @DisplayName("Should handle delete with different ticker id")
    void testDeleteProhibitedTickerWithDifferentId() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/prohibited-tickers/delete")
                .param("id", "999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/prohibited-tickers"));

        verify(manageProhibitedTickerUseCase, times(1)).removeProhibitedTicker(999L);
    }
}
