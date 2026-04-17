package com.market.analysis.application.dto;

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
    private TickerSuitabilityStatus suitabilityStatus;
    private List<String> traceability;
}
