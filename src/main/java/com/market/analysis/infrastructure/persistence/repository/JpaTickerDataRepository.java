package com.market.analysis.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.market.analysis.infrastructure.persistence.entity.TickerEntity;

@Repository
public interface JpaTickerDataRepository extends JpaRepository<TickerEntity, Long> {

    Optional<TickerEntity> findByTicker(String ticker);

    void deleteByTicker(String ticker);
}