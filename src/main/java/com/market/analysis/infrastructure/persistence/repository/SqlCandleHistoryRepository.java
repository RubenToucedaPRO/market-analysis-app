package com.market.analysis.infrastructure.persistence.repository;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.market.analysis.domain.model.Candle;
import com.market.analysis.infrastructure.persistence.entity.CandleEntity;
import com.market.analysis.infrastructure.persistence.mapper.CandleMapper;

import lombok.RequiredArgsConstructor;

/**
 * Infrastructure component responsible for persisting OHLCV candle data.
 * Encapsulates bulk save operations, keeping HTTP and JPA concerns separated
 * from PolygonAdapter.
 */
@Component
@RequiredArgsConstructor
public class SqlCandleHistoryRepository {

    private static final Logger log = LoggerFactory.getLogger(SqlCandleHistoryRepository.class);

    private final JpaCandleRepository jpaCandleRepository;
    private final CandleMapper candleMapper;

    /**
     * Replaces the full set of candles for a given ticker in a single transaction.
     *
     * <p>The strategy is delete-then-insert: all existing candles for the ticker
     * are deleted and the new batch is inserted atomically. If the provided list
     * is {@code null} or empty the method is a no-op — no data is removed or
     * written.</p>
     *
     * @param ticker  the ticker symbol (must not be {@code null})
     * @param candles the candles to persist; a {@code null} or empty list causes
     *                the method to return immediately without modifying the database
     */
    @Transactional
    public void saveCandlesForTicker(String ticker, List<Candle> candles) {
        Assert.hasText(ticker, "ticker must not be null or blank");

        if (candles == null || candles.isEmpty()) {
            log.debug("saveCandlesForTicker: skipping persistence for ticker={} — candle list is null or empty", ticker);
            return;
        }

        log.debug("saveCandlesForTicker: replacing {} candle(s) for ticker={}", candles.size(), ticker);

        jpaCandleRepository.deleteByTicker(ticker);

        List<CandleEntity> entities = candles.stream()
                .map(candleMapper::toEntity)
                .toList();

        jpaCandleRepository.saveAll(entities);

        log.info("saveCandlesForTicker: persisted {} candle(s) for ticker={}", entities.size(), ticker);
    }
}
