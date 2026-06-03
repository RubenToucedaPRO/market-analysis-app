package com.market.analysis.unit.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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

import com.market.analysis.application.dto.SuggestTickersRequestDTO;
import com.market.analysis.application.dto.SuggestTickersResponseDTO;
import com.market.analysis.application.dto.TickerSuitabilityStatus;
import com.market.analysis.application.usecase.SuggestTickersService;
import com.market.analysis.domain.model.FinvizFilterMappingResult;
import com.market.analysis.domain.model.ObjectiveType;
import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StockOrigin;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.domain.model.StrategyObjective;
import com.market.analysis.domain.model.SuggestionSnapshot;
import com.market.analysis.domain.port.out.FinvizScreenerPort;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StrategyRepository;
import com.market.analysis.domain.port.out.SuggestionSnapshotRepository;
import com.market.analysis.domain.service.FinvizFilterMapper;

@DisplayName("SuggestTickersService Unit Tests")
@ExtendWith(MockitoExtension.class)
class SuggestTickersServiceTest {

    @Mock
    private StrategyRepository strategyRepository;

    @Mock
    private FinvizFilterMapper finvizFilterMapper;

    @Mock
    private FinvizScreenerPort finvizScreenerPort;

    @Mock
    private StockDataRepository stockDataRepository;

    @Mock
    private com.market.analysis.application.usecase.AnalyzeAndPersistStockService analyzeAndPersistStockService;

    @Mock
    private SuggestionSnapshotRepository suggestionSnapshotRepository;

    @InjectMocks
    private SuggestTickersService suggestTickersService;

    @Test
    @DisplayName("Should classify APTO and NO_APTO with traceability")
    void shouldClassifyTickersInTolerantMode() {
        Strategy strategy = buildStrategy(10L);
        SuggestTickersRequestDTO request = SuggestTickersRequestDTO.builder()
                .strategyId(10L)
                .maxCandidates(5)
                .build();

        when(strategyRepository.findById(10L)).thenReturn(Optional.of(strategy));
        when(finvizFilterMapper.map(strategy)).thenReturn(FinvizFilterMappingResult.builder()
                .filters("ta_sma20_pa")
                .unmappableRules(List.of("MACD"))
                .warnings(List.of("Partial mapping used"))
                .build());
        when(finvizScreenerPort.findTickers("ta_sma20_pa", 5)).thenReturn(List.of("AAPL", "TSLA"));
        when(analyzeAndPersistStockService.validateAndUpdateCompanyProfiles(List.of("AAPL", "TSLA")))
                .thenReturn(List.of("AAPL", "TSLA"));
        when(analyzeAndPersistStockService.analyzeAndPersist("AAPL", strategy, StockOrigin.SUGGESTION_SNAPSHOT))
                .thenReturn(buildStock("AAPL", true, "All deterministic rules passed"));
        when(analyzeAndPersistStockService.analyzeAndPersist("TSLA", strategy, StockOrigin.SUGGESTION_SNAPSHOT))
                .thenReturn(buildStock("TSLA", false, "Rule RSI failed"));

        SuggestTickersResponseDTO result = suggestTickersService.suggestTickers(request);

        assertThat(result.getUnmappableRules()).containsExactly("MACD");
        assertThat(result.getWarnings()).containsExactly("Partial mapping used");
        assertThat(result.getSuggestedTickers()).hasSize(2);
        assertThat(result.getSuggestedAt()).isNotNull();
        assertThat(result.getSuggestedTickers().get(0).getSuitabilityStatus()).isEqualTo(TickerSuitabilityStatus.APTO);
        assertThat(result.getSuggestedTickers().get(1).getSuitabilityStatus()).isEqualTo(TickerSuitabilityStatus.NO_APTO);
        assertThat(result.getSuggestedTickers().get(1).getTraceability()).containsExactly("Rule RSI failed");

        ArgumentCaptor<SuggestionSnapshot> snapshotCaptor = ArgumentCaptor.forClass(SuggestionSnapshot.class);
        verify(suggestionSnapshotRepository).save(snapshotCaptor.capture());
        SuggestionSnapshot persistedSnapshot = snapshotCaptor.getValue();
        assertThat(persistedSnapshot.getSuggestedTickers()).hasSize(2);
        assertThat(persistedSnapshot.getSuggestedTickers().get(0).getStrategyId()).isEqualTo(10L);
        assertThat(persistedSnapshot.getSuggestedTickers().get(0).getSuggestedAt()).isEqualTo(result.getSuggestedAt());
        assertThat(persistedSnapshot.getSuggestedTickers().get(1).getStrategyId()).isNull();
        assertThat(persistedSnapshot.getSuggestedTickers().get(1).getSuggestedAt()).isNull();
        assertThat(persistedSnapshot.getSuggestedTickers().get(1).getDeterministicMetrics()).isEmpty();
        verify(analyzeAndPersistStockService).analyzeAndPersist("AAPL", strategy, StockOrigin.SUGGESTION_SNAPSHOT);
        verify(analyzeAndPersistStockService).analyzeAndPersist("TSLA", strategy, StockOrigin.SUGGESTION_SNAPSHOT);
    }

    @Test
    @DisplayName("Should continue even when mapper reports unmappable rules")
    void shouldContinueWhenMapperReportsUnmappableRules() {
        Strategy strategy = buildStrategy(11L);
        SuggestTickersRequestDTO request = SuggestTickersRequestDTO.builder()
                .strategyId(11L)
                .build();

        when(strategyRepository.findById(11L)).thenReturn(Optional.of(strategy));
        when(finvizFilterMapper.map(strategy)).thenReturn(FinvizFilterMappingResult.builder()
                .filters("ta_sma20_pa")
                .unmappableRules(List.of("EMA_200"))
                .warnings(List.of("Unsupported rule detected"))
                .build());
        when(finvizScreenerPort.findTickers("ta_sma20_pa", 20)).thenReturn(List.of("MSFT"));
        when(analyzeAndPersistStockService.validateAndUpdateCompanyProfiles(List.of("MSFT")))
                .thenReturn(List.of("MSFT"));
        when(analyzeAndPersistStockService.analyzeAndPersist("MSFT", strategy, StockOrigin.SUGGESTION_SNAPSHOT))
                .thenReturn(buildStock("MSFT", true, "Compliant"));

        SuggestTickersResponseDTO result = suggestTickersService.suggestTickers(request);

        assertThat(result.getSuggestedTickers()).hasSize(1);
        assertThat(result.getSuggestedTickers().get(0).getTicker()).isEqualTo("MSFT");
        assertThat(result.getWarnings())
                .contains("Unsupported rule detected")
                .doesNotContain("Strict mode enabled: execution blocked due to unmappable strategy rules.");
        verify(finvizScreenerPort).findTickers("ta_sma20_pa", 20);
        verify(analyzeAndPersistStockService).analyzeAndPersist("MSFT", strategy, StockOrigin.SUGGESTION_SNAPSHOT);
    }

    @Test
    @DisplayName("Should use default max candidates when request values are null")
    void shouldApplyDefaultExecutionValues() {
        Strategy strategy = buildStrategy(12L);
        SuggestTickersRequestDTO request = SuggestTickersRequestDTO.builder()
                .strategyId(12L)
                .maxCandidates(null)
                .build();

        when(strategyRepository.findById(12L)).thenReturn(Optional.of(strategy));
        when(finvizFilterMapper.map(strategy)).thenReturn(FinvizFilterMappingResult.builder()
                .filters("ta_rsi_os30")
                .build());
        when(finvizScreenerPort.findTickers("ta_rsi_os30", 20)).thenReturn(List.of("NFLX"));
        when(analyzeAndPersistStockService.validateAndUpdateCompanyProfiles(List.of("NFLX")))
                .thenReturn(List.of("NFLX"));
        when(analyzeAndPersistStockService.analyzeAndPersist("NFLX", strategy, StockOrigin.SUGGESTION_SNAPSHOT))
                .thenReturn(buildStock("NFLX", true, "Compliant"));

        SuggestTickersResponseDTO result = suggestTickersService.suggestTickers(request);

        assertThat(result.getSuggestedTickers()).hasSize(1);
        assertThat(result.getSuggestedTickers().get(0).getSuitabilityStatus()).isEqualTo(TickerSuitabilityStatus.APTO);
    }

    @Test
    @DisplayName("Should throw when strategy id is missing")
    void shouldThrowWhenStrategyIdMissing() {
        SuggestTickersRequestDTO request = SuggestTickersRequestDTO.builder()
                .strategyId(null)
                .build();

        assertThatThrownBy(() -> suggestTickersService.suggestTickers(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Strategy ID is required");
    }

    @Test
    @DisplayName("Should degrade gracefully when Finviz screener fails")
    void shouldDegradeWhenFinvizFails() {
        Strategy strategy = buildStrategy(13L);
        SuggestTickersRequestDTO request = SuggestTickersRequestDTO.builder()
                .strategyId(13L)
                .build();

        when(strategyRepository.findById(13L)).thenReturn(Optional.of(strategy));
        when(finvizFilterMapper.map(strategy)).thenReturn(FinvizFilterMappingResult.builder()
                .filters("ta_sma20_pa")
                .build());
        when(finvizScreenerPort.findTickers("ta_sma20_pa", 20)).thenThrow(new RuntimeException("timeout"));

        SuggestTickersResponseDTO result = suggestTickersService.suggestTickers(request);

        assertThat(result.getSuggestedTickers()).isEmpty();
        assertThat(result.getWarnings())
                .contains("Finviz no está disponible temporalmente; la sugerencia se ha degradado sin resultados.");
        verify(suggestionSnapshotRepository).save(any());
        verify(analyzeAndPersistStockService, never()).analyzeAndPersist(any(), any(), any());
    }

    @Test
    @DisplayName("Should return latest persisted snapshot")
    void shouldReturnLatestPersistedSnapshot() {
        SuggestionSnapshot snapshot = SuggestionSnapshot.builder()
                .strategyId(99L)
                .suggestedAt(Instant.parse("2026-04-18T12:00:00Z"))
                .appliedFilters("ta_sma20_pa")
                .unmappableRules(List.of("ATR(14)"))
                .warnings(List.of("warning"))
                .suggestedTickers(List.of(
                        com.market.analysis.domain.model.SuggestedTickerSnapshot.builder()
                                .ticker("AAPL")
                                .strategyId(99L)
                                .suggestedAt(Instant.parse("2026-04-18T12:00:00Z"))
                                .suitabilityStatus(TickerSuitabilityStatus.APTO.name())
                                .deterministicMetrics(List.of("SMA20=123.45"))
                                .traceability(List.of("ok"))
                                .build()))
                .build();
        when(suggestionSnapshotRepository.findLatestByStrategyId(99L)).thenReturn(Optional.of(snapshot));

        Optional<SuggestTickersResponseDTO> result = suggestTickersService.getLatestSuggestionSnapshot(99L);

        assertThat(result).isPresent();
        assertThat(result.get().getSuggestedAt()).isEqualTo(Instant.parse("2026-04-18T12:00:00Z"));
        assertThat(result.get().getSuggestedTickers()).hasSize(1);
        assertThat(result.get().getSuggestedTickers().get(0).getTicker()).isEqualTo("AAPL");
        assertThat(result.get().getSuggestedTickers().get(0).getStrategyId()).isEqualTo(99L);
        assertThat(result.get().getSuggestedTickers().get(0).getSuggestedAt())
                .isEqualTo(Instant.parse("2026-04-18T12:00:00Z"));
        assertThat(result.get().getSuggestedTickers().get(0).getDeterministicMetrics()).containsExactly("SMA20=123.45");
    }

        @Test
        @DisplayName("Should switch only snapshot-suggestion origins to external provider")
        void shouldSwitchSuggestedTickerOrigins() {
                Stock snapshotStock = stockWithOrigin("AAPL", StockOrigin.SUGGESTION_SNAPSHOT);
                Stock strategySuggestionStock = stockWithOrigin("TSLA", StockOrigin.STRATEGY_SUGGESTION);
                Stock externalStock = stockWithOrigin("MSFT", StockOrigin.ANALYSIS);
                when(stockDataRepository.findAllByStrategyId(7L)).thenReturn(List.of(snapshotStock, strategySuggestionStock, externalStock));

                int switched = suggestTickersService.convertSuggestedTickersToAnalysis(7L);

                assertThat(switched).isEqualTo(2);
                assertThat(snapshotStock.getOrigin()).isEqualTo(StockOrigin.ANALYSIS);
                assertThat(strategySuggestionStock.getOrigin()).isEqualTo(StockOrigin.ANALYSIS);
                assertThat(externalStock.getOrigin()).isEqualTo(StockOrigin.ANALYSIS);
                verify(stockDataRepository).findAllByStrategyId(7L);
                verify(stockDataRepository, times(2)).save(any(Stock.class));
        }

        @Test
        @DisplayName("Should return zero when no stocks are eligible to switch")
        void shouldReturnZeroWhenNoEligibleStocksToSwitch() {
                when(stockDataRepository.findAllByStrategyId(8L)).thenReturn(List.of(
                                stockWithOrigin("MSFT", StockOrigin.ANALYSIS),
                                stockWithOrigin("IBM", null)));

                int switched = suggestTickersService.convertSuggestedTickersToAnalysis(8L);

                assertThat(switched).isZero();
                verify(stockDataRepository).findAllByStrategyId(8L);
                verify(stockDataRepository, never()).save(any(Stock.class));
        }

        private Stock stockWithOrigin(String ticker, StockOrigin origin) {
                return Stock.builder()
                                .ticker(ticker)
                                .origin(origin)
                                .build();
        }

    private Stock buildStock(String ticker, boolean compliant, String summary) {
        return Stock.builder()
                .ticker(ticker)
                .origin(StockOrigin.SUGGESTION_SNAPSHOT)
                .strategyEvaluation(StrategyEvaluation.builder()
                        .ticker(ticker)
                        .strategyId(1L)
                        .strategyName("Breakout strategy")
                        .compliant(compliant)
                        .summary(summary)
                        .build())
                .build();
    }

    private Strategy buildStrategy(Long id) {
        return Strategy.builder()
                .id(id)
                .name("Breakout strategy")
                .description("Strategy for ticker suggestions")
                .rules(List.of(Rule.builder()
                        .name("SMA > PRICE")
                        .subjectCode("SMA")
                        .subjectParam(20.0)
                        .operator(">")
                        .targetCode("PRICE")
                        .build()))
                .objective(StrategyObjective.builder()
                        .targetType(ObjectiveType.PERCENTAGE)
                        .targetValue(BigDecimal.valueOf(5.0))
                        .stopLossType(ObjectiveType.PERCENTAGE)
                        .stopLossValue(BigDecimal.valueOf(2.0))
                        .capitalToRisk(BigDecimal.valueOf(1000.0))
                        .description("objective")
                        .build())
                .build();
    }
}
