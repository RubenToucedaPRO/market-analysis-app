package com.market.analysis.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.market.analysis.domain.model.Stock;

public interface StockDataRepository {

    public Stock save(Stock stockData);

    public List<Stock> findAllStocks();

    public Optional<Stock> findById(Long id);

    public void updateStockData(Stock stockData);

    public void deleteById(Long id);

}
