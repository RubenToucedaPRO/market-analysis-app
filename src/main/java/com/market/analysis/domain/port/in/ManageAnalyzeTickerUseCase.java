package com.market.analysis.domain.port.in;

import java.util.List;

import com.market.analysis.domain.model.TickerData;

public interface ManageAnalyzeTickerUseCase {

        public void getTickerData(String ticker);

        public List<TickerData> findAllTickers();

        public TickerData findTickerDataByTicker(String ticker);

        public void updateTickerData(TickerData tickerData);

        public void deleteTickerDataByTicker(String ticker);

}
