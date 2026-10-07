package com.market.analysis.presentation.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.market.analysis.application.dto.CandleChartDTO;
import com.market.analysis.application.dto.IaValorationJobStatusDTO;
import com.market.analysis.application.dto.StockDataDTO;
import com.market.analysis.application.dto.StrategyDTO;
import com.market.analysis.application.job.BackgroundJob;
import com.market.analysis.application.job.IaValorationJobService;
import com.market.analysis.application.job.JobRejectedException;
import com.market.analysis.application.job.JobStatus;
import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;
import com.market.analysis.domain.port.in.ManageStrategyUseCase;
import com.market.analysis.presentation.dto.UiNotification;
import com.market.analysis.presentation.util.ElapsedTimeFormatter;
import com.market.analysis.presentation.util.WebConstants;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/analysis")
@RequiredArgsConstructor
public class AnalyzeTickerController {

    private final ManageAnalyzeTickerUseCase manageAnalyzeTickerUseCase;
    private final ManageStrategyUseCase manageStrategyUseCase;
    private final IaValorationJobService iaValorationJobService;
    private final MessageSource messageSource;

    @GetMapping
    public String getAllTickers(Model model) {
        List<StockDataDTO> tickers = manageAnalyzeTickerUseCase.findAllStocks();
        List<StrategyDTO> strategies = manageStrategyUseCase.getAllStrategies();

        model.addAttribute(WebConstants.ATTR_TICKERS, tickers);
        model.addAttribute(WebConstants.ATTR_STRATEGIES, strategies);
        return WebConstants.TEMPLATE_ANALYSIS;
    }

    @PostMapping("/getTickerData")
    public String getTickerData(@RequestParam String tickers, @RequestParam Long strategyId,
            RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        if (strategyId == null) {
            throw new IllegalArgumentException("Strategy selection is required");
        }
        manageAnalyzeTickerUseCase.getStockData(tickers, strategyId);
        String message = messageSource.getMessage("ticker.added", null, locale);
        redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success(message));
        return WebConstants.REDIRECT_ANALYSIS;
    }

    @PostMapping("/update")
    public String updateTicker(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        performUpdate(id, redirectAttributes);
        return WebConstants.REDIRECT_ANALYSIS;
    }

    @PostMapping("/ticker/{id:\\d+}/update")
    public String updateTickerFromDetail(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        performUpdate(id, redirectAttributes);
        return WebConstants.REDIRECT_ANALYSIS_TICKER_PREFIX + id;
    }

    private void performUpdate(Long id, RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        manageAnalyzeTickerUseCase.updateStockData(id);
        String message = messageSource.getMessage("ticker.updated", null, locale);
        redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success(message));
    }

    @PostMapping("/delete")
    public String deleteTicker(@RequestParam Long id, @RequestParam String ticker,
            RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        manageAnalyzeTickerUseCase.deleteById(id, ticker);
        String message = messageSource.getMessage("ticker.deleted",
                new Object[] { ticker }, locale);
        redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success(message));
        return WebConstants.REDIRECT_ANALYSIS;
    }

    @GetMapping("/ticker/{id:\\d+}")
    public String getTickerDetail(@PathVariable Long id,
            @RequestParam(value = "ia", required = false) String iaResult,
            Model model) {
        StockDataDTO ticker = manageAnalyzeTickerUseCase.findStockDataById(id);
        model.addAttribute(WebConstants.ATTR_TICKER, ticker);
        if ("failed".equals(iaResult)) {
            String message = messageSource.getMessage("ticker.ia.failed", null, LocaleContextHolder.getLocale());
            model.addAttribute(WebConstants.UI_NOTIFICATION_KEY, UiNotification.error(message));
        }
        resolveActiveIaJob(id).ifPresent(job -> {
            model.addAttribute(WebConstants.ATTR_IA_JOB_ID, job.getJobId());
            model.addAttribute(WebConstants.ATTR_IA_JOB_STARTED_AT,
                    job.getStartedAt().truncatedTo(ChronoUnit.MILLIS).toString());
            model.addAttribute(WebConstants.ATTR_IA_JOB_ELAPSED,
                    ElapsedTimeFormatter.format(job.getStartedAt(), Instant.now()));
        });
        return WebConstants.TEMPLATE_TICKER_DETAIL;
    }

    private Optional<BackgroundJob> resolveActiveIaJob(long tickerId) {
        return iaValorationJobService.findActiveJobIdByTickerId(tickerId)
                .flatMap(iaValorationJobService::getJob);
    }

    /**
     * F2.7 — JSON endpoint that returns the OHLCV candle series plus scalar
     * SMA20/50/200 values for the given stock. Consumed by candle-chart.js and
     * mini-chart.js via {@code fetch()}.
     */
    @GetMapping(value = "/ticker/{id:\\d+}/candles", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CandleChartDTO getCandleChart(@PathVariable Long id) {
        return manageAnalyzeTickerUseCase.findCandlesByStockId(id);
    }

    /**
     * F2.8 — Thymeleaf view that renders the full interactive candlestick chart
     * with SMA20/50/200 overlays via TradingView Lightweight Charts.
     */
    @GetMapping("/ticker/{id:\\d+}/chart")
    public String getTickerChart(@PathVariable Long id, Model model) {
        StockDataDTO ticker = manageAnalyzeTickerUseCase.findStockDataById(id);
        model.addAttribute(WebConstants.ATTR_TICKER, ticker);
        return WebConstants.TEMPLATE_TICKER_CHART;
    }

    @PostMapping("/getValorationIA")
    public String getValorationIA(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            iaValorationJobService.submitValorationJob(id);
        } catch (JobRejectedException ex) {
            String message = messageSource.getMessage("ticker.ia.job.busy", null, locale);
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.warning(message));
            return WebConstants.REDIRECT_ANALYSIS_TICKER_PREFIX + id;
        }
        String message = messageSource.getMessage("ticker.ia.job.started", null, locale);
        redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
            UiNotification.success(message));
        return WebConstants.REDIRECT_ANALYSIS_TICKER_PREFIX + id;
    }

    @GetMapping("/ia-jobs/{jobId}")
    @ResponseBody
    public ResponseEntity<IaValorationJobStatusDTO> getIaJobStatus(@PathVariable("jobId") String jobId) {
        Locale locale = LocaleContextHolder.getLocale();
        return iaValorationJobService.getJob(jobId)
                .map(job -> ResponseEntity.ok(IaValorationJobStatusDTO.from(job, resolveIaJobMessage(job, locale))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private String resolveIaJobMessage(BackgroundJob job, Locale locale) {
        if (job.getStatus() == JobStatus.FAILED) {
            return messageSource.getMessage("ticker.ia.failed", null, locale);
        }
        return null;
    }

}
