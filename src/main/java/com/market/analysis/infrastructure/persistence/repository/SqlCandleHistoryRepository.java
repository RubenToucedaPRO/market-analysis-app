package com.market.analysis.infrastructure.persistence.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.market.analysis.domain.model.Candle;
import com.market.analysis.domain.port.out.CandleHistoryRepository;
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
public class SqlCandleHistoryRepository implements CandleHistoryRepository {

    private static final Logger log = LoggerFactory.getLogger(SqlCandleHistoryRepository.class);

    private final JpaCandleRepository jpaCandleRepository;
    private final CandleMapper candleMapper;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Native bulk insert for candles. {@code saveAll} issues one INSERT per row
     * (IDENTITY keys disable Hibernate batching); with ~145ms of DB latency in
     * production that meant ~35s for 240 candles. A single JDBC batch needs one
     * round-trip instead. For maximum effect the JDBC URL should also carry
     * {@code rewriteBatchedStatements=true} so the MariaDB driver rewrites the
     * batch into one multi-row INSERT.
     */
    private static final String INSERT_CANDLE_SQL =
            "INSERT INTO candles (ticker, date_time, open_price, high_price, low_price, close_price, volume)"
                    + " VALUES (?,?,?,?,?,?,?)";

    /**
     * Replaces the full set of candles for a given ticker in a single transaction.
     *
     * <p>The strategy is delete-then-insert: all existing candles for the ticker
     * are deleted and the new batch is inserted atomically via a native JDBC
     * batch (one round-trip per batch instead of one INSERT per row). If the
     * provided list is {@code null} or empty the method is a no-op — no data is
     * removed or written.</p>
     *
     * @param ticker  the ticker symbol (must not be {@code null})
     * @param candles the candles to persist; a {@code null} or empty list causes
     *                the method to return immediately without modifying the database
     */
    @Transactional
    public void saveCandlesForTicker(String ticker, List<Candle> candles) {
        Assert.hasText(ticker, "ticker must not be null or blank");

        if (candles == null || candles.isEmpty()) {
            log.debug("saveCandlesForTicker: skipping persistence for ticker={} — candle list is null or empty",
                    ticker);
            return;
        }

        log.debug("saveCandlesForTicker: replacing {} candle(s) for ticker={}", candles.size(), ticker);

        jpaCandleRepository.deleteByTicker(ticker);

        List<Object[]> batchArgs = candles.stream()
                .map(candle -> new Object[] {
                        candle.getTicker(),
                        Timestamp.from(candle.getDateTime()),
                        candle.getOpenPrice(),
                        candle.getHighPrice(),
                        candle.getLowPrice(),
                        candle.getClosePrice(),
                        candle.getVolume()
                })
                .toList();

        jdbcTemplate.batchUpdate(INSERT_CANDLE_SQL, batchArgs);

        log.info("saveCandlesForTicker: persisted {} candle(s) for ticker={}", batchArgs.size(), ticker);
    }

    @Override
    @Transactional
    public void deleteCandlesByTicker(String ticker) {
        Assert.hasText(ticker, "ticker must not be null or blank");
        jpaCandleRepository.deleteByTicker(ticker);
    }

     @Override
     @Transactional(readOnly = true)
     public List<Candle> findCandlesByTicker(String ticker) {
        Assert.hasText(ticker, "ticker must not be null or blank");
        log.debug("findCandlesByTicker: querying candles for ticker={}", ticker);
        List<Candle> candles = jpaCandleRepository.findByTickerOrderByDateTimeAsc(ticker)
                .stream()
                .map(candleMapper::toDomain)
                .toList();
        log.debug("findCandlesByTicker: found {} candle(s) for ticker={}", candles.size(), ticker);
        return candles;
    }

     @Override
     @Transactional(readOnly = true)
     public Optional<Candle> findLatestCandleByTicker(String ticker) {
        Assert.hasText(ticker, "ticker must not be null or blank");
        log.debug("findLatestCandleByTicker: querying latest candle for ticker={}", ticker);
        CandleEntity entity = jpaCandleRepository.findTopByTickerOrderByDateTimeDesc(ticker);
        Optional<Candle> result = Optional.ofNullable(entity).map(candleMapper::toDomain);
        log.debug("findLatestCandleByTicker: found candle for ticker={}={}", ticker, result.isPresent());
        return result;
    }

    @Override
    @Transactional
    public void purgeOrphanCandles() {
        log.info("purgeOrphanCandles: deleting candles not associated with any ticker analysis");
        jpaCandleRepository.deleteOrphans();
    }
}
