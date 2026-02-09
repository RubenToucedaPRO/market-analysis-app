package com.market.analysis.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.TickerData;
import com.market.analysis.domain.port.out.TickerDataRepository;
import com.market.analysis.infrastructure.persistence.mapper.TickerMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SqlTickerDataRepository implements TickerDataRepository {

    private final JpaTickerDataRepository jpaRepository;
    private final TickerMapper mapper;

    @Override
    public void saveTickerData(TickerData tickerData) {
        jpaRepository.save(mapper.toEntity(tickerData));
    }

    @Override
    public List<TickerData> findAllTickers() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<TickerData> findByTicker(String ticker) {
        return jpaRepository.findByTicker(ticker).map(mapper::toDomain);
    }

    @Override
    public void updateTickerData(TickerData tickerData) {
        jpaRepository.save(mapper.toEntity(tickerData));
    }

    @Override
    @Transactional
    public void deleteByTicker(String ticker) {
        jpaRepository.deleteByTicker(ticker);
    }

}
