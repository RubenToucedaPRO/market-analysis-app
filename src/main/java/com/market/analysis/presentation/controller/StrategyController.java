package com.market.analysis.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.port.in.ManageStrategyUseCase;

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
        // En lugar de enviarlo vacío, inicializamos la lista de reglas
        // para que Thymeleaf tenga donde iterar al menos una fila.
        Strategy strategy = Strategy.builder()
                .name("")
                .description("")
                // IMPORTANTE: Inicializar la lista con una regla vacía
                .rules(new java.util.ArrayList<>(java.util.List.of(Rule.builder().build())))
                .build();

        model.addAttribute("strategy", strategy);
        return "strategies/create";
    }

    @PostMapping
    public String saveStrategy(@ModelAttribute Strategy strategy) {
        manageStrategyUseCase.createStrategy(strategy);
        return "redirect:/strategies";
    }
}