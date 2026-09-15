package com.market.analysis.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.market.analysis.domain.model.PageResult;
import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.domain.port.out.ProhibitedTickerRepository;
import com.market.analysis.infrastructure.persistence.entity.ProhibitedTickerEntity;
import com.market.analysis.infrastructure.persistence.mapper.ProhibitedTickerMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqlProhibitedTickerRepository implements ProhibitedTickerRepository {

    private final JpaProhibitedTickerRepository jpaProhibitedTickerRepository;
    private final ProhibitedTickerMapper prohibitedTickerMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProhibitedTicker> findAll() {
        log.debug("Retrieving all prohibited tickers");
        return jpaProhibitedTickerRepository.findAll().stream()
                .map(prohibitedTickerMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ProhibitedTicker> findAll(int pageNumber, int pageSize) {
        log.debug("Retrieving prohibited tickers page {} size {}", pageNumber, pageSize);
        Page<ProhibitedTickerEntity> page = jpaProhibitedTickerRepository
                .findAll(PageRequest.of(pageNumber, pageSize));
        List<ProhibitedTicker> content = page.getContent().stream()
                .map(prohibitedTickerMapper::toDomain)
                .toList();
        return new PageResult<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTicker(String ticker) {
        log.debug("Checking if ticker is prohibited: {}", ticker);
        return jpaProhibitedTickerRepository.existsByTicker(ticker);
    }

    @Override
    public void save(ProhibitedTicker ticker) {
        log.debug("Saving prohibited ticker: {}", ticker.getTicker());
        if (!jpaProhibitedTickerRepository.existsByTicker(ticker.getTicker())) {
            ProhibitedTickerEntity entity = prohibitedTickerMapper.toEntity(ticker);
            jpaProhibitedTickerRepository.save(entity);
            log.debug("Prohibited ticker saved successfully: {}", ticker.getTicker());
        } else {
            log.debug("Prohibited ticker already exists, skipping save: {}", ticker.getTicker());
        }
    }

    @Override
    public void deleteByTicker(String ticker) {
        log.debug("Deleting prohibited ticker: {}", ticker);
        jpaProhibitedTickerRepository.deleteByTicker(ticker);
        log.debug("Prohibited ticker deleted successfully: {}", ticker);
    }

}
