package com.market.analysis.presentation.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.port.in.ManageRuleDefinitionUseCase;
import com.market.analysis.domain.port.in.ManageStrategyUseCase;
import com.market.analysis.presentation.dto.RuleDTO;
import com.market.analysis.presentation.dto.RuleDefinitionDTO;
import com.market.analysis.presentation.dto.StrategyDTO;
import com.market.analysis.presentation.mapper.RuleDefinitionDTOMapper;
import com.market.analysis.presentation.mapper.StrategyDTOMapper;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/strategies")
@RequiredArgsConstructor
public class StrategyController {

    private static final String ATTR_RULE_DEFINITIONS = "ruleDefinitions";
    private static final String ATTR_STRATEGY = "strategy";

    private final ManageStrategyUseCase manageStrategyUseCase;
    private final ManageRuleDefinitionUseCase manageRuleDefinitionUseCase;
    private final RuleDefinitionDTOMapper ruleDefinitionDTOMapper;
    private final StrategyDTOMapper strategyDTOMapper;

    @GetMapping
    public String listStrategies(Model model) {
        model.addAttribute("strategies", manageStrategyUseCase.getAllStrategies());
        return "strategies/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        RuleDTO emptyRule = RuleDTO.builder()
                .name("")
                .build();

        StrategyDTO strategy = StrategyDTO.builder()
                .name("")
                .description("")
                .rules(new ArrayList<>(List.of(emptyRule)))
                .build();

        List<RuleDefinitionDTO> ruleDefinitions = manageRuleDefinitionUseCase.getAllRuleDefinitions()
                .stream()
                .map(ruleDefinitionDTOMapper::toDTO)
                .toList();

        model.addAttribute(ATTR_RULE_DEFINITIONS, ruleDefinitions);
        model.addAttribute(ATTR_STRATEGY, strategy);

        return "strategies/create";
    }

    @PostMapping("/edit")
    public String showEditForm(@RequestParam("id") long strategyId, Model model) {
        Strategy strategy = manageStrategyUseCase.getStrategyById(strategyId);
        StrategyDTO strategyDTO = strategyDTOMapper.toDTO(strategy);

        List<RuleDefinitionDTO> ruleDefinitions = manageRuleDefinitionUseCase.getAllRuleDefinitions()
                .stream()
                .map(ruleDefinitionDTOMapper::toDTO)
                .toList();

        model.addAttribute(ATTR_RULE_DEFINITIONS, ruleDefinitions);
        model.addAttribute(ATTR_STRATEGY, strategyDTO);

        return "strategies/create";
    }

    @PostMapping
    public String saveStrategy(@ModelAttribute StrategyDTO strategyDTO) {
        Strategy strategy = strategyDTOMapper.toDomain(strategyDTO);
        manageStrategyUseCase.createStrategy(strategy);
        return "redirect:/strategies";
    }

    @PostMapping("/delete")
    public String deleteStrategy(@RequestParam("id") long strategyId) {
        manageStrategyUseCase.deleteStrategy(strategyId);
        return "redirect:/strategies";
    }
}
