package com.market.analysis.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

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

    private Long id;

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
    private Instant lastUpdated;

    /**
     * ID of the strategy associated with this ticker.
     * This enables strategy evaluation when ticker is added/updated.
     */
    private Long strategyId;

    private StrategyEvaluation strategyEvaluation;

    private String valorationIA;

    // EMA
    private BigDecimal ema9;
    private BigDecimal ema12;
    private BigDecimal ema20;
    private BigDecimal ema26;
    private BigDecimal ema50;
    private BigDecimal ema200;

    // RSI
    private BigDecimal rsi14;
    private BigDecimal rsi30;

    // MACD (derived from EMA)
    private BigDecimal macdLine;      // EMA(12) - EMA(26)
    private BigDecimal macdSignal;    // EMA(9) of MACD line
    private BigDecimal macdHistogram; // macdLine - macdSignal

    // Bollinger Bands (period 20)
    private BigDecimal bbUpper20;     // SMA20 + 2*StdDev20
    private BigDecimal bbLower20;     // SMA20 - 2*StdDev20

    // ATR
    private BigDecimal atr14;

}
