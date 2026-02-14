package com.market.analysis.infrastructure.persistence.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.infrastructure.persistence.entity.CompanyProfileEntity;
import com.market.analysis.infrastructure.persistence.mapper.StockMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SqlStockDataRepository implements StockDataRepository {

    private final JpaStockDataRepository jpaRepository;
    private final JpaCompanyProfileRepository companyProfileRepository;
    private final StockMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Stock save(Stock stockData) {
        Optional<CompanyProfileEntity> profile = companyProfileRepository.findByTicker(stockData.getTicker());
        var entity = mapper.toEntity(stockData);
        if (profile.isPresent()) {
            entity.setCompanyProfile(profile.get());
        }
        return mapper.toDomain(jpaRepository.save(entity));

    }

    @Override
    @Transactional(readOnly = true)
    public List<Stock> findAllStocks() {
        return jpaRepository.findAllWithProfile().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Query("SELECT s FROM StockEntity s LEFT JOIN FETCH s.companyProfile WHERE s.id = :id")
    public Optional<Stock> findById(@Param("id") Long id) {
        return jpaRepository.findByIdWithProfile(id).map(mapper::toDomain);

    }

    @Override
    @Transactional(readOnly = true)
    public Stock findByTickerAndLastUpdateBetween(String ticker, Instant date, Instant endDate) {
        return mapper.toDomain(jpaRepository.findFirstByTickerAndLastUpdateBetween(ticker, date, endDate));
    }

    @Override
    public void updateStockData(Stock stockData) {
        var entity = mapper.toEntity(stockData);
        jpaRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

}
