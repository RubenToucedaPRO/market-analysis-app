package com.market.analysis.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDataDTO {

    /**
     * Ticker symbol (e.g., "AAPL", "GOOGL").
     */
    private String ticker;
    /**
     * Current price of the ticker.
     */
    private BigDecimal currentPrice;

    /** Open price of the day from /quote API (o field) */
    private BigDecimal openPrice;

    /** High price of the day from /quote API (h field) */
    private BigDecimal highOfDay;

    /** Low price of the day from /quote API (l field) */
    private BigDecimal lowOfDay;

    /** Previous close price from /quote API (pc field) */
    private BigDecimal previousClose;

    /** SMA20 - Manually entered by user */
    private BigDecimal sma20;

    /** SMA50 - Manually entered by user */
    private BigDecimal sma50;

    /** SMA200 - Manually entered by user */
    private BigDecimal sma200;

    /** Current volume from candle data */
    private Long volume;

    /** Average volume (60-day period) calculated from candle data */
    private Long averageVolume;

    /** Last updated timestamp */
    private LocalDateTime lastUpdated;
}
