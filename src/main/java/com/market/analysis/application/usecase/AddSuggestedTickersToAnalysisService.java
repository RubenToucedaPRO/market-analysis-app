package com.market.analysis.application.usecase;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StockOrigin;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.SuggestedTickerSnapshot;
import com.market.analysis.domain.model.SuggestionSnapshot;
import com.market.analysis.domain.port.in.AddSuggestedTickersToAnalysisUseCase;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StockProviderPort;
import com.market.analysis.domain.port.out.StrategyRepository;
import com.market.analysis.domain.port.out.SuggestionSnapshotRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddSuggestedTickersToAnalysisService implements AddSuggestedTickersToAnalysisUseCase {

    private static final String APTO = "APTO";

    private final SuggestionSnapshotRepository suggestionSnapshotRepository;
    private final StrategyRepository strategyRepository;
    private final StockDataRepository stockDataRepository;
    private final StockProviderPort stockProviderPort;
    private final StockDeterministicAnalysisPipeline stockDeterministicAnalysisPipeline;

    @Override
    public int addFromLatestSnapshot(Long strategyId) {
        if (strategyId == null) {
            throw new IllegalArgumentException("Strategy ID is required");
        }

        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new IllegalArgumentException("Strategy not found with id: " + strategyId));

        Optional<SuggestionSnapshot> latestSnapshot = suggestionSnapshotRepository.findLatestByStrategyId(strategyId);
        if (latestSnapshot.isEmpty()) {
            return 0;
        }

        SuggestionSnapshot snapshot = latestSnapshot.get();
        Instant evaluatedAt = snapshot.getSuggestedAt();
        if (evaluatedAt == null) {
            throw new IllegalStateException("Latest suggestion snapshot is missing suggestedAt");
        }
        Set<String> aptTickers = Optional.ofNullable(snapshot.getSuggestedTickers())
            .orElse(java.util.List.of())
                .stream()
                .filter(this::isValidSnapshotTicker)
                .filter(this::isAptoTicker)
                .map(this::normalizeTicker)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        int addedCount = 0;
        for (String ticker : aptTickers) {
            Stock persistedStock = stockDeterministicAnalysisPipeline.analyzeAndPersist(
                ticker,
                strategy,
                StockOrigin.STRATEGY_SUGGESTION);
            if (persistedStock != null) {
            addedCount++;
            }
        }

        return addedCount;
    }

    @Override
    public int refreshFromSuggestionSnapshot(Long strategyId) {
        if (strategyId == null) {
            throw new IllegalArgumentException("Strategy ID is required");
        }

        strategyRepository.findById(strategyId)
                .orElseThrow(() -> new IllegalArgumentException("Strategy not found with id: " + strategyId));

        return (int) stockDataRepository.findAllByStrategyId(strategyId).stream()
                .filter(this::isValidRefreshCandidate)
                .map(this::refreshSnapshotStock)
                .filter(Boolean.TRUE::equals)
                .count();
    }

    private boolean isValidSnapshotTicker(SuggestedTickerSnapshot tickerSnapshot) {
        return tickerSnapshot != null && tickerSnapshot.getTicker() != null && !tickerSnapshot.getTicker().isBlank();
    }

    private boolean isAptoTicker(SuggestedTickerSnapshot tickerSnapshot) {
        return APTO.equalsIgnoreCase(tickerSnapshot.getSuitabilityStatus());
    }

    private String normalizeTicker(SuggestedTickerSnapshot tickerSnapshot) {
        return tickerSnapshot.getTicker().trim().toUpperCase(Locale.ROOT);
    }

    private boolean isValidRefreshCandidate(Stock existingStock) {
        return existingStock != null
                && existingStock.getTicker() != null
                && !existingStock.getTicker().isBlank()
                && isRefreshableSnapshotOrigin(existingStock.getOrigin());
    }

    private boolean refreshSnapshotStock(Stock existingStock) {
        Stock quote = stockProviderPort.getQuote(existingStock.getTicker());
        if (quote == null) {
            return false;
        }

        existingStock.setCurrentPrice(quote.getCurrentPrice());
        existingStock.setOpenPrice(quote.getOpenPrice());
        existingStock.setHighOfDay(quote.getHighOfDay());
        existingStock.setLowOfDay(quote.getLowOfDay());
        existingStock.setPreviousClose(quote.getPreviousClose());
        existingStock.setLastUpdated(Instant.now());
        stockDataRepository.save(existingStock);
        return true;
    }

    private boolean isRefreshableSnapshotOrigin(StockOrigin origin) {
        return origin == StockOrigin.SUGGESTION_SNAPSHOT || origin == StockOrigin.STRATEGY_SUGGESTION;
    }

}
