package com.market.analysis.presentation.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.market.analysis.application.dto.RuleDefinitionDTO;
import com.market.analysis.domain.port.in.ManageRuleDefinitionUseCase;

import lombok.RequiredArgsConstructor;

/**
 * Controller for managing RuleDefinition views and operations.
 * Handles CRUD operations for rule definitions through the presentation layer.
 */
@Controller
@RequestMapping("/rule-definitions")
@RequiredArgsConstructor
public class RuleDefinitionController {

    private final ManageRuleDefinitionUseCase manageRuleDefinitionUseCase;

    @GetMapping
    public String listRuleDefinitions(Model model) {
        List<RuleDefinitionDTO> ruleDefinitions = manageRuleDefinitionUseCase.getAllRuleDefinitions();

        model.addAttribute("ruleDefinitions", ruleDefinitions);
        return "rule-definitions/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("ruleDefinition", new RuleDefinitionDTO());
        model.addAttribute("isEdit", false);
        model.addAttribute("capabilities", manageRuleDefinitionUseCase.getCatalogCapabilities());
        return "rule-definitions/create";
    }

    @PostMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model) {
        RuleDefinitionDTO ruleDefinitionDto = manageRuleDefinitionUseCase.getRuleDefinitionById(id);
        model.addAttribute("ruleDefinition", ruleDefinitionDto);
        model.addAttribute("isEdit", true);
        return "rule-definitions/create";
    }

    @PostMapping
    public String saveRuleDefinition(@ModelAttribute RuleDefinitionDTO ruleDefinitionDTO) {

        if (ruleDefinitionDTO.getId() == null) {
            manageRuleDefinitionUseCase.createRuleDefinition(ruleDefinitionDTO);
        } else {
            manageRuleDefinitionUseCase.updateRuleDefinition(ruleDefinitionDTO);
        }

        return "redirect:/rule-definitions";
    }

    @PostMapping("/delete")
    public String deleteRuleDefinition(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        manageRuleDefinitionUseCase.deleteRuleDefinition(id);
        redirectAttributes.addFlashAttribute("message", "Estrategia eliminada con éxito.");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/rule-definitions";
    }
}
