package com.market.analysis.application.usecase;

import java.util.List;

import com.market.analysis.application.dto.ProhibitedTickerDTO;
import com.market.analysis.application.mapper.ProhibitedTickerDTOMapper;
import com.market.analysis.domain.model.PageResult;
import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.domain.port.in.ManageProhibitedTickerUseCase;
import com.market.analysis.domain.port.out.ProhibitedTickerRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service implementing prohibited ticker management use cases.
 * Coordinates operations on prohibited tickers through the repository port.
 */
@RequiredArgsConstructor
@Slf4j
public class ManageProhibitedTickerService implements ManageProhibitedTickerUseCase {

    private final ProhibitedTickerRepository prohibitedTickerRepository;
    private final ProhibitedTickerDTOMapper prohibitedTickerMapper;

    @Override
    public PageResult<ProhibitedTickerDTO> getProhibitedTickers(int pageNumber, int pageSize) {
        PageResult<ProhibitedTicker> page = prohibitedTickerRepository.findAll(pageNumber, pageSize);
        List<ProhibitedTickerDTO> dtos = page.content().stream()
                .map(prohibitedTickerMapper::toDTO)
                .toList();
        return new PageResult<>(dtos, page.pageNumber(), page.pageSize(),
                page.totalElements(), page.totalPages());
    }

    @Override
    public boolean isTickerProhibited(String ticker) {
        return prohibitedTickerRepository.existsByTicker(ticker);
    }

    @Override
    public void addProhibitedTicker(ProhibitedTickerDTO ticker) {
        log.info("Adding prohibited ticker: {}", ticker.getTicker());
        ProhibitedTicker stock = prohibitedTickerMapper.toDomain(ticker);
        prohibitedTickerRepository.save(stock);
        log.info("Prohibited ticker added successfully: {}", ticker.getTicker());
    }

    @Override
    @Transactional
    public void removeProhibitedTicker(String ticker) {
        log.info("Removing prohibited ticker: {}", ticker);
        prohibitedTickerRepository.deleteByTicker(ticker);
        log.info("Prohibited ticker removed successfully: {}", ticker);
    }

}
