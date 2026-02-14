package com.market.analysis.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.domain.port.out.ProhibitedTickerRepository;
import com.market.analysis.infrastructure.exception.PersistenceException;
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
    public List<ProhibitedTicker> findAll() {
        try {
            return jpaProhibitedTickerRepository.findAll().stream()
                    .map(prohibitedTickerMapper::toDomain)
                    .toList();
        } catch (DataAccessException e) {
            log.error("Database error finding all prohibited tickers", e);
            throw new PersistenceException("Error finding all prohibited tickers", e);
        }
    }

    @Override
    public boolean existsByTicker(String ticker) {
        try {
            return jpaProhibitedTickerRepository.existsByTicker(ticker);
        } catch (DataAccessException e) {
            log.error("Database error checking if prohibited ticker exists: {}", ticker, e);
            throw new PersistenceException("Error checking if prohibited ticker exists: " + ticker, e);
        }
    }

    @Override
    public void save(ProhibitedTicker ticker) {
        try {
            if (!jpaProhibitedTickerRepository.existsByTicker(ticker.getTicker())) {
                ProhibitedTickerEntity entity = prohibitedTickerMapper.toEntity(ticker);
                jpaProhibitedTickerRepository.save(entity);
            }
        } catch (DataAccessException e) {
            log.error("Database error saving prohibited ticker: {}", ticker.getTicker(), e);
            throw new PersistenceException("Error saving prohibited ticker: " + ticker.getTicker(), e);
        }
    }

    @Override
    public void deleteByTicker(String ticker) {
        try {
            jpaProhibitedTickerRepository.deleteByTicker(ticker);
        } catch (DataAccessException e) {
            log.error("Database error deleting prohibited ticker: {}", ticker, e);
            throw new PersistenceException("Error deleting prohibited ticker: " + ticker, e);
        }
    }

}
