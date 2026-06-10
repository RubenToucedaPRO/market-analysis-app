package com.market.analysis.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class TechnicalIndicators {

    BigDecimal sma20;

    BigDecimal sma50;

    BigDecimal sma200;

    Long currentVolume;

    Long averageVolume;

    Instant lastUpdated;

    // EMA
    BigDecimal ema9;
    BigDecimal ema12;
    BigDecimal ema20;
    BigDecimal ema26;
    BigDecimal ema50;
    BigDecimal ema200;

    // RSI
    BigDecimal rsi14;
    BigDecimal rsi30;

    // MACD (derived from EMA)
    BigDecimal macdLine;      // EMA(12) - EMA(26)
    BigDecimal macdSignal;    // EMA(9) of MACD line
    BigDecimal macdHistogram; // macdLine - macdSignal

    // Bollinger Bands (period 20)
    BigDecimal bbUpper20;     // SMA20 + 2*StdDev20
    BigDecimal bbLower20;     // SMA20 - 2*StdDev20

    // ATR
    BigDecimal atr14;
}
