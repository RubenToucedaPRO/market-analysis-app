package com.market.analysis.unit.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.application.dto.SuggestTickersRequestDTO;
import com.market.analysis.application.dto.SuggestTickersResponseDTO;
import com.market.analysis.application.dto.TickerSuitabilityStatus;
import com.market.analysis.application.usecase.DeterministicTickerEvaluation;
import com.market.analysis.application.usecase.DeterministicTickerEvaluator;
import com.market.analysis.application.usecase.SuggestTickersService;
import com.market.analysis.domain.model.FinvizFilterMappingResult;
import com.market.analysis.domain.model.ObjectiveType;
import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.StrategyObjective;
import com.market.analysis.domain.model.SuggestionSnapshot;
import com.market.analysis.domain.port.out.FinvizScreenerPort;
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
    private DeterministicTickerEvaluator deterministicTickerEvaluator;

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
        when(deterministicTickerEvaluator.evaluate("AAPL", strategy)).thenReturn(DeterministicTickerEvaluation.builder()
                .suitable(true)
                .traceability(List.of("All deterministic rules passed"))
                .build());
        when(deterministicTickerEvaluator.evaluate("TSLA", strategy)).thenReturn(DeterministicTickerEvaluation.builder()
                .suitable(false)
                .traceability(List.of("Rule RSI failed"))
                .build());

        SuggestTickersResponseDTO result = suggestTickersService.suggestTickers(request);

        assertThat(result.getUnmappableRules()).containsExactly("MACD");
        assertThat(result.getWarnings()).containsExactly("Partial mapping used");
        assertThat(result.getSuggestedTickers()).hasSize(2);
        assertThat(result.getSuggestedAt()).isNotNull();
        assertThat(result.getSuggestedTickers().get(0).getSuitabilityStatus()).isEqualTo(TickerSuitabilityStatus.APTO);
        assertThat(result.getSuggestedTickers().get(1).getSuitabilityStatus()).isEqualTo(TickerSuitabilityStatus.NO_APTO);
        assertThat(result.getSuggestedTickers().get(1).getTraceability()).containsExactly("Rule RSI failed");
        verify(suggestionSnapshotRepository).save(org.mockito.ArgumentMatchers.any());

        ArgumentCaptor<SuggestionSnapshot> snapshotCaptor = ArgumentCaptor.forClass(SuggestionSnapshot.class);
        verify(suggestionSnapshotRepository).save(snapshotCaptor.capture());
        SuggestionSnapshot persistedSnapshot = snapshotCaptor.getValue();
        assertThat(persistedSnapshot.getSuggestedTickers()).hasSize(2);
        assertThat(persistedSnapshot.getSuggestedTickers().get(0).getStrategyId()).isEqualTo(10L);
        assertThat(persistedSnapshot.getSuggestedTickers().get(0).getSuggestedAt()).isEqualTo(result.getSuggestedAt());
        assertThat(persistedSnapshot.getSuggestedTickers().get(1).getStrategyId()).isNull();
        assertThat(persistedSnapshot.getSuggestedTickers().get(1).getSuggestedAt()).isNull();
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
        when(deterministicTickerEvaluator.evaluate("MSFT", strategy)).thenReturn(DeterministicTickerEvaluation.builder()
                .suitable(true)
                .traceability(List.of("Compliant"))
                .build());

        SuggestTickersResponseDTO result = suggestTickersService.suggestTickers(request);

        assertThat(result.getSuggestedTickers()).hasSize(1);
        assertThat(result.getSuggestedTickers().get(0).getTicker()).isEqualTo("MSFT");
        assertThat(result.getWarnings())
                .contains("Unsupported rule detected")
                .doesNotContain("Strict mode enabled: execution blocked due to unmappable strategy rules.");
        verify(finvizScreenerPort).findTickers("ta_sma20_pa", 20);
        verify(deterministicTickerEvaluator).evaluate("MSFT", strategy);
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
        when(deterministicTickerEvaluator.evaluate("NFLX", strategy)).thenReturn(DeterministicTickerEvaluation.builder()
                .suitable(true)
                .traceability(List.of("Compliant"))
                .build());

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
        verify(suggestionSnapshotRepository).save(org.mockito.ArgumentMatchers.any());
        verify(deterministicTickerEvaluator, never()).evaluate(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Should return latest persisted snapshot")
    void shouldReturnLatestPersistedSnapshot() {
        com.market.analysis.domain.model.SuggestionSnapshot snapshot = com.market.analysis.domain.model.SuggestionSnapshot.builder()
                .strategyId(99L)
                .suggestedAt(java.time.Instant.parse("2026-04-18T12:00:00Z"))
                .appliedFilters("ta_sma20_pa")
                .unmappableRules(List.of("ATR(14)"))
                .warnings(List.of("warning"))
                .suggestedTickers(List.of(
                        com.market.analysis.domain.model.SuggestedTickerSnapshot.builder()
                                .ticker("AAPL")
                                .strategyId(99L)
                                .suggestedAt(java.time.Instant.parse("2026-04-18T12:00:00Z"))
                                .suitabilityStatus(TickerSuitabilityStatus.APTO.name())
                                .deterministicMetrics(List.of("SMA20=123.45"))
                                .traceability(List.of("ok"))
                                .build()))
                .build();
        when(suggestionSnapshotRepository.findLatestByStrategyId(99L)).thenReturn(Optional.of(snapshot));

        Optional<SuggestTickersResponseDTO> result = suggestTickersService.getLatestSuggestionSnapshot(99L);

        assertThat(result).isPresent();
        assertThat(result.get().getSuggestedAt()).isEqualTo(java.time.Instant.parse("2026-04-18T12:00:00Z"));
        assertThat(result.get().getSuggestedTickers()).hasSize(1);
        assertThat(result.get().getSuggestedTickers().get(0).getTicker()).isEqualTo("AAPL");
        assertThat(result.get().getSuggestedTickers().get(0).getStrategyId()).isEqualTo(99L);
        assertThat(result.get().getSuggestedTickers().get(0).getSuggestedAt())
                .isEqualTo(java.time.Instant.parse("2026-04-18T12:00:00Z"));
        assertThat(result.get().getSuggestedTickers().get(0).getDeterministicMetrics()).containsExactly("SMA20=123.45");
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
