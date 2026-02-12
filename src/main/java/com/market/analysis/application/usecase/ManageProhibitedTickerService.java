package com.market.analysis.application.usecase;

import java.util.List;

import com.market.analysis.application.dto.ProhibitedTickerDTO;
import com.market.analysis.application.mapper.ProhibitedTickerDTOMapper;
import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.domain.port.in.ManageProhibitedTickerUseCase;
import com.market.analysis.domain.port.out.ProhibitedTickerRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Service implementing prohibited ticker management use cases.
 * Coordinates operations on prohibited tickers through the repository port.
 */
@RequiredArgsConstructor
public class ManageProhibitedTickerService implements ManageProhibitedTickerUseCase {

    private final ProhibitedTickerRepository prohibitedTickerRepository;
    private final ProhibitedTickerDTOMapper prohibitedTickerMapper;

    @Override
    public List<ProhibitedTickerDTO> getAllProhibitedTickers() {
        return prohibitedTickerRepository.findAll().stream() .map(prohibitedTickerMapper::toDTO) .toList();
    }

    @Override
    public boolean isTickerProhibited(String ticker) {
        return prohibitedTickerRepository.existsByTicker(ticker);
    }

    @Override
    public void addProhibitedTicker(ProhibitedTickerDTO ticker) {
        ProhibitedTicker stock = prohibitedTickerMapper.toDomain(ticker);
        prohibitedTickerRepository.save(stock);
    }

    @Override
    @Transactional
    public void removeProhibitedTicker(String ticker) {
        prohibitedTickerRepository.deleteByTicker(ticker);
    }

}
