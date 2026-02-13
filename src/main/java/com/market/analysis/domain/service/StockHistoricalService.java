package com.market.analysis.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.HistoricalData;
import com.market.analysis.domain.model.TechnicalIndicators;

@Component
public class StockHistoricalService {

    public TechnicalIndicators calculateIndicators(HistoricalData data, int volumePeriod) {
        BigDecimal sma20 = calculateSma(data.getClosingPrices(), 20);
        BigDecimal sma50 = calculateSma(data.getClosingPrices(), 50);
        BigDecimal sma200 = calculateSma(data.getClosingPrices(), 200);

        Long currentVolume = data.getVolumes().isEmpty() ? null : data.getVolumes().get(0);
        Long avgVolume = calculateAverageVolume(data.getVolumes(), volumePeriod);

        return TechnicalIndicators.builder()
                .sma20(sma20)
                .sma50(sma50)
                .sma200(sma200)
                .currentVolume(currentVolume)
                .averageVolume(avgVolume)
                .build();
    }

    private BigDecimal calculateSma(List<Double> prices, int period) {
        if (prices == null || prices.size() < period)
            return null;

        double sum = prices.stream()
                .limit(period)
                .mapToDouble(Double::doubleValue)
                .sum();

        return BigDecimal.valueOf(sum / period)
                .setScale(2, RoundingMode.HALF_UP);
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