package com.market.analysis.domain.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StockOrigin;

public interface StockDataRepository {

    public Stock save(Stock stockData);

    public List<Stock> findAllStocks();

    Set<String> findTickerByStrategyId(Long strategyId);

    public List<Stock> findAllStocksVisibleInAnalysis();

    public Optional<Stock> findById(Long id);

    public boolean existsByTicker(String ticker);

    public Stock findByTickerAndLastUpdateBetween(String ticker, Instant date, Instant endDate);

    public List<Stock> findAllByStrategyId(Long strategyId);

    public void updateStockData(Stock stockData);

    public void deleteById(Long id);

    public void deleteAllByStrategyIdAndOrigin(Long strategyId, StockOrigin origin);

}
