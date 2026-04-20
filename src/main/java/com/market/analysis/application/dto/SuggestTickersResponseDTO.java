package com.market.analysis.application.dto;

import java.util.List;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Output contract for suggested tickers and mapping traceability.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestTickersResponseDTO {

    private Long strategyId;
    private String appliedFilters;
    private Instant suggestedAt;
    private List<String> unmappableRules;
    private List<String> warnings;
    private List<SuggestedTickerDTO> suggestedTickers;
}
