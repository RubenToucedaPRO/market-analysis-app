package com.market.analysis.application.usecase;

import java.util.List;

import com.market.analysis.domain.exception.TickerDataNotFoundException;
import com.market.analysis.domain.model.TickerData;
import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;
import com.market.analysis.domain.port.out.TickerDataRepository;
import com.market.analysis.infrastructure.external.finnhub.FinnhubAdapter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ManageAnalyzeTickerService implements ManageAnalyzeTickerUseCase {

    private final TickerDataRepository tickerDataRepository;
    private final FinnhubAdapter finnhubAdapter;

    @Override
    public void getTickerData(String tickers) {
        List<String> tickerList = List.of(tickers.split(",")).stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(t -> !t.isEmpty())
                .toList();
        tickerList.forEach(ticker -> tickerDataRepository.saveTickerData(finnhubAdapter.getQuote(ticker)));
    }

    @Override
    public List<TickerData> findAllTickers() {
        return tickerDataRepository.findAllTickers();
    }

    @Override
    public TickerData findTickerDataByTicker(String ticker) {
        return tickerDataRepository.findByTicker(ticker)
                .orElseThrow(() -> new TickerDataNotFoundException("Ticker data not found for: " + ticker));
    }

    @Override
    public void updateTickerData(TickerData tickerData) {
        tickerDataRepository.updateTickerData(tickerData);
    }

    @Override
    public void deleteTickerDataByTicker(String ticker) {
        tickerDataRepository.deleteByTicker(ticker);
    }

}
