package com.market.analysis.domain.port.in;

import java.util.List;

import com.market.analysis.domain.model.Stock;

public interface ManageAnalyzeTickerUseCase {

        public void getTickerData(String ticker);

        public List<Stock> findAllTickers();

        public Stock findTickerDataByTicker(String ticker);

        public void updateTickerData(Stock tickerData);

        public void deleteTickerDataByTicker(String ticker);

}
