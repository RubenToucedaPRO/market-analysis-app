package com.market.analysis.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.market.analysis.domain.model.HistoricalData;
import com.market.analysis.domain.model.TechnicalIndicators;

public class StockHistoricalService {

    private static final int SMA_SCALE = 4;

    public TechnicalIndicators calculateIndicators(HistoricalData data, int volumePeriod) {
        BigDecimal sma20 = calculateSma(data.getClosingPrices(), 20);
        BigDecimal sma50 = calculateSma(data.getClosingPrices(), 50);
        BigDecimal sma200 = calculateSma(data.getClosingPrices(), 200);

        Long currentVolume = data.getVolumes().isEmpty() ? null : data.getVolumes().get(0);
        Long avgVolume = calculateAverageVolume(data.getVolumes(), volumePeriod);

        BigDecimal ema9 = calculateEma(data.getClosingPrices(), 9);
        BigDecimal ema12 = calculateEma(data.getClosingPrices(), 12);
        BigDecimal ema20 = calculateEma(data.getClosingPrices(), 20);
        BigDecimal ema26 = calculateEma(data.getClosingPrices(), 26);
        BigDecimal ema50 = calculateEma(data.getClosingPrices(), 50);
        BigDecimal ema200 = calculateEma(data.getClosingPrices(), 200);

        BigDecimal rsi14 = calculateRsi(data.getClosingPrices(), 14);
        BigDecimal rsi30 = calculateRsi(data.getClosingPrices(), 30);

        return TechnicalIndicators.builder()
                .sma20(sma20)
                .sma50(sma50)
                .sma200(sma200)
                .currentVolume(currentVolume)
                .averageVolume(avgVolume)
                .lastUpdated(data.getLastUpdate())
                .ema9(ema9)
                .ema12(ema12)
                .ema20(ema20)
                .ema26(ema26)
                .ema50(ema50)
                .ema200(ema200)
                .rsi14(rsi14)
                .rsi30(rsi30)
                .build();
    }

    /**
     * Calculates the Exponential Moving Average (EMA) for the given period.
     *
     * <p>Polygon returns data sorted descending (most recent first). This method
     * reverses the list internally so that the EMA algorithm processes prices from
     * oldest to newest.
     *
     * @param prices closing prices in descending order (most recent at index 0)
     * @param period EMA period (e.g. 9, 12, 20, 26, 50, 200)
     * @return the latest EMA value rounded to 4 decimal places, or {@code null} if
     *         there are fewer prices than the required period
     */
    BigDecimal calculateEma(List<Double> prices, int period) {
        if (prices == null || prices.size() < period) {
            return null;
        }

        // Polygon returns data desc; reverse to process oldest → newest
        List<Double> asc = new ArrayList<>(prices);
        Collections.reverse(asc);

        // Seed: SMA of the first `period` values (oldest prices)
        double seed = asc.stream()
                .limit(period)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double multiplier = 2.0 / (period + 1);
        double ema = seed;

        for (int i = period; i < asc.size(); i++) {
            ema = (asc.get(i) - ema) * multiplier + ema;
        }

        return BigDecimal.valueOf(ema).setScale(SMA_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the Relative Strength Index (RSI) for the given period using a
     * simple average of gains and losses over the first {@code period} price changes.
     *
     * <p>Polygon returns data sorted descending (most recent first). This method
     * reverses the list internally so that deltas are computed from oldest to newest.
     *
     * <p>Edge cases:
     * <ul>
     *   <li>If avgLoss is zero → RSI = 100 (no losses in the period)</li>
     *   <li>If avgGain is zero → RSI = 0 (no gains in the period)</li>
     * </ul>
     *
     * @param prices closing prices in descending order (most recent at index 0)
     * @param period RSI period (e.g. 14, 30)
     * @return the RSI value rounded to 4 decimal places, or {@code null} if there
     *         are fewer than {@code period + 1} prices
     */
    BigDecimal calculateRsi(List<Double> prices, int period) {
        if (prices == null || prices.size() < period + 1) {
            return null;
        }

        // Polygon returns data desc; reverse to process oldest → newest
        List<Double> asc = new ArrayList<>(prices);
        Collections.reverse(asc);

        double totalGain = 0.0;
        double totalLoss = 0.0;

        for (int i = 1; i <= period; i++) {
            double delta = asc.get(i) - asc.get(i - 1);
            if (delta > 0) {
                totalGain += delta;
            } else {
                totalLoss += Math.abs(delta);
            }
        }

        double avgGain = totalGain / period;
        double avgLoss = totalLoss / period;

        double rsi;
        if (avgLoss == 0.0) {
            rsi = 100.0;
        } else if (avgGain == 0.0) {
            rsi = 0.0;
        } else {
            double rs = avgGain / avgLoss;
            rsi = 100.0 - (100.0 / (1.0 + rs));
        }

        return BigDecimal.valueOf(rsi).setScale(SMA_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateSma(List<Double> prices, int period) {
        if (prices == null || prices.size() < period)
            return null;

        double sum = prices.stream()
                .limit(period)
                .mapToDouble(Double::doubleValue)
                .sum();

        return BigDecimal.valueOf(sum / period)
                .setScale(SMA_SCALE, RoundingMode.HALF_UP);
    }

    private Long calculateAverageVolume(List<Long> volumes, int period) {
        if (volumes == null || volumes.size() < period)
            return null;

        long sum = volumes.stream()
                .limit(period)
                .mapToLong(Long::longValue)
                .sum();

        return sum / period;
    }
}