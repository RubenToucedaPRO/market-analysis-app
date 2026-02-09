package com.market.analysis.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/analyze")
@RequiredArgsConstructor
public class AnalyzeTickerController {

    private final ManageAnalyzeTickerUseCase manageAnalyzeTickerUseCase;

    @GetMapping("/getTickerData")
    public String getTickerData(@RequestParam String ticker) {
        manageAnalyzeTickerUseCase.getTickerData(ticker);
        return "redirect:/analyze";
    }

    @GetMapping
    public String getAllTickers(Model model) {
        model.addAttribute("tickers", manageAnalyzeTickerUseCase.findAllTickers());
        return "analyze";
    }

    @PostMapping("update")
    public String updateTicker(@RequestBody String entity) {
        return "redirect:/analyze";
    }

    @PostMapping("delete")
    public String deleteTicker(@RequestBody String entity) {

        return "redirect:/analyze";
    }
}
