package com.market.analysis.application.usecase;

import java.util.List;

import com.market.analysis.domain.exception.StockDataNotFoundException;
import com.market.analysis.domain.model.CompanyProfile;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.port.in.ManageAnalyzeTickerUseCase;
import com.market.analysis.domain.port.out.CompanyProfileRepository;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.infrastructure.external.finnhub.FinnhubAdapter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ManageAnalyzeStockService implements ManageAnalyzeTickerUseCase {

    private final StockDataRepository tickerDataRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final FinnhubAdapter finnhubAdapter;

    @Override
    public void getTickerData(String tickers) {
        List<String> tickerList = parseTickers(tickers);
        checkCompanyProfile(tickerList);

        for (String ticker : tickerList) {
            Stock stock = finnhubAdapter.getQuote(ticker);
            if (stock != null) {
                tickerDataRepository.saveTickerData(stock);
            }
        }
    }

    @Override
    public List<Stock> findAllTickers() {
        return tickerDataRepository.findAllTickers();
    }

    @Override
    public Stock findTickerDataByTicker(String ticker) {
        return tickerDataRepository.findByTicker(ticker)
                .orElseThrow(() -> new StockDataNotFoundException("Ticker data not found for: " + ticker));
    }

    @Override
    public void updateTickerData(Stock tickerData) {
        tickerDataRepository.updateTickerData(tickerData);
    }

    @Override
    public void deleteTickerDataByTicker(String ticker) {
        tickerDataRepository.deleteByTicker(ticker);
    }

    /**
     * Parses a comma-separated string of tickers into a list of uppercase ticker
     * symbols.
     * 
     * @param tickers
     * @return
     */
    private List<String> parseTickers(String tickers) {
        return List.of(tickers.split(",")).stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(t -> !t.isEmpty())
                .toList();
    }

    /**
     * Checks if company profiles exist and are up-to-date for the given list of
     * tickers.
     * 
     * @param tickerList
     */
    private void checkCompanyProfile(List<String> tickerList) {
        List<String> companiesToUpdate = new java.util.ArrayList<>();

        for (String ticker : tickerList) {
            CompanyProfile companyProfile = companyProfileRepository.findByTicker(ticker);
            if (companyProfile == null) {
                companiesToUpdate.add(ticker);
            } else if (companyProfile.getLastUpdated() == null || companyProfile.isOutdated()) {
                companyProfileRepository.deleteByTicker(ticker);
                companiesToUpdate.add(ticker);
            }
        }

        companiesToUpdate.stream()
                .forEach(ticker -> {
                    CompanyProfile companyProfile = finnhubAdapter.getCompanyProfile(ticker);
                    if (companyProfile != null) {
                        companyProfileRepository.save(companyProfile);
                    }
                });
    }

}
