package com.market.analysis.domain.port.out;

import java.util.List;

import com.market.analysis.domain.model.Candle;

/**
 * Output port for persisting and querying OHLCV candle history.
 * Implemented by infrastructure adapters (e.g. SQL).
 */
public interface CandleHistoryRepository {

    /**
     * Replaces the full set of candles for the given ticker.
     *
     * @param ticker  the ticker symbol (must not be blank)
     * @param candles the candles to persist; a null or empty list is a no-op
     */
    void saveCandlesForTicker(String ticker, List<Candle> candles);

    /**
     * Deletes all candles associated with the given ticker.
     *
     * @param ticker the ticker symbol (must not be blank)
     */
    void deleteCandlesByTicker(String ticker);

    /**
     * Returns all candles for the given ticker ordered by date ascending.
     *
     * @param ticker the ticker symbol (must not be blank)
     * @return ordered list of candles; empty list if none found
     */
    List<Candle> findCandlesByTicker(String ticker);
}
