package com.market.analysis.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity representing ticker data used for technical analysis evaluation.
 * Contains market information and technical indicators for a specific ticker.
 */
@Getter
@Setter
@Builder
@ToString
public class Stock {

    /**
     * Ticker symbol (e.g., "AAPL", "GOOGL").
     */
    private final String ticker;

    /**
     * URL of the company logo, obtained from the company profile API. This field is
     * optional and may be null if the logo is not available.
     */
    private String logoUrl;

    /**
     * Current price of the ticker.
     */
    private final BigDecimal currentPrice;

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
