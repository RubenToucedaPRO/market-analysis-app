package com.market.analysis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.market.analysis.infrastructure.persistence.entity.ApiCallLogEntity;
public interface JpaApiCallRateRepository extends JpaRepository<ApiCallLogEntity, Long> {

    ApiCallLogEntity findByTicker(String ticker);

    void deleteByTicker(String ticker);

    int deleteByOcurredAtBefore(java.time.Instant threshold);

}
