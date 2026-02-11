package com.market.analysis.infrastructure.external.finnhub.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing real-time quote from Finnhub API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteData {

    private String symbol;

    /** Current price */
    private BigDecimal c;

    /** Change */
    private Double d;

    /** Percent change */
    private BigDecimal dp;

    /** High price of the day */
    private BigDecimal h;

    /** Low price of the day */
    private BigDecimal l;

    /** Open price of the day */
    private BigDecimal o;

    /** Previous close price */
    private BigDecimal pc;

    /** Timestamp */
    private Long t;

    /**
     * Checks if the quote data is valid by ensuring it has a non-null symbol, a
     * positive current price, a valid timestamp, and coherent high/low prices.
     * 
     * @return true if the quote data has a non-null symbol, a positive current
     *         price, a valid timestamp, and coherent high/low prices; false
     *         otherwise.
     */
    public boolean isValid() {
        boolean hasSymbol = symbol != null && !symbol.isBlank();
        boolean hasValidPrice = c != null && c.compareTo(BigDecimal.ZERO) > 0;
        boolean hasValidTime = t != null && t > 0;
        boolean isCoherent = true;
        if (h != null && l != null) {
            isCoherent = h.compareTo(l) >= 0;
        }

        return hasSymbol && hasValidPrice && hasValidTime && isCoherent;
    }
}
