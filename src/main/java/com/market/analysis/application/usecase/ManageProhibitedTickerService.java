package com.market.analysis.application.usecase;

import java.util.List;

import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.domain.port.in.ManageProhibitedTickerUseCase;
import com.market.analysis.domain.port.out.ProhibitedTickerRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service implementing prohibited ticker management use cases.
 * Coordinates operations on prohibited tickers through the repository port.
 */
@RequiredArgsConstructor
public class ManageProhibitedTickerService implements ManageProhibitedTickerUseCase {

    private final ProhibitedTickerRepository prohibitedTickerRepository;

    @Override
    public List<ProhibitedTicker> getAllProhibitedTickers() {
        return prohibitedTickerRepository.findAll();
    }

    @Override
    public boolean isTickerProhibited(String ticker) {
        return prohibitedTickerRepository.existsByTicker(ticker);
    }

    @Override
    public void addProhibitedTicker(ProhibitedTicker ticker) {
        prohibitedTickerRepository.save(ticker);
    }

    @Override
    public void removeProhibitedTicker(Long id) {
        prohibitedTickerRepository.deleteById(id);
    }

}
