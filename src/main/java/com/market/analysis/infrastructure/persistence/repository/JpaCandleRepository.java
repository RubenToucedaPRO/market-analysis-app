package com.market.analysis.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
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
     * Deletes all candles for a given ticker.
     *
     * @param ticker the ticker symbol
     */
    void deleteByTicker(String ticker);

    /**
     * Checks whether any candle exists for the given ticker.
     *
     * @param ticker the ticker symbol
     * @return true if at least one candle exists, false otherwise
     */
    boolean existsByTicker(String ticker);

}
