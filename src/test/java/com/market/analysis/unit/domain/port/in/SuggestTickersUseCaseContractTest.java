package com.market.analysis.unit.domain.port.in;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.application.dto.SuggestTickersRequestDTO;
import com.market.analysis.application.dto.SuggestTickersResponseDTO;
import com.market.analysis.application.dto.SuggestedTickerDTO;
import com.market.analysis.application.dto.TickerSuitabilityStatus;
import com.market.analysis.domain.port.in.SuggestTickersUseCase;

@DisplayName("SuggestTickersUseCase Contract Tests")
class SuggestTickersUseCaseContractTest {

    @Test
    @DisplayName("Should keep APTO and NO_APTO states in response contract")
    void shouldKeepAptoAndNoAptoStates() {
        SuggestTickersUseCase useCase = new SuggestTickersUseCase() {
            @Override
            public SuggestTickersResponseDTO suggestTickers(SuggestTickersRequestDTO request) {
                return SuggestTickersResponseDTO.builder()
                        .strategyId(request.getStrategyId())
                        .appliedFilters("ta_sma20_pa")
                        .suggestedTickers(List.of(
                                SuggestedTickerDTO.builder()
                                        .ticker("AAPL")
                                        .suitabilityStatus(TickerSuitabilityStatus.APTO)
                                        .traceability(List.of("Cumple reglas"))
                                        .build(),
                                SuggestedTickerDTO.builder()
                                        .ticker("TSLA")
                                        .suitabilityStatus(TickerSuitabilityStatus.NO_APTO)
                                        .traceability(List.of("No cumple volumen"))
                                        .build()))
                        .warnings(List.of("Regla ATR(14) marcada como UNMAPPABLE"))
                        .unmappableRules(List.of("ATR(14)"))
                        .build();
            }

            @Override
            public Optional<SuggestTickersResponseDTO> getLatestSuggestionSnapshot(Long strategyId) {
                return Optional.empty();
            }

            @Override
            public int convertSuggestedTickersToAnalysis(long strategyId) {
                return 0;
            }
        };

        SuggestTickersResponseDTO response = useCase.suggestTickers(SuggestTickersRequestDTO.builder()
                .strategyId(5L)
                .build());

        assertThat(response.getSuggestedTickers())
                .extracting(SuggestedTickerDTO::getSuitabilityStatus)
                .containsExactly(TickerSuitabilityStatus.APTO, TickerSuitabilityStatus.NO_APTO);
        assertThat(response.getWarnings()).containsExactly("Regla ATR(14) marcada como UNMAPPABLE");
        assertThat(response.getUnmappableRules()).containsExactly("ATR(14)");
    }
}
