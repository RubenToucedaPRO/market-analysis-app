package com.market.analysis.presentation.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.domain.port.in.ManageRuleDefinitionUseCase;
import com.market.analysis.presentation.dto.RuleDefinitionDTO;
import com.market.analysis.presentation.mapper.RuleDefinitionDTOMapper;

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
    private final RuleDefinitionDTOMapper mapper;

    @GetMapping
    public String listRuleDefinitions(Model model) {
        List<RuleDefinitionDTO> ruleDefinitions = manageRuleDefinitionUseCase.getAllRuleDefinitions()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        
        model.addAttribute("ruleDefinitions", ruleDefinitions);
        return "rule-definitions/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("ruleDefinition", new RuleDefinitionDTO());
        model.addAttribute("isEdit", false);
        return "rule-definitions/create";
    }

    @PostMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model) {
        RuleDefinition ruleDefinition = manageRuleDefinitionUseCase.getRuleDefinitionById(id);
        model.addAttribute("ruleDefinition", mapper.toDTO(ruleDefinition));
        model.addAttribute("isEdit", true);
        return "rule-definitions/create";
    }

    @PostMapping
    public String saveRuleDefinition(@ModelAttribute RuleDefinitionDTO ruleDefinitionDTO) {
        RuleDefinition ruleDefinition = mapper.toDomain(ruleDefinitionDTO);
        
        if (ruleDefinitionDTO.getId() == null) {
            manageRuleDefinitionUseCase.createRuleDefinition(ruleDefinition);
        } else {
            manageRuleDefinitionUseCase.updateRuleDefinition(ruleDefinition);
        }
        
        return "redirect:/rule-definitions";
    }

    @PostMapping("/delete")
    public String deleteRuleDefinition(@RequestParam("id") Long id) {
        manageRuleDefinitionUseCase.deleteRuleDefinition(id);
        return "redirect:/rule-definitions";
    }
}
