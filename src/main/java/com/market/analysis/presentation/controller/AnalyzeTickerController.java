package com.market.analysis.presentation.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;
import com.market.analysis.presentation.dto.TickerDataDTO;
import com.market.analysis.presentation.mapper.TickerDataDTOMapper;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/analyze")
@RequiredArgsConstructor
public class AnalyzeTickerController {

    private final ManageAnalyzeTickerUseCase manageAnalyzeTickerUseCase;
    private final TickerDataDTOMapper mapper;

    @GetMapping
    public String getAllTickers(Model model) {
        List<TickerDataDTO> tickers = manageAnalyzeTickerUseCase.findAllTickers().stream().map(mapper::toDTO).toList();
        model.addAttribute("tickers", tickers);
        return "analyze/analyze";
    }

    @PostMapping("/getTickerData")
    public String getTickerData(@RequestParam String tickers) {
        manageAnalyzeTickerUseCase.getTickerData(tickers);
        return "redirect:/analyze";
    }

    @PostMapping("/update")
    public String updateTicker(@RequestBody String entity) {
        return "redirect:/analyze";
    }

    @PostMapping("/delete")
    public String deleteTicker(@RequestParam String ticker) {
        manageAnalyzeTickerUseCase.deleteTickerDataByTicker(ticker);
        return "redirect:/analyze";
    }
}
