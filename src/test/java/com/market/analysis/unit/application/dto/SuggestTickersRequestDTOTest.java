package com.market.analysis.unit.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.application.dto.SuggestTickersRequestDTO;

@DisplayName("SuggestTickersRequestDTO Contract Tests")
class SuggestTickersRequestDTOTest {

    @Test
    @DisplayName("Should build request without execution mode")
    void shouldBuildRequestWithoutExecutionMode() {
        SuggestTickersRequestDTO request = SuggestTickersRequestDTO.builder()
                .strategyId(10L)
                .maxCandidates(25)
                .build();

        assertThat(request.getStrategyId()).isEqualTo(10L);
        assertThat(request.getMaxCandidates()).isEqualTo(25);
    }
}
