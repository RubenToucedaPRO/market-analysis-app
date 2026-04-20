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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.application.usecase.AddSuggestedTickersToAnalysisService;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.SuggestedTickerSnapshot;
import com.market.analysis.domain.model.SuggestionSnapshot;
import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StrategyEvaluationRepository;
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
    private StrategyEvaluationRepository strategyEvaluationRepository;

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
        when(stockDataRepository.save(any())).thenAnswer(invocation -> {
            Stock stock = invocation.getArgument(0, Stock.class);
            stock.setId(10L);
            return stock;
        });

        int added = service.addFromLatestSnapshot(strategyId);

        assertThat(added).isEqualTo(1);
        ArgumentCaptor<Stock> stockCaptor = ArgumentCaptor.forClass(Stock.class);
        verify(stockDataRepository, times(1)).save(stockCaptor.capture());
        assertThat(stockCaptor.getValue().getTicker()).isEqualTo("AAPL");
        assertThat(stockCaptor.getValue().getStrategyId()).isEqualTo(strategyId);
        assertThat(stockCaptor.getValue().getLastUpdated()).isEqualTo(suggestedAt);
        verify(strategyEvaluationRepository, times(1)).save(any(StrategyEvaluation.class), any(Stock.class));
    }

    @Test
    @DisplayName("Should return zero when latest snapshot is missing")
    void shouldReturnZeroWhenSnapshotMissing() {
        Long strategyId = 1L;
        when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(Strategy.builder().id(strategyId).name("S").build()));
        when(suggestionSnapshotRepository.findLatestByStrategyId(strategyId)).thenReturn(Optional.empty());

        int added = service.addFromLatestSnapshot(strategyId);

        assertThat(added).isZero();
        verify(stockDataRepository, never()).save(any());
        verify(strategyEvaluationRepository, never()).save(any(), any());
    }

    @Test
    @DisplayName("Should reject null strategy id")
    void shouldRejectNullStrategyId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.addFromLatestSnapshot(null));
        assertThat(ex.getMessage()).isEqualTo("Strategy ID is required");
    }
}
