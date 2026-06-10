package com.market.analysis.application.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a single OHLCV candlestick data point for chart rendering.
 * The {@code time} field uses Unix epoch seconds, which is the format expected
 * by TradingView Lightweight Charts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandleDTO {

    /** Unix epoch seconds (required by TradingView Lightweight Charts). */
    private long time;

    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private Long volume;
}
