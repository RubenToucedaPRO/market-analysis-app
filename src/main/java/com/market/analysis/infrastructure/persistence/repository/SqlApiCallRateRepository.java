package com.market.analysis.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.ApiCallLog;
import com.market.analysis.domain.port.out.ApiCallRateRepository;
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
        log.debug("Finding API call log for ticker: {}", ticker);
        ApiCallLogEntity entity = jpaRepository.findByTicker(ticker);
        return Optional.ofNullable(mapper.toDomain(entity));
    }

    @Override
    public void save(String ticker, Instant timestamp) {
        log.debug("Saving API call log for ticker: {}", ticker);
        ApiCallLogEntity entity = mapper.toEntity(ticker, timestamp.toString());
        jpaRepository.save(entity);
        log.debug("API call log saved successfully for ticker: {}", ticker);
    }

    @Override
    public void deleteByTicker(String ticker) {
        log.debug("Deleting API call log for ticker: {}", ticker);
        jpaRepository.deleteByTicker(ticker);
        log.debug("API call log deleted successfully for ticker: {}", ticker);
    }

}
