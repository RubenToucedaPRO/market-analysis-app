package com.market.analysis.domain.port.out;

import com.market.analysis.domain.model.HistoricalData;

public interface CandleHistoryRepository {

    HistoricalData fetchHistoricalData(String ticker);
}
