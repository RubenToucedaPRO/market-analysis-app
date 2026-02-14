package com.market.analysis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.market.analysis.infrastructure.persistence.entity.ApiCallLogEntity;

@Repository
public interface JpaApiCallRateRepository extends JpaRepository<ApiCallLogEntity, Long> {

    ApiCallLogEntity findByTicker(String ticker);

    void deleteByTicker(String ticker);

}
