package com.market.analysis.application.usecase;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.market.analysis.application.dto.SuggestTickersRequestDTO;
import com.market.analysis.application.dto.SuggestTickersResponseDTO;
import com.market.analysis.application.dto.SuggestedTickerDTO;
import com.market.analysis.application.dto.TickerSuitabilityStatus;
import com.market.analysis.domain.model.FinvizFilterMappingResult;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StockOrigin;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.SuggestedTickerSnapshot;
import com.market.analysis.domain.model.SuggestionSnapshot;
import com.market.analysis.domain.port.in.SuggestTickersUseCase;
import com.market.analysis.domain.port.out.FinvizScreenerPort;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StrategyRepository;
import com.market.analysis.domain.port.out.SuggestedTickerRepository;
import com.market.analysis.domain.port.out.SuggestionSnapshotRepository;
import com.market.analysis.domain.service.FinvizFilterMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class SuggestTickersService implements SuggestTickersUseCase {

    private static final int DEFAULT_MAX_CANDIDATES = 10;
    private static final String EMPTY_FILTERS_WARNING =
            "No Finviz filters could be generated for this strategy.";
    private static final String FINVIZ_DEGRADED_WARNING =
            "Finviz no está disponible temporalmente; la sugerencia se ha degradado sin resultados.";

    private final StrategyRepository strategyRepository;
    private final FinvizFilterMapper finvizFilterMapper;
    private final FinvizScreenerPort finvizScreenerPort;
    private final AnalyzeAndPersistStockService analyzeAndPersistStockService;
    private final SuggestionSnapshotRepository suggestionSnapshotRepository;
    private final SuggestedTickerRepository suggestedTickerRepository;
    private final StockDataRepository stockDataRepository;

    @Override
    public SuggestTickersResponseDTO suggestTickers(SuggestTickersRequestDTO request) {
        validateRequest(request);

        int maxCandidates = resolveMaxCandidates(request.getMaxCandidates());
        Strategy strategy = strategyRepository.findById(request.getStrategyId())
                .orElseThrow(() -> new IllegalArgumentException("Strategy not found with id: " + request.getStrategyId()));

        stockDataRepository.deleteAllByStrategyIdAndOrigin(strategy.getId(), StockOrigin.SUGGESTION_SNAPSHOT);
        suggestionSnapshotRepository.deleteAllByStrategyId(strategy.getId());
        
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
            return buildAndPersistResponse(request.getStrategyId(), appliedFilters, unmappableRules, warnings, List.of());
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
            return buildAndPersistResponse(request.getStrategyId(), appliedFilters, unmappableRules, warnings, List.of());
        }

        log.info("suggest_tickers_candidates strategyId={} candidatesCount={}",
                request.getStrategyId(),
                Optional.ofNullable(candidates).orElse(List.of()).size());

        Set<String> existing = stockDataRepository.findTickerByStrategyId(strategy.getId());
        List<String> filteredCandidates = candidates.stream()
        .filter(ticker -> !existing.contains(ticker))
        .toList();
             
        List<String> validTickers = analyzeAndPersistStockService.validateAndUpdateCompanyProfiles(filteredCandidates);
        List<SuggestedTickerDTO> suggestedTickers = Optional.ofNullable(validTickers).orElse(List.of()).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(ticker -> !ticker.isEmpty())
                .distinct()
                .map(ticker -> classifyAndPersistTicker(ticker, strategy))
                .toList();

        return buildAndPersistResponse(request.getStrategyId(), appliedFilters, unmappableRules, warnings, suggestedTickers);
    }

    @Override
    public Optional<SuggestTickersResponseDTO> getLatestSuggestionSnapshot(Long strategyId) {
        if (strategyId == null) {
            return Optional.empty();
        }
        return suggestionSnapshotRepository.findLatestByStrategyId(strategyId).map(this::toResponse);
    }

    @Override
    public int convertSuggestedTickersToAnalysis(long strategyId) {
        List<Stock> stocks = stockDataRepository.findAllByStrategyId(strategyId);
        int switched = 0;
        for (Stock stock : stocks) {
            if (stock != null && stock.getOrigin() != null
                    && (stock.getOrigin() == StockOrigin.SUGGESTION_SNAPSHOT
                            || stock.getOrigin() == StockOrigin.STRATEGY_SUGGESTION) && stock.getStrategyEvaluation().isCompliant()) {
                stock.setOrigin(StockOrigin.ANALYSIS);
                stockDataRepository.save(stock);
                suggestedTickerRepository.deleteByStrategyIdAndTicker(strategyId, stock.getTicker());
                switched++;
            }
        }
        return switched;
    }

    private SuggestedTickerDTO classifyAndPersistTicker(String ticker, Strategy strategy) {
    Stock persistedStock = analyzeAndPersistStockService.analyzeAndPersist(ticker, strategy,
                StockOrigin.SUGGESTION_SNAPSHOT);
        boolean suitable = persistedStock != null && persistedStock.getStrategyEvaluation() != null
                && persistedStock.getStrategyEvaluation().isCompliant();

        List<String> traceability = buildTraceability(persistedStock, suitable);
        SuggestedTickerDTO dto = SuggestedTickerDTO.builder()
                .ticker(ticker)
                .strategyId(strategy.getId())
                .suitabilityStatus(suitable ? TickerSuitabilityStatus.APTO : TickerSuitabilityStatus.NO_APTO)
                .traceability(traceability)
                .build();

        if (!suitable) {
            log.info("suggest_ticker_discarded ticker={} traceability={}", ticker, traceability);
        }
        return dto;
    }

    private List<String> buildTraceability(Stock persistedStock, boolean suitable) {
        if (persistedStock == null || persistedStock.getStrategyEvaluation() == null) {
            return List.of("No se pudo generar una evaluación determinista válida.");
        }

        String summary = persistedStock.getStrategyEvaluation().getSummary();
        if (summary == null || summary.isBlank()) {
            return List.of(suitable ? "Ticker apto" : "Ticker no apto");
        }
        return List.of(summary);
    }

    private SuggestTickersResponseDTO buildAndPersistResponse(Long strategyId, String appliedFilters,
            List<String> unmappableRules, List<String> warnings, List<SuggestedTickerDTO> suggestedTickers) {
        SuggestTickersResponseDTO response = buildResponse(strategyId, appliedFilters, unmappableRules, warnings, suggestedTickers,
                Instant.now());
        suggestionSnapshotRepository.save(toSnapshot(response));
        return response;
    }

    private SuggestTickersResponseDTO buildResponse(Long strategyId, String appliedFilters,
            List<String> unmappableRules, List<String> warnings, List<SuggestedTickerDTO> suggestedTickers, Instant suggestedAt) {
        return SuggestTickersResponseDTO.builder()
                .strategyId(strategyId)
                .appliedFilters(appliedFilters)
                .suggestedAt(suggestedAt)
                .unmappableRules(unmappableRules)
                .warnings(warnings)
                .suggestedTickers(suggestedTickers)
                .build();
    }

    private SuggestionSnapshot toSnapshot(SuggestTickersResponseDTO response) {
        return SuggestionSnapshot.builder()
                .strategyId(response.getStrategyId())
                .suggestedAt(response.getSuggestedAt())
                .appliedFilters(response.getAppliedFilters())
                .unmappableRules(response.getUnmappableRules())
                .warnings(response.getWarnings())
                .suggestedTickers(Optional.ofNullable(response.getSuggestedTickers()).orElse(List.of()).stream()
                        .map(dto -> toSnapshotTicker(dto, response.getStrategyId(), response.getSuggestedAt()))
                        .toList())
                .build();
    }

    private SuggestedTickerSnapshot toSnapshotTicker(SuggestedTickerDTO dto, Long strategyId, Instant suggestedAt) {
        boolean suitable = TickerSuitabilityStatus.APTO.equals(dto.getSuitabilityStatus());
        List<String> deterministicMetrics =
                suitable && dto.getDeterministicMetrics() != null ? dto.getDeterministicMetrics() : List.of();
        return SuggestedTickerSnapshot.builder()
                .ticker(dto.getTicker())
                .strategyId(strategyId)
                .suggestedAt(suggestedAt)
                .suitabilityStatus(dto.getSuitabilityStatus() == null ? null : dto.getSuitabilityStatus().name())
                .deterministicMetrics(deterministicMetrics)
                .traceability(dto.getTraceability())
                .build();
    }

    private SuggestTickersResponseDTO toResponse(SuggestionSnapshot snapshot) {
        return buildResponse(
                snapshot.getStrategyId(),
                snapshot.getAppliedFilters(),
                snapshot.getUnmappableRules(),
                snapshot.getWarnings(),
                snapshot.getSuggestedTickers().stream()
                        .map(this::toSuggestedTickerDTO)
                        .toList(),
                snapshot.getSuggestedAt());
    }

    private SuggestedTickerDTO toSuggestedTickerDTO(SuggestedTickerSnapshot snapshot) {
        TickerSuitabilityStatus suitabilityStatus = TickerSuitabilityStatus.NO_APTO;
        if (snapshot.getSuitabilityStatus() != null) {
            try {
                suitabilityStatus = TickerSuitabilityStatus.valueOf(snapshot.getSuitabilityStatus());
            } catch (IllegalArgumentException ex) {
                log.warn("Unknown suitability status '{}' for ticker '{}'", snapshot.getSuitabilityStatus(),
                        snapshot.getTicker());
            }
        }
        return SuggestedTickerDTO.builder()
                .ticker(snapshot.getTicker())
                .strategyId(snapshot.getStrategyId())
                .suggestedAt(snapshot.getSuggestedAt())
                .suitabilityStatus(suitabilityStatus)
                .deterministicMetrics(snapshot.getDeterministicMetrics())
                .traceability(snapshot.getTraceability())
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
