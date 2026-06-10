package com.market.analysis.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StockOrigin;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.infrastructure.persistence.entity.CompanyProfileEntity;
import com.market.analysis.infrastructure.persistence.mapper.StockMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqlStockDataRepository implements StockDataRepository {

    private final JpaStockDataRepository jpaRepository;
    private final JpaCompanyProfileRepository companyProfileRepository;
    private final StockMapper mapper;

    @Override
    @Transactional
    public Stock save(Stock stockData) {
        log.debug("Saving stock data for ticker: {}", stockData.getTicker());
        Optional<CompanyProfileEntity> profile = companyProfileRepository.findByTicker(stockData.getTicker());
        var entity = mapper.toEntity(stockData);
        if (profile.isPresent()) {
            entity.setCompanyProfile(profile.get());
        }
        Stock savedStock = mapper.toDomain(jpaRepository.save(entity));
        log.debug("Stock data saved successfully for ticker: {}", savedStock.getTicker());
        return savedStock;

    }

    @Override
    @Transactional(readOnly = true)
    public List<Stock> findAllStocks() {
        log.debug("Retrieving all stock data");
        return jpaRepository.findAllWithProfile().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> findTickerByStrategyId(Long strategyId) {
        return jpaRepository.findTickerByStrategyId(strategyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Stock> findAllStocksVisibleInAnalysis() {
        log.debug("Retrieving stock data visible in analysis");
        return jpaRepository.findAllVisibleInAnalysis(StockOrigin.ANALYSIS).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Query("SELECT s FROM StockEntity s LEFT JOIN FETCH s.companyProfile WHERE s.id = :id")
    public Optional<Stock> findById(@Param("id") Long id) {
        return jpaRepository.findByIdWithProfile(id).map(mapper::toDomain);

    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByTicker(String ticker) {
        return jpaRepository.existsByTicker(ticker);
    }

    @Override
    @Transactional(readOnly = true)
    public Stock findByTickerAndLastUpdateBetween(String ticker, Instant date, Instant endDate) {
        return mapper.toDomain(jpaRepository.findFirstByTickerAndLastUpdateBetween(ticker, date, endDate));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Stock> findAllByStrategyId(Long strategyId) {
        return jpaRepository.findAllByStrategyId(strategyId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void updateStockData(Stock stockData) {
        var entity = mapper.toEntity(stockData);
        Optional<CompanyProfileEntity> profile = companyProfileRepository.findByTicker(stockData.getTicker());
        if (profile.isPresent()) {
            entity.setCompanyProfile(profile.get());
        }
        entity.getStrategyEvaluation().setStock(entity);
        jpaRepository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAllByStrategyIdAndOrigin(Long strategyId, StockOrigin origin) {
        jpaRepository.deleteAllByStrategyIdAndOrigin(strategyId, origin);
    }

}
