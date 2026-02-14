package com.market.analysis.domain.port.out;

import java.time.Instant;
import java.util.Optional;

import com.market.analysis.domain.model.ApiCallLog;

public interface ApiCallRateRepository {

    Optional<ApiCallLog> findByTicker(String ticker);

    void save(String ticker, Instant timestamp);

    void deleteByTicker(String ticker);

}
