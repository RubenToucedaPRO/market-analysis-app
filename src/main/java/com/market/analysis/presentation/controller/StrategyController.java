package com.market.analysis.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.service.ManageStrategyUseCase;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/strategies")
@RequiredArgsConstructor
public class StrategyController {

    private final ManageStrategyUseCase manageStrategyUseCase;

    @GetMapping
    public String listStrategies(Model model) {
        model.addAttribute("strategies", manageStrategyUseCase.getAllStrategies());
        return "strategies/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        // Pasamos una estrategia vacía para que el formulario la rellene
        model.addAttribute("strategy", Strategy.builder().build());
        return "strategies/create";
    }

    @PostMapping
    public String saveStrategy(@ModelAttribute Strategy strategy) {
        manageStrategyUseCase.createStrategy(strategy);
        return "redirect:/strategies";
    }
}