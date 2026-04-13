package com.market.analysis.domain.model;

import java.math.BigDecimal;

/**
 * Functional interface for resolving an indicator value from stock data.
 *
 * <p>Each supported indicator has a resolver registered in
 * {@link RuleCapabilityCatalog}. The resolver encapsulates the mapping from
 * an optional parameter and a {@link Stock} instance to the corresponding
 * numeric value, replacing the scattered {@code switch} logic previously
 * embedded in the evaluator.</p>
 */
@FunctionalInterface
public interface IndicatorResolver {

    /**
     * Resolves the indicator value for the given parameter and stock data.
     *
     * @param param the indicator parameter (may be {@code null} for no-param indicators)
     * @param stock the stock data to resolve against
     * @return the resolved value, or {@code null} if the stock does not carry
     *         the required data for that parameter
     */
    BigDecimal resolve(Double param, Stock stock);
}
