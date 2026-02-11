package com.market.analysis.domain.port.in;

import java.util.List;

import com.market.analysis.domain.model.Stock;

public interface ManageAnalyzeTickerUseCase {

        public void getStockData(String ticker, Long strategyId);

        public List<Stock> findAllStocks();

        public Stock findStockDataByTicker(String ticker);

        public void updateStockData(String ticker);

        public void deleteStockDataByTicker(String ticker);

}
