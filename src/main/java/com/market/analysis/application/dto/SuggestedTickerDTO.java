package com.market.analysis.application.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Candidate ticker with deterministic suitability status and traceability.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestedTickerDTO {

    private String ticker;
    private Long strategyId;
    private Instant suggestedAt;
    private TickerSuitabilityStatus suitabilityStatus;
    private List<String> deterministicMetrics;
    private List<String> traceability;
}
