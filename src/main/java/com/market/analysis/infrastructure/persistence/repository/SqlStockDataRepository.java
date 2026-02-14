package com.market.analysis.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.infrastructure.exception.PersistenceException;
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
        try {
            Optional<CompanyProfileEntity> profile = companyProfileRepository.findByTicker(stockData.getTicker());
            var entity = mapper.toEntity(stockData);
            if (profile.isPresent()) {
                entity.setCompanyProfile(profile.get());
            }
            return mapper.toDomain(jpaRepository.save(entity));
        } catch (DataAccessException e) {
            log.error("Database error saving stock data for ticker: {}", stockData.getTicker(), e);
            throw new PersistenceException("Error saving stock data for ticker: " + stockData.getTicker(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Stock> findAllStocks() {
        try {
            return jpaRepository.findAllWithProfile().stream()
                    .map(mapper::toDomain)
                    .toList();
        } catch (DataAccessException e) {
            log.error("Database error finding all stocks", e);
            throw new PersistenceException("Error finding all stocks", e);
        }
    }

    @Query("SELECT s FROM StockEntity s LEFT JOIN FETCH s.companyProfile WHERE s.id = :id")
    public Optional<Stock> findById(@Param("id") Long id) {
        try {
            return jpaRepository.findByIdWithProfile(id).map(mapper::toDomain);
        } catch (DataAccessException e) {
            log.error("Database error finding stock by id: {}", id, e);
            throw new PersistenceException("Error finding stock by id: " + id, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Stock findByTickerAndLastUpdateBetween(String ticker, Instant date, Instant endDate) {
        try {
            return mapper.toDomain(jpaRepository.findFirstByTickerAndLastUpdateBetween(ticker, date, endDate));
        } catch (DataAccessException e) {
            log.error("Database error finding stock by ticker and date range: {}", ticker, e);
            throw new PersistenceException("Error finding stock for ticker: " + ticker, e);
        }
    }

    @Override
    public void updateStockData(Stock stockData) {
        try {
            var entity = mapper.toEntity(stockData);
            jpaRepository.save(entity);
        } catch (DataAccessException e) {
            log.error("Database error updating stock data for ticker: {}", stockData.getTicker(), e);
            throw new PersistenceException("Error updating stock data for ticker: " + stockData.getTicker(), e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            jpaRepository.deleteById(id);
        } catch (DataAccessException e) {
            log.error("Database error deleting stock by id: {}", id, e);
            throw new PersistenceException("Error deleting stock by id: " + id, e);
        }
    }

}
