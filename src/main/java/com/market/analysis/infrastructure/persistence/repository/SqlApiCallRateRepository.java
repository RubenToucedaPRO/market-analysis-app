package com.market.analysis.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.ApiCallLog;
import com.market.analysis.domain.port.out.ApiCallRateRepository;
import com.market.analysis.infrastructure.persistence.entity.ApiCallLogEntity;
import com.market.analysis.infrastructure.persistence.mapper.ApiCallLogMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SqlApiCallRateRepository implements ApiCallRateRepository {

    private final JpaApiCallRateRepository jpaRepository;
    private final ApiCallLogMapper mapper;

    @Override
    public Optional<ApiCallLog> findByTicker(String ticker) {
        ApiCallLogEntity entity = jpaRepository.findByTicker(ticker);
        return Optional.ofNullable(mapper.toDomain(entity));
    }

    @Override
    public void save(String ticker, LocalDate timestamp) {
        ApiCallLogEntity entity = mapper.toEntity(ticker, timestamp.toString());
        jpaRepository.save(entity);
    }

    @Override
    public void deleteByTicker(String ticker) {
        jpaRepository.deleteByTicker(ticker);
    }

}
