package com.market.analysis.application.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.market.analysis.application.dto.FinvizExecutionMode;
import com.market.analysis.application.dto.SuggestTickersRequestDTO;
import com.market.analysis.application.dto.SuggestTickersResponseDTO;
import com.market.analysis.application.dto.SuggestedTickerDTO;
import com.market.analysis.application.dto.TickerSuitabilityStatus;
import com.market.analysis.domain.model.FinvizFilterMappingResult;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.port.in.SuggestTickersUseCase;
import com.market.analysis.domain.port.out.FinvizScreenerPort;
import com.market.analysis.domain.port.out.StrategyRepository;
import com.market.analysis.domain.service.FinvizFilterMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SuggestTickersService implements SuggestTickersUseCase {

    private static final int DEFAULT_MAX_CANDIDATES = 25;
    private static final String STRICT_MODE_BLOCKED_WARNING =
            "Strict mode enabled: execution blocked due to unmappable strategy rules.";
    private static final String EMPTY_FILTERS_WARNING =
            "No Finviz filters could be generated for this strategy.";
    private static final String EVALUATOR_EMPTY_TRACE_WARNING =
            "Deterministic evaluation did not return traceability details.";

    private final StrategyRepository strategyRepository;
    private final FinvizFilterMapper finvizFilterMapper;
    private final FinvizScreenerPort finvizScreenerPort;
    private final DeterministicTickerEvaluator deterministicTickerEvaluator;

    @Override
    public SuggestTickersResponseDTO suggestTickers(SuggestTickersRequestDTO request) {
        validateRequest(request);

        FinvizExecutionMode executionMode = resolveExecutionMode(request.getExecutionMode());
        int maxCandidates = resolveMaxCandidates(request.getMaxCandidates());
        Strategy strategy = strategyRepository.findById(request.getStrategyId())
                .orElseThrow(() -> new IllegalArgumentException("Strategy not found with id: " + request.getStrategyId()));

        FinvizFilterMappingResult mappingResult = finvizFilterMapper.map(strategy);
        List<String> unmappableRules = mappingResult != null ? mappingResult.getUnmappableRules() : List.of();
        List<String> warnings = new ArrayList<>(mappingResult != null ? mappingResult.getWarnings() : List.of());
        String appliedFilters = mappingResult != null ? mappingResult.getFilters() : null;

        if (executionMode == FinvizExecutionMode.STRICT && !unmappableRules.isEmpty()) {
            warnings.add(STRICT_MODE_BLOCKED_WARNING);
            return buildResponse(request.getStrategyId(), appliedFilters, executionMode, unmappableRules, warnings, List.of());
        }

        if (appliedFilters == null || appliedFilters.isBlank()) {
            warnings.add(EMPTY_FILTERS_WARNING);
            return buildResponse(request.getStrategyId(), appliedFilters, executionMode, unmappableRules, warnings, List.of());
        }

        List<String> candidates = finvizScreenerPort.findTickers(appliedFilters, maxCandidates);
        List<SuggestedTickerDTO> suggestedTickers = Optional.ofNullable(candidates).orElse(List.of()).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(ticker -> !ticker.isEmpty())
                .distinct()
                .map(ticker -> classifyTicker(ticker, strategy))
                .toList();

        return buildResponse(request.getStrategyId(), appliedFilters, executionMode, unmappableRules, warnings, suggestedTickers);
    }

    private SuggestedTickerDTO classifyTicker(String ticker, Strategy strategy) {
        DeterministicTickerEvaluation evaluation = deterministicTickerEvaluator.evaluate(ticker, strategy);
        List<String> traceability = (evaluation != null && !evaluation.getTraceability().isEmpty())
                ? new ArrayList<>(evaluation.getTraceability())
                : new ArrayList<>(List.of(EVALUATOR_EMPTY_TRACE_WARNING));

        boolean suitable = evaluation != null && evaluation.isSuitable();
        return SuggestedTickerDTO.builder()
                .ticker(ticker)
                .suitabilityStatus(suitable ? TickerSuitabilityStatus.APTO : TickerSuitabilityStatus.NO_APTO)
                .traceability(traceability)
                .build();
    }

    private SuggestTickersResponseDTO buildResponse(Long strategyId, String appliedFilters, FinvizExecutionMode executionMode,
            List<String> unmappableRules, List<String> warnings, List<SuggestedTickerDTO> suggestedTickers) {
        return SuggestTickersResponseDTO.builder()
                .strategyId(strategyId)
                .appliedFilters(appliedFilters)
                .executionMode(executionMode)
                .unmappableRules(unmappableRules)
                .warnings(warnings)
                .suggestedTickers(suggestedTickers)
                .build();
    }

    private void validateRequest(SuggestTickersRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Suggest tickers request cannot be null");
        }
        if (request.getStrategyId() == null) {
            throw new IllegalArgumentException("Strategy ID is required");
        }
    }

    private FinvizExecutionMode resolveExecutionMode(FinvizExecutionMode mode) {
        return mode != null ? mode : FinvizExecutionMode.TOLERANT;
    }

    private int resolveMaxCandidates(Integer maxCandidates) {
        return maxCandidates != null && maxCandidates > 0 ? maxCandidates : DEFAULT_MAX_CANDIDATES;
    }
}
