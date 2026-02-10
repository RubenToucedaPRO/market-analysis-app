package com.market.analysis.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.market.analysis.domain.model.Stock;

public interface StockDataRepository {

    public void saveTickerData(Stock tickerData);

    public List<Stock> findAllTickers();

    public Optional<Stock> findByTicker(String ticker);

    public void updateTickerData(Stock tickerData);

    public void deleteByTicker(String ticker);

}
