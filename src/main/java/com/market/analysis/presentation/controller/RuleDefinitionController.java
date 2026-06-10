package com.market.analysis.presentation.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
import com.market.analysis.presentation.dto.UiNotification;
import com.market.analysis.presentation.util.WebConstants;

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
    private final MessageSource messageSource;

    @GetMapping
    public String listRuleDefinitions(Model model) {
        List<RuleDefinitionDTO> ruleDefinitions = manageRuleDefinitionUseCase.getAllRuleDefinitions();

        model.addAttribute(WebConstants.ATTR_RULE_DEFINITIONS, ruleDefinitions);
        return WebConstants.TEMPLATE_RULE_DEFINITIONS_LIST;
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute(WebConstants.ATTR_RULE_DEFINITION, new RuleDefinitionDTO());
        model.addAttribute(WebConstants.ATTR_IS_EDIT, false);
        model.addAttribute(WebConstants.ATTR_CAPABILITIES, manageRuleDefinitionUseCase.getCatalogCapabilities());
        return WebConstants.TEMPLATE_RULE_DEFINITIONS_CREATE;
    }

    @PostMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model) {
        RuleDefinitionDTO ruleDefinitionDto = manageRuleDefinitionUseCase.getRuleDefinitionById(id);
        model.addAttribute(WebConstants.ATTR_RULE_DEFINITION, ruleDefinitionDto);
        model.addAttribute(WebConstants.ATTR_IS_EDIT, true);
        return WebConstants.TEMPLATE_RULE_DEFINITIONS_CREATE;
    }

    @PostMapping
    public String saveRuleDefinition(@ModelAttribute RuleDefinitionDTO ruleDefinitionDTO,
            RedirectAttributes redirectAttributes) {

        Locale locale = LocaleContextHolder.getLocale();
        if (ruleDefinitionDTO.getId() == null) {
            manageRuleDefinitionUseCase.createRuleDefinition(ruleDefinitionDTO);
            String message = messageSource.getMessage("ruledefinition.created", null, locale);
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.success(message));
        } else {
            manageRuleDefinitionUseCase.updateRuleDefinition(ruleDefinitionDTO);
            String message = messageSource.getMessage("ruledefinition.updated", null, locale);
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.success(message));
        }

        return WebConstants.REDIRECT_RULE_DEFINITIONS;
    }

    @PostMapping("/delete")
    public String deleteRuleDefinition(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        manageRuleDefinitionUseCase.deleteRuleDefinition(id);
        String message = messageSource.getMessage("ruledefinition.deleted", null, locale);
        redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success(message));
        return WebConstants.REDIRECT_RULE_DEFINITIONS;
    }
}
