package com.market.analysis.presentation.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import com.market.analysis.application.dto.SuggestTickersRequestDTO;
import com.market.analysis.application.dto.SuggestTickersResponseDTO;
import com.market.analysis.application.dto.SuggestedTickerDTO;
import com.market.analysis.application.dto.StrategyDTO;
import com.market.analysis.application.dto.StrategyObjectiveDTO;
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

    private static final String ATTR_RULE_DEFINITIONS = "ruleDefinitions";
    private static final String ATTR_STRATEGY = "strategy";
    private static final String ATTR_SUGGESTED_TICKERS = "suggestedTickers";
    private static final String ATTR_DISCARDED_TICKERS = "discardedTickers";
    private static final String ATTR_UNMAPPABLE_RULES = "unmappableRules";

    private final ManageStrategyUseCase manageStrategyUseCase;
    private final ManageRuleDefinitionUseCase manageRuleDefinitionUseCase;
    private final Optional<SuggestTickersUseCase> suggestTickersUseCase;

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

    @PostMapping("/{id}/suggest-tickers")
    public String suggestTickersFromMarket(@PathVariable("id") long strategyId, RedirectAttributes redirectAttributes) {
        if (suggestTickersUseCase.isEmpty()) {
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.error("La sugerencia de tickers desde mercado no está disponible todavía."));
            return "redirect:/strategies/" + strategyId;
        }

        SuggestTickersResponseDTO response;
        try {
            response = suggestTickersUseCase.get().suggestTickers(
                    SuggestTickersRequestDTO.builder()
                            .strategyId(strategyId)
                            .build());
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.error("No se pudo sugerir tickers desde mercado en este momento."));
            return "redirect:/strategies/" + strategyId;
        }

        List<SuggestedTickerDTO> suggested = filterBySuitabilityStatus(response, TickerSuitabilityStatus.APTO);
        List<SuggestedTickerDTO> discarded = filterBySuitabilityStatus(response, TickerSuitabilityStatus.NO_APTO);
        List<String> unmappableRules = response == null || response.getUnmappableRules() == null
                ? List.of()
                : response.getUnmappableRules();

        redirectAttributes.addFlashAttribute(ATTR_SUGGESTED_TICKERS, suggested);
        redirectAttributes.addFlashAttribute(ATTR_DISCARDED_TICKERS, discarded);
        redirectAttributes.addFlashAttribute(ATTR_UNMAPPABLE_RULES, unmappableRules);

        if (!unmappableRules.isEmpty() || !discarded.isEmpty()) {
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.warning("Sugerencia parcial: revisa trazabilidad de descartes o reglas no mapeables."));
        } else {
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.success("Sugerencias generadas correctamente desde mercado."));
        }

        return "redirect:/strategies/" + strategyId;
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
}
