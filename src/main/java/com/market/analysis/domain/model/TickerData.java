package com.market.analysis.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
     * Historical OHLCV data for pattern analysis and technical calculations.
     * Each MarketDataPoint represents a single time period (e.g., daily candle).
     */
    private final List<MarketDataPoint> historicalData;

    /**
     * Gets an immutable copy of the historical data list.
     *
     * @return unmodifiable list of market data points
     */
    public List<MarketDataPoint> getHistoricalData() {
        return historicalData != null ? List.copyOf(historicalData) : List.of();
    }

    /**
     * Custom builder to ensure defensive copying of the historical data list.
     */
    public static class TickerDataBuilder {
        public TickerDataBuilder historicalData(List<MarketDataPoint> historicalData) {
            this.historicalData = historicalData != null ? new ArrayList<>(historicalData) : new ArrayList<>();
            return this;
        }
    }
}
