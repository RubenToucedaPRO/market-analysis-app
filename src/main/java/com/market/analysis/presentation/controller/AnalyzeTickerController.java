package com.market.analysis.presentation.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
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
import com.market.analysis.application.dto.StockDataDTO;
import com.market.analysis.application.dto.StrategyDTO;
import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;
import com.market.analysis.domain.port.in.ManageStrategyUseCase;
import com.market.analysis.presentation.dto.UiNotification;
import com.market.analysis.presentation.util.WebConstants;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/analysis")
@RequiredArgsConstructor
public class AnalyzeTickerController {

    private final ManageAnalyzeTickerUseCase manageAnalyzeTickerUseCase;
    private final ManageStrategyUseCase manageStrategyUseCase;
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
        Locale locale = LocaleContextHolder.getLocale();
        manageAnalyzeTickerUseCase.updateStockData(id);
        String message = messageSource.getMessage("ticker.updated", null, locale);
        redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success(message));
        return WebConstants.REDIRECT_ANALYSIS;
    }

    @PostMapping("/ticker/{id}/update")
    public String updateTickerFromDetail(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        manageAnalyzeTickerUseCase.updateStockData(id);
        String message = messageSource.getMessage("ticker.updated", null, locale);
        redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success(message));
        return WebConstants.REDIRECT_ANALYSIS + "/ticker/" + id;
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

    @GetMapping("/ticker/{id}")
    public String getTickerDetail(@PathVariable Long id, Model model) {
        StockDataDTO ticker = manageAnalyzeTickerUseCase.findStockDataById(id);
        model.addAttribute(WebConstants.ATTR_TICKER, ticker);
        return WebConstants.TEMPLATE_TICKER_DETAIL;
    }

    /**
     * F2.7 — JSON endpoint that returns the OHLCV candle series plus scalar
     * SMA20/50/200 values for the given stock. Consumed by candle-chart.js and
     * mini-chart.js via {@code fetch()}.
     */
    @GetMapping(value = "/ticker/{id}/candles", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CandleChartDTO getCandleChart(@PathVariable Long id) {
        return manageAnalyzeTickerUseCase.findCandlesByStockId(id);
    }

    /**
     * F2.8 — Thymeleaf view that renders the full interactive candlestick chart
     * with SMA20/50/200 overlays via TradingView Lightweight Charts.
     */
    @GetMapping("/ticker/{id}/chart")
    public String getTickerChart(@PathVariable Long id, Model model) {
        StockDataDTO ticker = manageAnalyzeTickerUseCase.findStockDataById(id);
        model.addAttribute(WebConstants.ATTR_TICKER, ticker);
        return WebConstants.TEMPLATE_TICKER_CHART;
    }

    @PostMapping("/getValorationIA")
    public String getValorationIA(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        boolean generated = manageAnalyzeTickerUseCase.getValorationIA(id);
        if (generated) {
            String message = messageSource.getMessage("ticker.ia.success", null, locale);
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success(message));
        } else {
            String message = messageSource.getMessage("ticker.ia.failed", null, locale);
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.error(message));
        }
        return WebConstants.REDIRECT_ANALYSIS + "/ticker/" + id;
    }

}
