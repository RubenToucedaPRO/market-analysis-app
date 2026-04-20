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
    private StockOrigin origin;

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

    /**
     * Applies technical indicators to this stock snapshot.
     *
     * @param indicators technical indicators calculated for the same ticker; if
     *                   null, the stock remains unchanged.
     */
    public void applyTechnicalIndicators(TechnicalIndicators indicators) {
        if (indicators == null) {
            return;
        }
        this.sma20 = indicators.getSma20();
        this.sma50 = indicators.getSma50();
        this.sma200 = indicators.getSma200();
        this.volume = indicators.getCurrentVolume();
        this.averageVolume = indicators.getAverageVolume();
        this.lastUpdated = indicators.getLastUpdated();
        this.ema9 = indicators.getEma9();
        this.ema12 = indicators.getEma12();
        this.ema20 = indicators.getEma20();
        this.ema26 = indicators.getEma26();
        this.ema50 = indicators.getEma50();
        this.ema200 = indicators.getEma200();
        this.rsi14 = indicators.getRsi14();
        this.rsi30 = indicators.getRsi30();
        this.macdLine = indicators.getMacdLine();
        this.macdSignal = indicators.getMacdSignal();
        this.macdHistogram = indicators.getMacdHistogram();
        this.bbUpper20 = indicators.getBbUpper20();
        this.bbLower20 = indicators.getBbLower20();
        this.atr14 = indicators.getAtr14();
    }

}
