package com.market.analysis.presentation.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.market.analysis.application.dto.RuleDTO;
import com.market.analysis.application.dto.RuleDefinitionDTO;
import com.market.analysis.application.dto.StrategyDTO;
import com.market.analysis.application.dto.StrategyObjectiveDTO;
import com.market.analysis.application.dto.SuggestJobStatusDTO;
import com.market.analysis.application.dto.SuggestTickersResponseDTO;
import com.market.analysis.application.dto.SuggestedTickerDTO;
import com.market.analysis.application.dto.TickerSuitabilityStatus;
import com.market.analysis.application.dto.UpdateStrategyResult;
import com.market.analysis.application.job.SuggestJobRejectedException;
import com.market.analysis.application.job.SuggestTickerJob;
import com.market.analysis.application.job.SuggestTickerJobService;
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
    private final SuggestTickerJobService suggestTickerJobService;
    private final MessageSource messageSource;

    @GetMapping
    public String listStrategies(Model model) {
        model.addAttribute(WebConstants.ATTR_STRATEGIES, manageStrategyUseCase.getAllStrategies());
        return WebConstants.TEMPLATE_STRATEGIES_LIST;
    }

    @GetMapping("/{id:\\d+}")
    public String viewStrategyDetail(@PathVariable("id") long strategyId,
            @RequestParam(value = "jobId", required = false) String jobIdParam,
            Model model) {
        StrategyDTO strategyDTO = manageStrategyUseCase.getStrategyById(strategyId);
        model.addAttribute(WebConstants.ATTR_STRATEGY, strategyDTO);
        loadLastSuggestionSnapshot(strategyId, model);
        resolveSuggestJobId(jobIdParam, strategyId).ifPresent(jobId ->
                model.addAttribute(WebConstants.ATTR_SUGGEST_JOB_ID, jobId));
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
                .threshold(100)
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

    /**
     * Redirects GET access to the POST-only {@code /edit} and {@code /delete}
     * actions back to the strategy list. Without this, such URLs fall through
     * to the numeric detail mapping (or a 405) and surface a cryptic error
     * instead of a safe landing page (e.g. stale tabs, error-redirect chains).
     *
     * @return redirect to the strategy list
     */
    @GetMapping({"/edit", "/delete"})
    public String redirectPostOnlyActions() {
        return WebConstants.REDIRECT_STRATEGIES;
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
            UpdateStrategyResult result = manageStrategyUseCase.updateStrategy(strategyDTO);
            List<String> degraded = result.getDegradedTickers() == null ? List.of() : result.getDegradedTickers();
            if (degraded.isEmpty()) {
                String message = messageSource.getMessage("strategy.updated", null, locale);
                redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                        UiNotification.success(message));
            } else {
                String message = messageSource.getMessage("strategy.updated.partial",
                        new Object[]{String.join(", ", degraded)}, locale);
                redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                        UiNotification.warning(message));
            }
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

    @PostMapping("/{id:\\d+}/suggest-tickers")
    public String suggestTickersFromMarket(@PathVariable("id") long strategyId, RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        if (suggestTickersUseCase.isEmpty()) {
            String message = messageSource.getMessage("strategy.suggestion.unavailable", null, locale);
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.error(message));
            return WebConstants.REDIRECT_STRATEGIES_PREFIX + strategyId;
        }

        final String jobId;
        try {
            jobId = suggestTickerJobService.submitSuggestionJob(strategyId);
        } catch (SuggestJobRejectedException ex) {
            String message = messageSource.getMessage("strategy.suggestion.job.busy", null, locale);
            redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                    UiNotification.warning(message));
            return WebConstants.REDIRECT_STRATEGIES_PREFIX + strategyId;
        }

        String message = messageSource.getMessage("strategy.suggestion.job.started", null, locale);
        redirectAttributes.addFlashAttribute(WebConstants.UI_NOTIFICATION_KEY,
                UiNotification.success(message));
        return WebConstants.REDIRECT_STRATEGIES_PREFIX + strategyId + WebConstants.PARAM_SUGGEST_JOB_ID + jobId;
    }

    @GetMapping("/suggest-jobs/{jobId}")
    @ResponseBody
    public ResponseEntity<SuggestJobStatusDTO> getSuggestJobStatus(@PathVariable("jobId") String jobId) {
        Locale locale = LocaleContextHolder.getLocale();
        return suggestTickerJobService.getJob(jobId)
                .map(job -> ResponseEntity.ok(SuggestJobStatusDTO.from(job, resolveJobMessage(job, locale))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private Optional<String> resolveSuggestJobId(String jobIdParam, long strategyId) {
        if (jobIdParam != null && !jobIdParam.isBlank()
                && suggestTickerJobService.getJob(jobIdParam).map(SuggestTickerJob::isActive).orElse(false)) {
            return Optional.of(jobIdParam);
        }
        return suggestTickerJobService.findActiveJobIdByStrategyId(strategyId);
    }

    private String resolveJobMessage(SuggestTickerJob job, Locale locale) {
        return switch (job.getStatus()) {
            case DONE -> messageSource.getMessage("strategy.suggestion.job.done", null, locale);
            case FAILED -> messageSource.getMessage("strategy.suggestion.job.failed", null, locale);
            default -> null;
        };
    }

    @PostMapping("/{id:\\d+}/add-suggested-tickers")
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
        model.addAttribute(WebConstants.ATTR_SNAPSHOT_WARNINGS, response.getWarnings() == null ? List.of() : response.getWarnings());
        model.addAttribute(WebConstants.ATTR_SUGGESTED_AT, response.getSuggestedAt());
    }
}
