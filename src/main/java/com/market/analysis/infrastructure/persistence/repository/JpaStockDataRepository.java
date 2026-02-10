package com.market.analysis.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.market.analysis.infrastructure.persistence.entity.StockEntity;

@Repository
public interface JpaStockDataRepository extends JpaRepository<StockEntity, Long> {

    Optional<StockEntity> findByTicker(String ticker);

    void deleteByTicker(String ticker);
}