package com.market.analysis.presentation.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.market.analysis.application.dto.ProhibitedTickerDTO;
import com.market.analysis.domain.port.in.ManageProhibitedTickerUseCase;

import lombok.RequiredArgsConstructor;


/**
 * Controller for managing ProhibitedTicker views and operations.
 * Handles CRUD operations for prohibited tickers through the presentation
 * layer.
 */
@Controller
@RequestMapping("/prohibited-tickers")
@RequiredArgsConstructor
public class ProhibitedTickerController {

    private final ManageProhibitedTickerUseCase manageProhibitedTickerUseCase;

    @GetMapping
    public String listProhibitedTickers(Model model) {
        List<ProhibitedTickerDTO> prohibitedTickers = manageProhibitedTickerUseCase.getAllProhibitedTickers();

        model.addAttribute("prohibitedTickers", prohibitedTickers);
        return "prohibited-tickers/list";
    }

    @PostMapping("/delete")
    public String deleteProhibitedTicker(@RequestParam("ticker") String ticker) {
        manageProhibitedTickerUseCase.removeProhibitedTicker(ticker);
        return "redirect:/prohibited-tickers";
    }
    
}
