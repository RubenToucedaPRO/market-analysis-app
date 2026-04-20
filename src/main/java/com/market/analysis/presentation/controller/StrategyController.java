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
import com.market.analysis.domain.port.in.AddSuggestedTickersToAnalysisUseCase;
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
    private static final String ATTR_SUGGESTED_AT = "suggestedAt";
    private static final String MSG_ADD_SNAPSHOT_UNAVAILABLE =
            "La alta desde snapshot de sugerencias no está disponible todavía.";
    private static final String MSG_ADD_SNAPSHOT_NONE =
            "No hay sugerencias aptas en snapshot para añadir.";

    private final ManageStrategyUseCase manageStrategyUseCase;
    private final ManageRuleDefinitionUseCase manageRuleDefinitionUseCase;
    private final Optional<SuggestTickersUseCase> suggestTickersUseCase;
    private final Optional<AddSuggestedTickersToAnalysisUseCase> addSuggestedTickersToAnalysisUseCase;

    @GetMapping
    public String listStrategies(Model model) {
        model.addAttribute("strategies", manageStrategyUseCase.getAllStrategies());
        return "strategies/list";
    }

    @GetMapping("/{id}")
    public String viewStrategyDetail(@PathVariable("id") long strategyId, Model model) {
        StrategyDTO strategyDTO = manageStrategyUseCase.getStrategyById(strategyId);
        model.addAttribute(ATTR_STRATEGY, strategyDTO);
        loadLastSuggestionSnapshot(strategyId, model);
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
        List<String> responseWarnings = response == null || response.getWarnings() == null
                ? List.of()
                : response.getWarnings();

        if (!unmappableRules.isEmpty() || !discarded.isEmpty() || !responseWarnings.isEmpty()) {
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.warning("Sugerencia parcial: revisa trazabilidad de descartes o reglas no mapeables."));
        } else {
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.success("Sugerencias generadas correctamente desde mercado."));
        }

        return "redirect:/strategies/" + strategyId;
    }

    @PostMapping("/{id}/add-suggested-tickers")
    public String addSuggestedTickersToAnalysis(@PathVariable("id") long strategyId,
            RedirectAttributes redirectAttributes) {
        if (addSuggestedTickersToAnalysisUseCase.isEmpty()) {
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.error(MSG_ADD_SNAPSHOT_UNAVAILABLE));
            return "redirect:/strategies/" + strategyId;
        }

        int added = addSuggestedTickersToAnalysisUseCase.get().addFromLatestSnapshot(strategyId);
        if (added > 0) {
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.success("Ticker(s) añadidos desde snapshot de sugerencias: " + added + "."));
        } else {
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.warning(MSG_ADD_SNAPSHOT_NONE));
        }

        return "redirect:/analysis";
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
        model.addAttribute(ATTR_SUGGESTED_TICKERS, filterBySuitabilityStatus(response, TickerSuitabilityStatus.APTO));
        model.addAttribute(ATTR_DISCARDED_TICKERS, filterBySuitabilityStatus(response, TickerSuitabilityStatus.NO_APTO));
        model.addAttribute(ATTR_UNMAPPABLE_RULES, response.getUnmappableRules() == null ? List.of() : response.getUnmappableRules());
        model.addAttribute(ATTR_SUGGESTED_AT, response.getSuggestedAt());
    }
}
