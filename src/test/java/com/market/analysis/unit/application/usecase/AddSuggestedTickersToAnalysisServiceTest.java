package com.market.analysis.unit.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.application.usecase.AddSuggestedTickersToAnalysisService;
import com.market.analysis.application.usecase.StockDeterministicAnalysisPipeline;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StockOrigin;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.SuggestedTickerSnapshot;
import com.market.analysis.domain.model.SuggestionSnapshot;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StockProviderPort;
import com.market.analysis.domain.port.out.StrategyRepository;
import com.market.analysis.domain.port.out.SuggestionSnapshotRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddSuggestedTickersToAnalysisService Unit Tests")
class AddSuggestedTickersToAnalysisServiceTest {

    @Mock
    private SuggestionSnapshotRepository suggestionSnapshotRepository;

    @Mock
    private StrategyRepository strategyRepository;

    @Mock
    private StockDataRepository stockDataRepository;

    @Mock
    private StockDeterministicAnalysisPipeline stockDeterministicAnalysisPipeline;

    @Mock
    private StockProviderPort stockProviderPort;

    @InjectMocks
    private AddSuggestedTickersToAnalysisService service;

    @Test
    @DisplayName("Should add APTO tickers from latest snapshot")
    void shouldAddAptoTickersFromLatestSnapshot() {
        Long strategyId = 1L;
        Instant suggestedAt = Instant.parse("2026-04-20T08:00:00Z");
        SuggestionSnapshot snapshot = SuggestionSnapshot.builder()
                .strategyId(strategyId)
                .suggestedAt(suggestedAt)
                .suggestedTickers(List.of(
                        SuggestedTickerSnapshot.builder()
                                .ticker("aapl")
                                .strategyId(strategyId)
                                .suggestedAt(suggestedAt)
                                .suitabilityStatus("APTO")
                                .traceability(List.of("Cumple reglas"))
                                .build(),
                        SuggestedTickerSnapshot.builder()
                                .ticker("TSLA")
                                .strategyId(strategyId)
                                .suggestedAt(suggestedAt)
                                .suitabilityStatus("NO_APTO")
                                .traceability(List.of("No cumple"))
                                .build()))
                .build();
        Strategy strategy = Strategy.builder().id(strategyId).name("Momentum").build();

        when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(strategy));
        when(suggestionSnapshotRepository.findLatestByStrategyId(strategyId)).thenReturn(Optional.of(snapshot));
        when(stockDeterministicAnalysisPipeline.analyzeAndPersist("AAPL", strategy, StockOrigin.STRATEGY_SUGGESTION))
            .thenReturn(Stock.builder().id(10L).ticker("AAPL").build());

        int added = service.addFromLatestSnapshot(strategyId);

        assertThat(added).isEqualTo(1);
        verify(stockDeterministicAnalysisPipeline, times(1)).analyzeAndPersist(
            "AAPL",
            strategy,
            StockOrigin.STRATEGY_SUGGESTION);
        verify(stockProviderPort, never()).getQuote(any());
    }

    @Test
    @DisplayName("Should return zero when latest snapshot is missing")
    void shouldReturnZeroWhenSnapshotMissing() {
        Long strategyId = 1L;
        when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(Strategy.builder().id(strategyId).name("S").build()));
        when(suggestionSnapshotRepository.findLatestByStrategyId(strategyId)).thenReturn(Optional.empty());

        int added = service.addFromLatestSnapshot(strategyId);

        assertThat(added).isZero();
        verify(stockDeterministicAnalysisPipeline, never()).analyzeAndPersist(any(), any(), any());
    }

    @Test
    @DisplayName("Should reject null strategy id")
    void shouldRejectNullStrategyId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.addFromLatestSnapshot(null));
        assertThat(ex.getMessage()).isEqualTo("Strategy ID is required");
    }

    @Test
    @DisplayName("Should fail when snapshot is missing suggested timestamp")
    void shouldFailWhenSnapshotMissingSuggestedAt() {
        Long strategyId = 1L;
        when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(Strategy.builder().id(strategyId).name("S").build()));
        when(suggestionSnapshotRepository.findLatestByStrategyId(strategyId))
                .thenReturn(Optional.of(SuggestionSnapshot.builder()
                        .strategyId(strategyId)
                        .suggestedTickers(List.of())
                        .build()));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.addFromLatestSnapshot(strategyId));
        assertThat(ex.getMessage()).isEqualTo("Latest suggestion snapshot is missing suggestedAt");
    }

    @Test
    @DisplayName("Should refresh snapshot-origin tickers in batch")
    void shouldRefreshSnapshotOriginTickersInBatch() {
        Long strategyId = 1L;
        Stock snapshotStock = Stock.builder()
                .id(10L)
                .ticker("AAPL")
                .strategyId(strategyId)
            .origin(StockOrigin.STRATEGY_SUGGESTION)
                .build();
        Stock externalStock = Stock.builder()
                .id(11L)
                .ticker("MSFT")
                .strategyId(strategyId)
                .origin(StockOrigin.EXTERNAL_PROVIDER)
                .build();
        Stock quote = Stock.builder()
                .ticker("AAPL")
                .currentPrice(java.math.BigDecimal.valueOf(185.1))
                .openPrice(java.math.BigDecimal.valueOf(183.9))
                .highOfDay(java.math.BigDecimal.valueOf(186.0))
                .lowOfDay(java.math.BigDecimal.valueOf(183.2))
                .previousClose(java.math.BigDecimal.valueOf(182.7))
                .build();

        when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(Strategy.builder().id(strategyId).name("S").build()));
        when(stockDataRepository.findAllByStrategyId(strategyId)).thenReturn(List.of(snapshotStock, externalStock));
        when(stockProviderPort.getQuote("AAPL")).thenReturn(quote);

        int refreshed = service.refreshFromSuggestionSnapshot(strategyId);

        assertThat(refreshed).isEqualTo(1);
        verify(stockProviderPort, times(1)).getQuote("AAPL");
        verify(stockDataRepository, times(1)).save(snapshotStock);
    }

    @Test
    @DisplayName("Should return zero refreshes when there are no snapshot-origin tickers")
    void shouldReturnZeroRefreshesWhenNoSnapshotOriginTickers() {
        Long strategyId = 1L;
        Stock externalStock = Stock.builder()
                .id(11L)
                .ticker("MSFT")
                .strategyId(strategyId)
                .origin(StockOrigin.EXTERNAL_PROVIDER)
                .build();

        when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(Strategy.builder().id(strategyId).name("S").build()));
        when(stockDataRepository.findAllByStrategyId(strategyId)).thenReturn(List.of(externalStock));

        int refreshed = service.refreshFromSuggestionSnapshot(strategyId);

        assertThat(refreshed).isZero();
        verify(stockProviderPort, never()).getQuote(any());
    }
}
