package com.market.analysis.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.market.analysis.domain.model.StockOrigin;
import com.market.analysis.infrastructure.persistence.entity.StockEntity;

@Repository
public interface JpaStockDataRepository extends JpaRepository<StockEntity, Long> {

    @Query("SELECT s FROM StockEntity s " +
            "LEFT JOIN FETCH s.companyProfile " +
            "LEFT JOIN FETCH s.strategyEvaluation")
    List<StockEntity> findAllWithProfile();

        @Query("SELECT s FROM StockEntity s " +
            "LEFT JOIN FETCH s.companyProfile " +
            "LEFT JOIN FETCH s.strategyEvaluation " +
            "WHERE s.origin IS NULL OR s.origin <> :excludedOrigin")
        List<StockEntity> findAllVisibleInAnalysis(@Param("excludedOrigin") StockOrigin excludedOrigin);

    @Query("SELECT s FROM StockEntity s LEFT JOIN FETCH s.companyProfile WHERE s.id = :id")
    Optional<StockEntity> findByIdWithProfile(@Param("id") Long id);

    StockEntity findFirstByTickerAndLastUpdateBetween(String ticker, Instant start, Instant end);

    @Query("SELECT s FROM StockEntity s LEFT JOIN FETCH s.companyProfile WHERE s.strategyId = :strategyId")
    List<StockEntity> findAllByStrategyId(@Param("strategyId") Long strategyId);

    boolean existsByTicker(String ticker);
}