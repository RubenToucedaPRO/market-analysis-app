package com.market.analysis.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.market.analysis.infrastructure.persistence.entity.StockEntity;

@Repository
public interface JpaStockDataRepository extends JpaRepository<StockEntity, Long> {

    @Query("SELECT s FROM StockEntity s " +
            "LEFT JOIN FETCH s.companyProfile " +
            "LEFT JOIN FETCH s.strategyEvaluation")
    List<StockEntity> findAllWithProfile();

    @Query("SELECT s FROM StockEntity s LEFT JOIN FETCH s.companyProfile WHERE s.id = :id")
    Optional<StockEntity> findByIdWithProfile(@Param("id") Long id);

    @Query("SELECT s FROM StockEntity s WHERE s.ticker = :ticker AND s.lastUpdated = :date")
    StockEntity findByTickerAndDate(@Param("ticker") String ticker, @Param("date") LocalDate date);
}