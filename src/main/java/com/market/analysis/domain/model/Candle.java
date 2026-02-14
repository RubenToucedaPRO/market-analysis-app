package com.market.analysis.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Entity representing a single OHLCV (Open, High, Low, Close, Volume) market
 * data point.
 * Used for historical data and technical analysis calculations.
 */
@Getter
@Builder
@ToString
public class Candle {

    /**
     * Ticker symbol (e.g., "AAPL", "GOOGL").
     */
    private final String ticker;

    /**
     * Date and time of this data point.
     */
    private final Instant dateTime;

    /**
     * Opening price for the period.
     */
    private final BigDecimal openPrice;

    /**
     * Highest price during the period.
     */
    private final BigDecimal highPrice;

    /**
     * Lowest price during the period.
     */
    private final BigDecimal lowPrice;

    /**
     * Closing price for the period.
     */
    private final BigDecimal closePrice;

    /**
     * Trading volume during the period.
     */
    private final Long volume;

}
