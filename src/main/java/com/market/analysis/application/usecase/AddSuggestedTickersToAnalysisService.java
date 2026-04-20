package com.market.analysis.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StockOrigin;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.domain.model.SuggestedTickerSnapshot;
import com.market.analysis.domain.model.SuggestionSnapshot;
import com.market.analysis.domain.port.in.AddSuggestedTickersToAnalysisUseCase;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StockProviderPort;
import com.market.analysis.domain.port.out.StrategyEvaluationRepository;
import com.market.analysis.domain.port.out.StrategyRepository;
import com.market.analysis.domain.port.out.SuggestionSnapshotRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddSuggestedTickersToAnalysisService implements AddSuggestedTickersToAnalysisUseCase {

    private static final String APTO = "APTO";
    private static final String OFFLINE_SUMMARY = "Alta offline desde snapshot de sugerencias.";

    private final SuggestionSnapshotRepository suggestionSnapshotRepository;
    private final StrategyRepository strategyRepository;
    private final StockDataRepository stockDataRepository;
    private final StrategyEvaluationRepository strategyEvaluationRepository;
    private final StockProviderPort stockProviderPort;

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
        Set<String> aptTickers = new LinkedHashSet<>();

        for (SuggestedTickerSnapshot tickerSnapshot : snapshot.getSuggestedTickers()) {
            if (tickerSnapshot == null || tickerSnapshot.getTicker() == null || tickerSnapshot.getTicker().isBlank()) {
                continue;
            }
            if (!APTO.equalsIgnoreCase(tickerSnapshot.getSuitabilityStatus())) {
                continue;
            }
            aptTickers.add(tickerSnapshot.getTicker().trim().toUpperCase(Locale.ROOT));
        }

        int addedCount = 0;
        for (String ticker : aptTickers) {
            Stock stock = stockDataRepository.save(Stock.builder()
                    .ticker(ticker)
                    .strategyId(strategyId)
                    .origin(StockOrigin.SUGGESTION_SNAPSHOT)
                    .lastUpdated(evaluatedAt)
                    .build());

            StrategyEvaluation evaluation = StrategyEvaluation.builder()
                    .ticker(ticker)
                    .strategyId(strategyId)
                    .strategyName(strategy.getName())
                    .compliant(true)
                    .complianceRate(BigDecimal.valueOf(100))
                    .summary(buildOfflineSummary(snapshot, ticker))
                    .evaluatedAt(evaluatedAt)
                    .priceAtEvaluation(stock.getCurrentPrice())
                    .isLatest(true)
                    .build();

            strategyEvaluationRepository.save(evaluation, stock);
            addedCount++;
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

        int refreshed = 0;
        for (Stock existingStock : stockDataRepository.findAllByStrategyId(strategyId)) {
            if (existingStock == null || existingStock.getTicker() == null || existingStock.getTicker().isBlank()) {
                continue;
            }
            if (existingStock.getOrigin() != StockOrigin.SUGGESTION_SNAPSHOT) {
                continue;
            }

            Stock quote = stockProviderPort.getQuote(existingStock.getTicker());
            if (quote == null) {
                continue;
            }

            existingStock.setCurrentPrice(quote.getCurrentPrice());
            existingStock.setOpenPrice(quote.getOpenPrice());
            existingStock.setHighOfDay(quote.getHighOfDay());
            existingStock.setLowOfDay(quote.getLowOfDay());
            existingStock.setPreviousClose(quote.getPreviousClose());
            existingStock.setLastUpdated(Instant.now());
            stockDataRepository.save(existingStock);
            refreshed++;
        }

        return refreshed;
    }

    private String buildOfflineSummary(SuggestionSnapshot snapshot, String ticker) {
        SuggestedTickerSnapshot matchedTicker = findMatchedTicker(snapshot, ticker).orElse(null);
        if (matchedTicker == null || matchedTicker.getTraceability() == null || matchedTicker.getTraceability().isEmpty()) {
            return OFFLINE_SUMMARY;
        }

        return matchedTicker.getTraceability().stream()
                .filter(line -> line != null && !line.isBlank())
                .findFirst()
                .orElse(OFFLINE_SUMMARY);
    }

    private Optional<SuggestedTickerSnapshot> findMatchedTicker(SuggestionSnapshot snapshot, String ticker) {
        return snapshot.getSuggestedTickers().stream()
                .filter(item -> item != null && ticker.equalsIgnoreCase(item.getTicker()))
                .findFirst();
    }
}
