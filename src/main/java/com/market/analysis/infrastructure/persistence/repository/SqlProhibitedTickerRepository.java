package com.market.analysis.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.domain.port.out.ProhibitedTickerRepository;
import com.market.analysis.infrastructure.persistence.entity.ProhibitedTickerEntity;
import com.market.analysis.infrastructure.persistence.mapper.ProhibitedTickerMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SqlProhibitedTickerRepository implements ProhibitedTickerRepository {

    JpaProhibitedTickerRepository jpaProhibitedTickerRepository;
    ProhibitedTickerMapper prohibitedTickerMapper;

    @Override
    public List<ProhibitedTicker> findAll() {
        return jpaProhibitedTickerRepository.findAll().stream()
                .map(prohibitedTickerMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByTicker(String ticker) {
        return jpaProhibitedTickerRepository.existsByTicker(ticker);
    }

    @Override
    public ProhibitedTicker save(ProhibitedTicker ticker) {
        ProhibitedTickerEntity entity = prohibitedTickerMapper.toEntity(ticker);
        return prohibitedTickerMapper.toDomain(jpaProhibitedTickerRepository.save(entity));
    }

    @Override
    public void deleteById(Long id) {
        jpaProhibitedTickerRepository.deleteById(id);
    }

}
