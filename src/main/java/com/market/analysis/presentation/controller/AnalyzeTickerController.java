package com.market.analysis.presentation.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;
import com.market.analysis.presentation.dto.StockDataDTO;
import com.market.analysis.presentation.mapper.StockDataDTOMapper;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/analysis")
@RequiredArgsConstructor
public class AnalyzeTickerController {

    private static final String REDIRECT_ANALYZE = "redirect:/analysis";

    private final ManageAnalyzeTickerUseCase manageAnalyzeTickerUseCase;
    private final StockDataDTOMapper mapper;

    @GetMapping
    public String getAllTickers(Model model) {
        List<StockDataDTO> tickers = manageAnalyzeTickerUseCase.findAllStocks().stream().map(mapper::toDTO).toList();
        model.addAttribute("tickers", tickers);
        return "analysis/analysis";
    }

    @PostMapping("/getTickerData")
    public String getTickerData(@RequestParam String tickers) {
        manageAnalyzeTickerUseCase.getStockData(tickers);
        return REDIRECT_ANALYZE;
    }

    @PostMapping("/update")
    public String updateTicker(@RequestParam String ticker) {
        manageAnalyzeTickerUseCase.updateStockData(ticker);
        return REDIRECT_ANALYZE;
    }

    @PostMapping("/delete")
    public String deleteTicker(@RequestParam String ticker) {
        manageAnalyzeTickerUseCase.deleteStockDataByTicker(ticker);
        return REDIRECT_ANALYZE;
    }
}
