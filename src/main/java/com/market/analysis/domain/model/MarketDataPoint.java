package com.market.analysis.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Entity representing a single OHLCV (Open, High, Low, Close, Volume) market data point.
 * Used for historical data and technical analysis calculations.
 */
@Getter
@Builder
@ToString
public class MarketDataPoint {

    /**
     * Date and time of this data point.
     */
    private final LocalDateTime date;

    /**
     * Opening price for the period.
     */
    private final BigDecimal open;

    /**
     * Highest price during the period.
     */
    private final BigDecimal high;

    /**
     * Lowest price during the period.
     */
    private final BigDecimal low;

    /**
     * Closing price for the period.
     */
    private final BigDecimal close;

    /**
     * Trading volume during the period.
     */
    private final Long volume;
}
