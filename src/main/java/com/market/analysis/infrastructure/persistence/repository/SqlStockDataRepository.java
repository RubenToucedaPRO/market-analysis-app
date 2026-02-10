package com.market.analysis.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.infrastructure.persistence.entity.StockEntity;
import com.market.analysis.infrastructure.persistence.mapper.StockMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SqlStockDataRepository implements StockDataRepository {

    private final JpaStockDataRepository jpaRepository;
    private final StockMapper mapper;

    @Override
    public void saveTickerData(Stock tickerData) {
        Optional<StockEntity> existingEntity = jpaRepository.findByTicker(tickerData.getTicker());
        if (existingEntity.isPresent()) {
            StockEntity entity = mapper.toEntity(tickerData);
            entity.setId(existingEntity.get().getId());
            jpaRepository.save(entity);
        } else {
            jpaRepository.save(mapper.toEntity(tickerData));
        }
    }

    @Override
    public List<Stock> findAllTickers() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Stock> findByTicker(String ticker) {
        return jpaRepository.findByTicker(ticker).map(mapper::toDomain);
    }

    @Override
    public void updateTickerData(Stock tickerData) {
        jpaRepository.save(mapper.toEntity(tickerData));
    }

    @Override
    @Transactional
    public void deleteByTicker(String ticker) {
        jpaRepository.deleteByTicker(ticker);
    }

}
