package com.market.analysis.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Input contract for requesting market ticker suggestions from a strategy.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestTickersRequestDTO {

    private Long strategyId;
    private Integer maxCandidates;
}
