package com.market.analysis.presentation.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.market.analysis.application.dto.RuleDTO;
import com.market.analysis.application.dto.RuleDefinitionDTO;
import com.market.analysis.application.dto.StrategyDTO;
import com.market.analysis.application.dto.StrategyObjectiveDTO;
import com.market.analysis.domain.port.in.ManageRuleDefinitionUseCase;
import com.market.analysis.domain.port.in.ManageStrategyUseCase;
import com.market.analysis.presentation.dto.UiNotification;
import com.market.analysis.presentation.util.WebConstants;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/strategies")
@RequiredArgsConstructor
public class StrategyController {

    private static final String ATTR_RULE_DEFINITIONS = "ruleDefinitions";
    private static final String ATTR_STRATEGY = "strategy";

    private final ManageStrategyUseCase manageStrategyUseCase;
    private final ManageRuleDefinitionUseCase manageRuleDefinitionUseCase;

    @GetMapping
    public String listStrategies(Model model) {
        model.addAttribute("strategies", manageStrategyUseCase.getAllStrategies());
        return "strategies/list";
    }

    @GetMapping("/{id}")
    public String viewStrategyDetail(@PathVariable("id") long strategyId, Model model) {
        StrategyDTO strategyDTO = manageStrategyUseCase.getStrategyById(strategyId);
        model.addAttribute(ATTR_STRATEGY, strategyDTO);
        return "strategies/detail";
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
                .objective(StrategyObjectiveDTO.builder().build())
                .build();

        List<RuleDefinitionDTO> ruleDefinitions = manageRuleDefinitionUseCase.getAllRuleDefinitions();

        model.addAttribute(ATTR_RULE_DEFINITIONS, ruleDefinitions);
        model.addAttribute(ATTR_STRATEGY, strategy);
        model.addAttribute("isEdit", false);

        return "strategies/create";
    }

    @PostMapping("/edit")
    public String showEditForm(@RequestParam("id") long strategyId, Model model) {
        StrategyDTO strategyDTO = manageStrategyUseCase.getStrategyById(strategyId);

        List<RuleDefinitionDTO> ruleDefinitionsDTOs = manageRuleDefinitionUseCase.getAllRuleDefinitions();

        model.addAttribute(ATTR_RULE_DEFINITIONS, ruleDefinitionsDTOs);
        model.addAttribute(ATTR_STRATEGY, strategyDTO);
        model.addAttribute("isEdit", true);

        return "strategies/create";
    }

    @PostMapping
    public String saveStrategy(@ModelAttribute StrategyDTO strategyDTO, RedirectAttributes redirectAttributes) {
        if (strategyDTO.getId() == null) {
            manageStrategyUseCase.createStrategy(strategyDTO);
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.success("Estrategia creada correctamente."));
        } else {
            manageStrategyUseCase.updateStrategy(strategyDTO);
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.success("Estrategia actualizada correctamente."));
        }
        return "redirect:/strategies";
    }

    @PostMapping("/delete")
    public String deleteStrategy(@RequestParam("id") long strategyId, RedirectAttributes redirectAttributes) {
        manageStrategyUseCase.deleteStrategy(strategyId);
        redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success("Estrategia eliminada correctamente."));
        return "redirect:/strategies";
    }
}
