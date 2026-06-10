package com.market.analysis.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricalData {

    String ticker;

    List<Double> closingPrices;

    List<Long> volumes;

    Instant lastUpdate;

    /**
     * Full OHLCV candles extracted during the same parse pass as closingPrices /
     * volumes. Populated by the infrastructure adapter; persisted by the
     * Application Use Case. Defaults to an empty list when not populated.
     */
    @Builder.Default
    List<Candle> candles = new ArrayList<>();
}
