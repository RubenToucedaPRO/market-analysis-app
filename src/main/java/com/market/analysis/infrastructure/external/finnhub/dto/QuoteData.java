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
     * Checks if the quote data is valid.
     * 
     * @return true if current price is available
     */
    public boolean isValid() {
        return c != null && c.compareTo(BigDecimal.ZERO) > 0;
    }
}
