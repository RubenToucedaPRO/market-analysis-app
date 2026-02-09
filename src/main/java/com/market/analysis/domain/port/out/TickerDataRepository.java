package com.market.analysis.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.market.analysis.domain.model.TickerData;

public interface TickerDataRepository {

    public void saveTickerData(TickerData tickerData);

    public List<TickerData> findAllTickers();

    public Optional<TickerData> findByTicker(String ticker);

    public void updateTickerData(TickerData tickerData);

    public void deleteByTicker(String ticker);

}
