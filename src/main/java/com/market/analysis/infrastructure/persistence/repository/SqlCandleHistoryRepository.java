package com.market.analysis.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.Candle;
import com.market.analysis.infrastructure.persistence.mapper.CandleMapper;

import lombok.RequiredArgsConstructor;

/**
 * Infrastructure component responsible for persisting OHLCV candle data.
 * Encapsulates bulk save operations, keeping HTTP and JPA concerns separated
 * from PolygonAdapter.
 *
 * <p>Transactional replace logic will be implemented in F1.5.</p>
 */
@Component
@RequiredArgsConstructor
public class SqlCandleHistoryRepository {

    private final JpaCandleRepository jpaCandleRepository;
    private final CandleMapper candleMapper;

    /**
     * Persists the full set of candles for a given ticker.
     * The transactional replace strategy (delete-then-insert) will be
     * implemented in F1.5.
     *
     * @param ticker  the ticker symbol
     * @param candles the list of candles to persist
     */
    public void saveCandlesForTicker(String ticker, List<Candle> candles) {
        // Body intentionally empty — transactional replace implemented in F1.5
    }
}
