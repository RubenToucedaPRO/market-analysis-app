package com.market.analysis.unit.presentation.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.market.analysis.application.dto.StockDataDTO;
import com.market.analysis.application.job.BackgroundJob;
import com.market.analysis.application.job.IaValorationJobService;
import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;
import com.market.analysis.domain.port.in.ManageStrategyUseCase;
import com.market.analysis.presentation.controller.AnalyzeTickerController;

@WebMvcTest(AnalyzeTickerController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AnalyzeTickerController View Tests")
class AnalyzeTickerControllerViewTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManageAnalyzeTickerUseCase manageAnalyzeTickerUseCase;

    @MockitoBean
    private ManageStrategyUseCase manageStrategyUseCase;

    @MockitoBean
    private IaValorationJobService iaValorationJobService;

    @Test
    @DisplayName("Should submit IA job and redirect to ticker detail")
    void shouldSubmitIaJobAndRedirect() throws Exception {
        when(iaValorationJobService.submitValorationJob(1L)).thenReturn("job-ia-1");

        mockMvc.perform(post("/analysis/getValorationIA").param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/analysis/ticker/1"))
                .andExpect(flash().attributeExists("uiNotification"));

        verify(iaValorationJobService).submitValorationJob(1L);
    }

    @Test
    @DisplayName("Should render IA job banner when job is active")
    void shouldRenderIaJobBanner() throws Exception {
        StockDataDTO ticker = StockDataDTO.builder()
                .id(1L)
                .ticker("AAPL")
                .currentPrice(new java.math.BigDecimal("150.50"))
                .previousClose(new java.math.BigDecimal("149.00"))
                .openPrice(new java.math.BigDecimal("149.50"))
                .highOfDay(new java.math.BigDecimal("151.00"))
                .lowOfDay(new java.math.BigDecimal("148.50"))
                .volume(1000000L)
                .averageVolume(900000L)
                .evaluationPassed(Boolean.TRUE)
                .build();
        BackgroundJob job = new BackgroundJob("job-ia-1", "ia-valoration", 1L, Instant.now());
        job.markRunning();
        when(manageAnalyzeTickerUseCase.findStockDataById(1L)).thenReturn(ticker);
        when(iaValorationJobService.findActiveJobIdByTickerId(1L)).thenReturn(java.util.Optional.of("job-ia-1"));
        when(iaValorationJobService.getJob("job-ia-1")).thenReturn(java.util.Optional.of(job));

        mockMvc.perform(get("/analysis/ticker/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("job-banner")))
                .andExpect(content().string(containsString("/analysis/ia-jobs/job-ia-1")))
                .andExpect(content().string(containsString("data-started-at=")));
    }

    @Test
    @DisplayName("Should return IA job status as JSON")
    void shouldReturnIaJobStatus() throws Exception {
        BackgroundJob job = new BackgroundJob("job-ia-1", "ia-valoration", 1L, Instant.now());
        job.markRunning();
        when(iaValorationJobService.getJob("job-ia-1")).thenReturn(java.util.Optional.of(job));

        mockMvc.perform(get("/analysis/ia-jobs/job-ia-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value("job-ia-1"))
                .andExpect(jsonPath("$.status").value("RUNNING"));
    }

    @Test
    @DisplayName("Should return 404 for unknown IA job id")
    void shouldReturnNotFoundForUnknownIaJob() throws Exception {
        when(iaValorationJobService.getJob("unknown")).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/analysis/ia-jobs/unknown"))
                .andExpect(status().isNotFound());
    }
}
