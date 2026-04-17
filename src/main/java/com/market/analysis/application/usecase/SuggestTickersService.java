package com.market.analysis.application.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class SuggestTickersService implements SuggestTickersUseCase {

    private static final int DEFAULT_MAX_CANDIDATES = 3;
    private static final String EMPTY_FILTERS_WARNING =
            "No Finviz filters could be generated for this strategy.";
    private static final String EVALUATOR_EMPTY_TRACE_WARNING =
            "Deterministic evaluation did not return traceability details.";
    private static final String FINVIZ_DEGRADED_WARNING =
            "Finviz no está disponible temporalmente; la sugerencia se ha degradado sin resultados.";

    private final StrategyRepository strategyRepository;
    private final FinvizFilterMapper finvizFilterMapper;
    private final FinvizScreenerPort finvizScreenerPort;
    private final DeterministicTickerEvaluator deterministicTickerEvaluator;

    @Override
    public SuggestTickersResponseDTO suggestTickers(SuggestTickersRequestDTO request) {
        validateRequest(request);

        int maxCandidates = resolveMaxCandidates(request.getMaxCandidates());
        Strategy strategy = strategyRepository.findById(request.getStrategyId())
                .orElseThrow(() -> new IllegalArgumentException("Strategy not found with id: " + request.getStrategyId()));

        FinvizFilterMappingResult mappingResult = finvizFilterMapper.map(strategy);
        List<String> unmappableRules = mappingResult != null ? mappingResult.getUnmappableRules() : List.of();
        List<String> warnings = new ArrayList<>(mappingResult != null ? mappingResult.getWarnings() : List.of());
        String appliedFilters = mappingResult != null ? mappingResult.getFilters() : null;
        log.info(
                "suggest_tickers_mapping strategyId={} appliedFilters={} unmappableRulesCount={} warningsCount={}",
                request.getStrategyId(),
                appliedFilters,
                unmappableRules.size(),
                warnings.size());

        if (appliedFilters == null || appliedFilters.isBlank()) {
            warnings.add(EMPTY_FILTERS_WARNING);
            log.info("suggest_tickers_empty_filters strategyId={}", request.getStrategyId());
            return buildResponse(request.getStrategyId(), appliedFilters, unmappableRules, warnings, List.of());
        }

        List<String> candidates;
        try {
            candidates = finvizScreenerPort.findTickers(appliedFilters, maxCandidates);
        } catch (RuntimeException ex) {
            warnings.add(FINVIZ_DEGRADED_WARNING);
            log.warn("suggest_tickers_finviz_degraded strategyId={} appliedFilters={} message={}",
                    request.getStrategyId(),
                    appliedFilters,
                    ex.getMessage());
            return buildResponse(request.getStrategyId(), appliedFilters, unmappableRules, warnings, List.of());
        }

        log.info("suggest_tickers_candidates strategyId={} candidatesCount={}",
                request.getStrategyId(),
                Optional.ofNullable(candidates).orElse(List.of()).size());
        List<SuggestedTickerDTO> suggestedTickers = Optional.ofNullable(candidates).orElse(List.of()).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(ticker -> !ticker.isEmpty())
                .distinct()
                .map(ticker -> classifyTicker(ticker, strategy))
                .toList();

        return buildResponse(request.getStrategyId(), appliedFilters, unmappableRules, warnings, suggestedTickers);
    }

    private SuggestedTickerDTO classifyTicker(String ticker, Strategy strategy) {
        DeterministicTickerEvaluation evaluation = deterministicTickerEvaluator.evaluate(ticker, strategy);
        List<String> traceability = (evaluation != null && !evaluation.getTraceability().isEmpty())
                ? new ArrayList<>(evaluation.getTraceability())
                : new ArrayList<>(List.of(EVALUATOR_EMPTY_TRACE_WARNING));

        boolean suitable = evaluation != null && evaluation.isSuitable();
        if (!suitable) {
            log.info("suggest_ticker_discarded ticker={} traceability={}", ticker, traceability);
        }
        return SuggestedTickerDTO.builder()
                .ticker(ticker)
                .suitabilityStatus(suitable ? TickerSuitabilityStatus.APTO : TickerSuitabilityStatus.NO_APTO)
                .traceability(traceability)
                .build();
    }

        private SuggestTickersResponseDTO buildResponse(Long strategyId, String appliedFilters,
            List<String> unmappableRules, List<String> warnings, List<SuggestedTickerDTO> suggestedTickers) {
        return SuggestTickersResponseDTO.builder()
                .strategyId(strategyId)
                .appliedFilters(appliedFilters)
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

    private int resolveMaxCandidates(Integer maxCandidates) {
        return maxCandidates != null && maxCandidates > 0 ? maxCandidates : DEFAULT_MAX_CANDIDATES;
    }
}
