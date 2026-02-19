package com.market.analysis.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDataDTO {
    private Long id;

    /**
     * Ticker symbol (e.g., "AAPL", "GOOGL").
     */
    private String ticker;

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

    /** ID of the associated strategy */
    private Long strategyId;

    /** Name of the associated strategy */
    private String strategyName;

    /** Whether the strategy evaluation passed */
    private Boolean evaluationPassed;

    /** Compliance rate (percentage of rules passed) */
    private BigDecimal complianceRate;

    /** Summary of the evaluation result */
    private String evaluationSummary;

    /** AI valoration of the stock */
    private String valorationIA;

    /** Calculated target price from risk-reward evaluation */
    private BigDecimal targetPrice;

    /** Calculated stop-loss price from risk-reward evaluation */
    private BigDecimal stopLossPrice;

    /** Risk-reward ratio calculated at evaluation time */
    private BigDecimal riskRewardRatio;

    /** Recommended number of shares based on capital at risk */
    private Integer recommendedShares;
}
