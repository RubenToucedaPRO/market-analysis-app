package com.market.analysis.domain.port.in;

import java.util.List;

import com.market.analysis.application.dto.StockDataDTO;

public interface ManageAnalyzeTickerUseCase {

        public void getStockData(String ticker, Long strategyId);

        public List<StockDataDTO> findAllStocks();

        public StockDataDTO findStockDataByTicker(String ticker);

        public void updateStockData(String ticker);

        public void deleteStockDataByTicker(String ticker);

}
