package com.market.analysis.domain.port.out;

import java.time.Instant;

public interface ApiCallRateRepository {

    void save(String ticker, Instant timestamp);

}
