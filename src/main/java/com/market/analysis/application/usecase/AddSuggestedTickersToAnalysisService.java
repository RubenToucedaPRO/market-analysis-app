package com.market.analysis.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.domain.model.SuggestedTickerSnapshot;
import com.market.analysis.domain.model.SuggestionSnapshot;
import com.market.analysis.domain.port.in.AddSuggestedTickersToAnalysisUseCase;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StrategyEvaluationRepository;
import com.market.analysis.domain.port.out.StrategyRepository;
import com.market.analysis.domain.port.out.SuggestionSnapshotRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddSuggestedTickersToAnalysisService implements AddSuggestedTickersToAnalysisUseCase {

    private static final String APTO = "APTO";

    private final SuggestionSnapshotRepository suggestionSnapshotRepository;
    private final StrategyRepository strategyRepository;
    private final StockDataRepository stockDataRepository;
    private final StrategyEvaluationRepository strategyEvaluationRepository;

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
        Instant evaluatedAt = snapshot.getSuggestedAt() != null ? snapshot.getSuggestedAt() : Instant.now();
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

    private String buildOfflineSummary(SuggestionSnapshot snapshot, String ticker) {
        SuggestedTickerSnapshot matchedTicker = findMatchedTicker(snapshot, ticker).orElse(null);
        if (matchedTicker == null || matchedTicker.getTraceability() == null || matchedTicker.getTraceability().isEmpty()) {
            return "Alta offline desde snapshot de sugerencias.";
        }

        return matchedTicker.getTraceability().stream()
                .filter(line -> line != null && !line.isBlank())
                .findFirst()
                .orElse("Alta offline desde snapshot de sugerencias.");
    }

    private Optional<SuggestedTickerSnapshot> findMatchedTicker(SuggestionSnapshot snapshot, String ticker) {
        return snapshot.getSuggestedTickers().stream()
                .filter(item -> item != null && ticker.equalsIgnoreCase(item.getTicker()))
                .findFirst();
    }
}
