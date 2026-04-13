package com.market.analysis.presentation.controller;

import java.util.List;

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

    private static final String REDIRECT_ANALYZE = "redirect:/analysis";

    private final ManageAnalyzeTickerUseCase manageAnalyzeTickerUseCase;
    private final ManageStrategyUseCase manageStrategyUseCase;

    @GetMapping
    public String getAllTickers(Model model) {
        List<StockDataDTO> tickers = manageAnalyzeTickerUseCase.findAllStocks();
        List<StrategyDTO> strategies = manageStrategyUseCase.getAllStrategies();

        model.addAttribute("tickers", tickers);
        model.addAttribute("strategies", strategies);
        return "analysis/analysis";
    }

    @PostMapping("/getTickerData")
    public String getTickerData(@RequestParam String tickers, @RequestParam Long strategyId,
            RedirectAttributes redirectAttributes) {
        if (strategyId == null) {
            throw new IllegalArgumentException("Strategy selection is required");
        }
        manageAnalyzeTickerUseCase.getStockData(tickers, strategyId);
        redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Ticker(s) añadidos y analizados correctamente."));
        return REDIRECT_ANALYZE;
    }

    @PostMapping("/update")
    public String updateTicker(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        manageAnalyzeTickerUseCase.updateStockData(id);
        redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Datos del ticker actualizados correctamente."));
        return REDIRECT_ANALYZE;
    }

    @PostMapping("/ticker/{id}/update")
    public String updateTickerFromDetail(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        manageAnalyzeTickerUseCase.updateStockData(id);
        redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Datos del ticker actualizados correctamente."));
        return REDIRECT_ANALYZE + "/ticker/" + id;
    }

    @PostMapping("/delete")
    public String deleteTicker(@RequestParam Long id, @RequestParam String ticker,
            RedirectAttributes redirectAttributes) {
        manageAnalyzeTickerUseCase.deleteById(id, ticker);
        redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Ticker '" + ticker + "' eliminado correctamente."));
        return REDIRECT_ANALYZE;
    }

    @GetMapping("/ticker/{id}")
    public String getTickerDetail(@PathVariable Long id, Model model) {
        StockDataDTO ticker = manageAnalyzeTickerUseCase.findStockDataById(id);
        model.addAttribute("ticker", ticker);
        return "analysis/ticker-detail";
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
        model.addAttribute("ticker", ticker);
        return "analysis/ticker-chart";
    }

    @PostMapping("/getValorationIA")
    public String getValorationIA(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        manageAnalyzeTickerUseCase.getValorationIA(id);
        redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Valoración IA generada y guardada correctamente."));
        return REDIRECT_ANALYZE;
    }

}
