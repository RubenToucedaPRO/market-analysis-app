package com.market.analysis.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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

        BigDecimal[] macd = calculateMacd(data.getClosingPrices());
        BigDecimal macdLine      = macd != null ? macd[0] : null;
        BigDecimal macdSignal    = macd != null ? macd[1] : null;
        BigDecimal macdHistogram = macd != null ? macd[2] : null;

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
                .macdLine(macdLine)
                .macdSignal(macdSignal)
                .macdHistogram(macdHistogram)
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
        asc = asc.reversed();

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
        asc = asc.reversed();

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

    /**
     * Calculates MACD Line, Signal Line and Histogram from closing prices.
     *
     * <p>Standard parameters: fast EMA = 12, slow EMA = 26, signal EMA = 9.
     * Requires at least {@code slow + signal} (35) data points.
     *
     * @param prices closing prices in descending order (most recent at index 0)
     * @return {@code BigDecimal[3]} = {macdLine, macdSignal, macdHistogram}, or
     *         {@code null} if there are insufficient data points
     */
    BigDecimal[] calculateMacd(List<Double> prices) {
        final int fast = 12;
        final int slow = 26;
        final int signal = 9;

        if (prices == null || prices.size() < slow + signal) {
            return null;
        }

        // Polygon returns data desc; reverse to process oldest → newest
        List<Double> asc = new ArrayList<>(prices);
        asc = asc.reversed();

        List<Double> emaFast = calculateEmaAsDoubles(asc, fast);
        List<Double> emaSlow = calculateEmaAsDoubles(asc, slow);

        // MACD series starts at index (slow - 1) where emaSlow first has a value
        int macdStart = slow - 1;
        List<Double> macdSeries = new ArrayList<>();
        for (int i = macdStart; i < emaFast.size(); i++) {
            macdSeries.add(emaFast.get(i) - emaSlow.get(i - macdStart));
        }

        List<Double> signalSeries = calculateEmaAsDoubles(macdSeries, signal);

        double lastMacdLine = macdSeries.get(macdSeries.size() - 1);
        double lastSignal = signalSeries.get(signalSeries.size() - 1);
        double histogram = lastMacdLine - lastSignal;

        return new BigDecimal[] {
                BigDecimal.valueOf(lastMacdLine).setScale(SMA_SCALE, RoundingMode.HALF_UP),
                BigDecimal.valueOf(lastSignal).setScale(SMA_SCALE, RoundingMode.HALF_UP),
                BigDecimal.valueOf(histogram).setScale(SMA_SCALE, RoundingMode.HALF_UP)
        };
    }

    /**
     * Computes the full EMA series as a {@code List<Double>} over the provided
     * prices (already sorted ascending — oldest first).
     *
     * <p>The returned list has length {@code prices.size() - period + 1}: the first
     * element corresponds to the seed SMA, and subsequent elements are the EMA
     * values for every additional price.
     *
     * @param ascPrices prices sorted ascending (oldest first)
     * @param period    EMA period
     * @return full EMA series as doubles
     */
    private List<Double> calculateEmaAsDoubles(List<Double> ascPrices, int period) {
        double seed = ascPrices.stream()
                .limit(period)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double multiplier = 2.0 / (period + 1);
        List<Double> emaSeries = new ArrayList<>();
        emaSeries.add(seed);

        for (int i = period; i < ascPrices.size(); i++) {
            double prev = emaSeries.get(emaSeries.size() - 1);
            emaSeries.add((ascPrices.get(i) - prev) * multiplier + prev);
        }

        return emaSeries;
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