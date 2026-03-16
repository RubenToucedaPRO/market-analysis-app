package com.market.analysis.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.market.analysis.infrastructure.persistence.entity.CandleEntity;

/**
 * Spring Data JPA repository for CandleEntity.
 * Provides OHLCV candle data access operations by ticker.
 */
@Repository
public interface JpaCandleRepository extends JpaRepository<CandleEntity, Long> {

    /**
     * Finds all candles for a given ticker ordered by date ascending.
     *
     * @param ticker the ticker symbol
     * @return list of candle entities ordered by dateTime ascending
     */
    List<CandleEntity> findByTickerOrderByDateTimeAsc(String ticker);

    /**
     * Bulk-deletes all candles for a given ticker using a JPQL statement.
     *
     * <p>Using {@code @Modifying} with {@code clearAutomatically = true} ensures
     * that (a) the DELETE is sent to the database immediately rather than being
     * queued after pending INSERT/UPDATE actions, and (b) the first-level cache
     * is cleared afterwards so subsequent reads and writes are consistent.</p>
     *
     * @param ticker the ticker symbol
     */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM CandleEntity c WHERE c.ticker = :ticker")
    void deleteByTicker(@Param("ticker") String ticker);

    /**
     * Checks whether any candle exists for the given ticker.
     *
     * @param ticker the ticker symbol
     * @return true if at least one candle exists, false otherwise
     */
    boolean existsByTicker(String ticker);

}
