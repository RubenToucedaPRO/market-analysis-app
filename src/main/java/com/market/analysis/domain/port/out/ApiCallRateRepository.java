package com.market.analysis.domain.port.out;

import java.time.LocalDate;
import java.util.Optional;

import com.market.analysis.domain.model.ApiCallLog;

public interface ApiCallRateRepository {

    Optional<ApiCallLog> findByTicker(String ticker);

    void save(String ticker, LocalDate timestamp);

    void deleteByTicker(String ticker);

}
