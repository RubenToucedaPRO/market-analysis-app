package com.market.analysis.presentation.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.market.analysis.domain.port.in.ManageProhibitedTickerUseCase;
import com.market.analysis.presentation.dto.ProhibitedTickerDTO;
import com.market.analysis.presentation.mapper.ProhibitedTickerDTOMapper;

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
    private final ProhibitedTickerDTOMapper mapper;

    @GetMapping
    public String listProhibitedTickers(Model model) {
        List<ProhibitedTickerDTO> prohibitedTickers = manageProhibitedTickerUseCase.getAllProhibitedTickers()
                .stream()
                .map(mapper::toDTO).toList();

        model.addAttribute("prohibitedTickers", prohibitedTickers);
        return "prohibited-tickers/list";
    }

    @PostMapping("/delete")
    public String deleteProhibitedTicker(@RequestParam("id") Long id) {
        manageProhibitedTickerUseCase.removeProhibitedTicker(id);
        return "redirect:/prohibited-tickers";
    }
    
}
