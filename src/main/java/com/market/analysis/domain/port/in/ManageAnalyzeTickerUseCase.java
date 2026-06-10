package com.market.analysis.domain.port.in;

import java.util.List;

import com.market.analysis.application.dto.CandleChartDTO;
import com.market.analysis.application.dto.StockDataDTO;

public interface ManageAnalyzeTickerUseCase {

        public void getStockData(String ticker, Long strategyId);

        public List<StockDataDTO> findAllStocks();

        public StockDataDTO findStockDataById(Long id);

        public void updateStockData(Long id);

        public void deleteById(Long id, String ticker);

        public boolean getValorationIA(Long id);

        public CandleChartDTO findCandlesByStockId(Long id);

}
