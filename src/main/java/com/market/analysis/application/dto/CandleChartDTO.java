package com.market.analysis.application.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the full payload required to render a candlestick chart
 * for a given ticker, including the ordered OHLCV series and the current
 * scalar SMA values (20, 50, 200 periods).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandleChartDTO {

    private String ticker;

    /** Ordered OHLCV candle series (ascending by time). */
    private List<CandleDTO> candles;

    /** Current 20-period SMA; {@code null} if insufficient data. */
    private BigDecimal sma20;

    /** Current 50-period SMA; {@code null} if insufficient data. */
    private BigDecimal sma50;

    /** Current 200-period SMA; {@code null} if insufficient data. */
    private BigDecimal sma200;
}
