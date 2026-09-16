package com.market.analysis.application.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of updating a strategy, including the tickers whose risk plan
 * could not be recalculated (compliant evaluations with null risk fields).
 * Carries the degraded ticker symbols so the presentation layer can warn
 * the user where to look in the analysis section.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStrategyResult {

    private StrategyDTO strategy;

    private List<String> degradedTickers;
}
