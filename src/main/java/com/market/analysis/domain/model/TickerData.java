package com.market.analysis.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Entity representing ticker data used for technical analysis evaluation.
 * Contains market information and technical indicators for a specific ticker.
 */
@Getter
@Builder
@ToString
public class TickerData {

    /**
     * Ticker symbol (e.g., "AAPL", "GOOGL").
     */
    private final String ticker;

    /**
     * Current price of the ticker.
     */
    private final BigDecimal currentPrice;

    /**
     * Volume information.
     */
    private final Long volume;

    /**
     * Timestamp of the data.
     */
    private final LocalDateTime timestamp;

    /**
     * Additional technical indicators and data points.
     * Can include moving averages, RSI, MACD, etc.
     */
    private final Map<String, Object> indicators;

    /**
     * Historical OHLCV data if needed for pattern analysis.
     */
    private final Map<String, Object> historicalData;
}
