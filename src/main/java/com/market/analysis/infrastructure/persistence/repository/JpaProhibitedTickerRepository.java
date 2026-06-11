package com.market.analysis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.market.analysis.infrastructure.persistence.entity.ProhibitedTickerEntity;

/**
 * Spring Data JPA repository for ProhibitedTickerEntity.
 * Provides standard CRUD operations through JPA.
 */
public interface JpaProhibitedTickerRepository extends JpaRepository<ProhibitedTickerEntity, Long> {

    boolean existsByTicker(String ticker);

    void deleteByTicker(String ticker);

}
