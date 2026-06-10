package com.market.analysis.presentation.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
import com.market.analysis.application.dto.SuggestTickersRequestDTO;
import com.market.analysis.application.dto.SuggestTickersResponseDTO;
import com.market.analysis.application.dto.SuggestedTickerDTO;
import com.market.analysis.application.dto.TickerSuitabilityStatus;
import com.market.analysis.domain.port.in.ManageRuleDefinitionUseCase;
import com.market.analysis.domain.port.in.ManageStrategyUseCase;
import com.market.analysis.domain.port.in.SuggestTickersUseCase;
import com.market.analysis.presentation.dto.UiNotification;
import com.market.analysis.presentation.util.WebConstants;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/strategies")
@RequiredArgsConstructor
public class StrategyController {

    private final ManageStrategyUseCase manageStrategyUseCase;
    private final ManageRuleDefinitionUseCase manageRuleDefinitionUseCase;
    private final Optional<SuggestTickersUseCase> suggestTickersUseCase;
    private final MessageSource messageSource;

    @GetMapping
    public String listStrategies(Model model) {
        model.addAttribute(WebConstants.ATTR_STRATEGIES, manageStrategyUseCase.getAllStrategies());
        return WebConstants.TEMPLATE_STRATEGIES_LIST;
    }

    @GetMapping("/{id}")
    public String viewStrategyDetail(@PathVariable("id") long strategyId, Model model) {
        StrategyDTO strategyDTO = manageStrategyUseCase.getStrategyById(strategyId);
        model.addAttribute(WebConstants.ATTR_STRATEGY, strategyDTO);
        loadLastSuggestionSnapshot(strategyId, model);
        return WebConstants.TEMPLATE_STRATEGIES_DETAIL;
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

        model.addAttribute(WebConstants.ATTR_RULE_DEFINITIONS, ruleDefinitions);
        model.addAttribute(WebConstants.ATTR_STRATEGY, strategy);
        model.addAttribute(WebConstants.ATTR_IS_EDIT, false);

        return WebConstants.TEMPLATE_STRATEGIES_CREATE;
    }

    @PostMapping("/edit")
    public String showEditForm(@RequestParam("id") long strategyId, Model model) {
        StrategyDTO strategyDTO = manageStrategyUseCase.getStrategyById(strategyId);

        List<RuleDefinitionDTO> ruleDefinitionsDTOs = manageRuleDefinitionUseCase.getAllRuleDefinitions();

        model.addAttribute(WebConstants.ATTR_RULE_DEFINITIONS, ruleDefinitionsDTOs);
        model.addAttribute(WebConstants.ATTR_STRATEGY, strategyDTO);
        model.addAttribute(WebConstants.ATTR_IS_EDIT, true);

        return WebConstants.TEMPLATE_STRATEGIES_CREATE;
    }

    @PostMapping
    public String saveStrategy(@ModelAttribute StrategyDTO strategyDTO, RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        if (strategyDTO.getId() == null) {
            manageStrategyUseCase.createStrategy(strategyDTO);
            String message = messageSource.getMessage("strategy.created", null, locale);
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.success(message));
        } else {
            manageStrategyUseCase.updateStrategy(strategyDTO);
            String message = messageSource.getMessage("strategy.updated", null, locale);
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.success(message));
        }
        return WebConstants.REDIRECT_STRATEGIES;
    }

    @PostMapping("/delete")
    public String deleteStrategy(@RequestParam("id") long strategyId, RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        manageStrategyUseCase.deleteStrategy(strategyId);
        String message = messageSource.getMessage("strategy.deleted", null, locale);
        redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success(message));
        return WebConstants.REDIRECT_STRATEGIES;
    }

    @PostMapping("/{id}/suggest-tickers")
    public String suggestTickersFromMarket(@PathVariable("id") long strategyId, RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        if (suggestTickersUseCase.isEmpty()) {
            String message = messageSource.getMessage("strategy.suggestion.unavailable", null, locale);
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.error(message));
            return WebConstants.REDIRECT_STRATEGIES_PREFIX + strategyId;
        }

        SuggestTickersResponseDTO response;
        try {
            response = suggestTickersUseCase.get().suggestTickers(
                    SuggestTickersRequestDTO.builder()
                            .strategyId(strategyId)
                            .build());
        } catch (RuntimeException ex) {
            String message = messageSource.getMessage("strategy.suggestion.failed", null, locale);
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.error(message));
            return WebConstants.REDIRECT_STRATEGIES_PREFIX + strategyId;
        }

        List<SuggestedTickerDTO> discarded = filterBySuitabilityStatus(response, TickerSuitabilityStatus.NO_APTO);
        List<String> unmappableRules = response == null || response.getUnmappableRules() == null
                ? List.of()
                : response.getUnmappableRules();
        List<String> responseWarnings = response == null || response.getWarnings() == null
                ? List.of()
                : response.getWarnings();

        if (!unmappableRules.isEmpty() || !discarded.isEmpty() || !responseWarnings.isEmpty()) {
            String message = messageSource.getMessage("strategy.suggestion.partial", null, locale);
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.warning(message));
        } else {
            String message = messageSource.getMessage("strategy.suggestion.success", null, locale);
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.success(message));
        }

        return WebConstants.REDIRECT_STRATEGIES_PREFIX + strategyId;
    }

    @PostMapping("/{id}/add-suggested-tickers")
    public String addSuggestedTickersToAnalysis(@PathVariable("id") long strategyId,
            RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        int added = suggestTickersUseCase
            .map(useCase -> useCase.convertSuggestedTickersToAnalysis(strategyId))
            .orElse(0);
        if (added > 0) {
            String message = messageSource.getMessage("strategy.tickers.switched",
                    new Object[] { added }, locale);
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.success(message));
        } else {
            String message = messageSource.getMessage("strategy.suggestion.none_added", null, locale);
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.warning(message));
        }

        return WebConstants.REDIRECT_ANALYSIS;
    }

    private List<SuggestedTickerDTO> filterBySuitabilityStatus(SuggestTickersResponseDTO response,
            TickerSuitabilityStatus status) {
        if (response == null || response.getSuggestedTickers() == null) {
            return List.of();
        }
        return response.getSuggestedTickers().stream()
                .filter(ticker -> ticker.getSuitabilityStatus() == status)
                .toList();
    }

    private void loadLastSuggestionSnapshot(long strategyId, Model model) {
        if (suggestTickersUseCase.isEmpty()) {
            return;
        }
        Optional<SuggestTickersResponseDTO> snapshot = suggestTickersUseCase.get().getLatestSuggestionSnapshot(strategyId);
        if (snapshot.isEmpty()) {
            return;
        }

        SuggestTickersResponseDTO response = snapshot.get();
        model.addAttribute(WebConstants.ATTR_SUGGESTED_TICKERS, filterBySuitabilityStatus(response, TickerSuitabilityStatus.APTO));
        model.addAttribute(WebConstants.ATTR_DISCARDED_TICKERS, filterBySuitabilityStatus(response, TickerSuitabilityStatus.NO_APTO));
        model.addAttribute(WebConstants.ATTR_UNMAPPABLE_RULES, response.getUnmappableRules() == null ? List.of() : response.getUnmappableRules());
        model.addAttribute(WebConstants.ATTR_SUGGESTED_AT, response.getSuggestedAt());
    }
}
