package com.market.analysis.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.ApiCallLog;
import com.market.analysis.domain.port.out.ApiCallRateRepository;
import com.market.analysis.infrastructure.exception.PersistenceException;
import com.market.analysis.infrastructure.persistence.entity.ApiCallLogEntity;
import com.market.analysis.infrastructure.persistence.mapper.ApiCallLogMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqlApiCallRateRepository implements ApiCallRateRepository {

    private final JpaApiCallRateRepository jpaRepository;
    private final ApiCallLogMapper mapper;

    @Override
    public Optional<ApiCallLog> findByTicker(String ticker) {
        try {
            ApiCallLogEntity entity = jpaRepository.findByTicker(ticker);
            return Optional.ofNullable(mapper.toDomain(entity));
        } catch (DataAccessException e) {
            log.error("Database error finding API call log for ticker: {}", ticker, e);
            throw new PersistenceException("Error retrieving API call log for ticker: " + ticker, e);
        }
    }

    @Override
    public void save(String ticker, Instant timestamp) {
        try {
            ApiCallLogEntity entity = mapper.toEntity(ticker, timestamp.toString());
            jpaRepository.save(entity);
        } catch (DataAccessException e) {
            log.error("Database error saving API call log for ticker: {}", ticker, e);
            throw new PersistenceException("Error saving API call log for ticker: " + ticker, e);
        }
    }

    @Override
    public void deleteByTicker(String ticker) {
        try {
            jpaRepository.deleteByTicker(ticker);
        } catch (DataAccessException e) {
            log.error("Database error deleting API call log for ticker: {}", ticker, e);
            throw new PersistenceException("Error deleting API call log for ticker: " + ticker, e);
        }
    }

}
