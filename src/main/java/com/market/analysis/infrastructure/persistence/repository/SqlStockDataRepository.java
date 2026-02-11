package com.market.analysis.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.infrastructure.persistence.entity.StockEntity;
import com.market.analysis.infrastructure.persistence.mapper.StockMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SqlStockDataRepository implements StockDataRepository {

    private final JpaStockDataRepository jpaRepository;
    private final JpaCompanyProfileRepository jpaCompanyProfileRepository;
    private final StockMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<Stock> findAllStocks() {
        return jpaRepository.findAllWithProfile().stream()
                .map(entity -> {
                    Stock domain = mapper.toDomain(entity);
                    if (entity.getCompanyProfile() != null) {
                        domain.setLogoUrl(entity.getCompanyProfile().getLogo());
                    }
                    return domain;
                })
                .toList();
    }

    @Override
    @Transactional
    public void saveStockData(Stock stock) {
        Optional<StockEntity> existingEntity = jpaRepository.findByTicker(stock.getTicker());
        if (existingEntity.isPresent()) {
            StockEntity entity = mapper.toEntity(stock);
            entity.setId(existingEntity.get().getId());
            entity.setCompanyProfile(existingEntity.get().getCompanyProfile());
            jpaRepository.save(entity);
        } else {
            StockEntity newEntity = mapper.toEntity(stock);
            jpaCompanyProfileRepository.findByTicker(stock.getTicker()).ifPresent(newEntity::setCompanyProfile);
            jpaRepository.save(newEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Stock> findByTicker(String ticker) {
        return jpaRepository.findByTicker(ticker).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void updateStockData(Stock stock) {
        jpaRepository.findByTicker(stock.getTicker())
                .ifPresent(existingEntity -> mapper.toEntity(stock));
    }

    @Override
    @Transactional
    public void deleteByTicker(String ticker) {
        jpaRepository.deleteByTicker(ticker);
    }

}
