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
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.application.dto.FinvizExecutionMode;
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
import com.market.analysis.domain.port.out.FinvizScreenerPort;
import com.market.analysis.domain.port.out.StrategyRepository;
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

    @InjectMocks
    private SuggestTickersService suggestTickersService;

    @Test
    @DisplayName("Should classify APTO and NO_APTO in tolerant mode with traceability")
    void shouldClassifyTickersInTolerantMode() {
        Strategy strategy = buildStrategy(10L);
        SuggestTickersRequestDTO request = SuggestTickersRequestDTO.builder()
                .strategyId(10L)
                .maxCandidates(5)
                .executionMode(FinvizExecutionMode.TOLERANT)
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

        assertThat(result.getExecutionMode()).isEqualTo(FinvizExecutionMode.TOLERANT);
        assertThat(result.getUnmappableRules()).containsExactly("MACD");
        assertThat(result.getWarnings()).containsExactly("Partial mapping used");
        assertThat(result.getSuggestedTickers()).hasSize(2);
        assertThat(result.getSuggestedTickers().get(0).getSuitabilityStatus()).isEqualTo(TickerSuitabilityStatus.APTO);
        assertThat(result.getSuggestedTickers().get(1).getSuitabilityStatus()).isEqualTo(TickerSuitabilityStatus.NO_APTO);
        assertThat(result.getSuggestedTickers().get(1).getTraceability()).containsExactly("Rule RSI failed");
    }

    @Test
    @DisplayName("Should stop in strict mode when mapper reports unmappable rules")
    void shouldStopWhenStrictModeAndUnmappableRules() {
        Strategy strategy = buildStrategy(11L);
        SuggestTickersRequestDTO request = SuggestTickersRequestDTO.builder()
                .strategyId(11L)
                .executionMode(FinvizExecutionMode.STRICT)
                .build();

        when(strategyRepository.findById(11L)).thenReturn(Optional.of(strategy));
        when(finvizFilterMapper.map(strategy)).thenReturn(FinvizFilterMappingResult.builder()
                .filters("ta_sma20_pa")
                .unmappableRules(List.of("EMA_200"))
                .warnings(List.of("Unsupported rule detected"))
                .build());

        SuggestTickersResponseDTO result = suggestTickersService.suggestTickers(request);

        assertThat(result.getSuggestedTickers()).isEmpty();
        assertThat(result.getWarnings())
                .contains("Unsupported rule detected")
                .contains("Strict mode enabled: execution blocked due to unmappable strategy rules.");
        verify(finvizScreenerPort, never()).findTickers(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyInt());
        verify(deterministicTickerEvaluator, never()).evaluate(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Should use tolerant mode and default max candidates when request values are null")
    void shouldApplyDefaultExecutionValues() {
        Strategy strategy = buildStrategy(12L);
        SuggestTickersRequestDTO request = SuggestTickersRequestDTO.builder()
                .strategyId(12L)
                .executionMode(null)
                .maxCandidates(null)
                .build();

        when(strategyRepository.findById(12L)).thenReturn(Optional.of(strategy));
        when(finvizFilterMapper.map(strategy)).thenReturn(FinvizFilterMappingResult.builder()
                .filters("ta_rsi_os30")
                .build());
        when(finvizScreenerPort.findTickers("ta_rsi_os30", 25)).thenReturn(List.of("NFLX"));
        when(deterministicTickerEvaluator.evaluate("NFLX", strategy)).thenReturn(DeterministicTickerEvaluation.builder()
                .suitable(true)
                .traceability(List.of("Compliant"))
                .build());

        SuggestTickersResponseDTO result = suggestTickersService.suggestTickers(request);

        assertThat(result.getExecutionMode()).isEqualTo(FinvizExecutionMode.TOLERANT);
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
        when(finvizScreenerPort.findTickers("ta_sma20_pa", 25)).thenThrow(new RuntimeException("timeout"));

        SuggestTickersResponseDTO result = suggestTickersService.suggestTickers(request);

        assertThat(result.getSuggestedTickers()).isEmpty();
        assertThat(result.getWarnings())
                .contains("Finviz no está disponible temporalmente; la sugerencia se ha degradado sin resultados.");
        verify(deterministicTickerEvaluator, never()).evaluate(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
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
