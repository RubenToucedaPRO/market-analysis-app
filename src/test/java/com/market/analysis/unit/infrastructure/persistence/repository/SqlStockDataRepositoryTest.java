package com.market.analysis.unit.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.infrastructure.persistence.entity.CompanyProfileEntity;
import com.market.analysis.infrastructure.persistence.entity.StockEntity;
import com.market.analysis.infrastructure.persistence.mapper.StockMapper;
import com.market.analysis.infrastructure.persistence.repository.JpaCompanyProfileRepository;
import com.market.analysis.infrastructure.persistence.repository.JpaStockDataRepository;
import com.market.analysis.infrastructure.persistence.repository.SqlStockDataRepository;

/**
 * Unit tests for SqlStockDataRepository.
 */
@DisplayName("SqlStockDataRepository Unit Tests")
@ExtendWith(MockitoExtension.class)
class SqlStockDataRepositoryTest {

    @Mock
    private JpaStockDataRepository jpaRepository;

    @Mock
    private JpaCompanyProfileRepository jpaCompanyProfileRepository;

    @Mock
    private StockMapper mapper;

    @InjectMocks
    private SqlStockDataRepository sqlRepository;

    private Stock testStock;
    private StockEntity testEntity;
    private CompanyProfileEntity testCompanyProfile;

    @BeforeEach
    void setUp() {
        LocalDateTime lastUpdated = LocalDateTime.now();

        testCompanyProfile = CompanyProfileEntity.builder()
                .id(1L)
                .ticker("AAPL")
                .logo("https://example.com/logo.png")
                .build();

        testStock = Stock.builder()
                .ticker("AAPL")
                .logoUrl("https://example.com/logo.png")
                .currentPrice(new BigDecimal("150.50"))
                .openPrice(new BigDecimal("149.00"))
                .volume(50000000L)
                .averageVolume(45000000L)
                .lastUpdated(lastUpdated)
                .build();

        testEntity = new StockEntity();
        testEntity.setId(1L);
        testEntity.setTicker("AAPL");
        testEntity.setCurrentPrice(new BigDecimal("150.50"));
        testEntity.setOpenPrice(new BigDecimal("149.00"));
        testEntity.setVolume(50000000L);
        testEntity.setAverageVolume(45000000L);
        testEntity.setLastUpdated(lastUpdated);
        testEntity.setCompanyProfile(testCompanyProfile);
    }

    @Test
    @DisplayName("Should find all stocks with profiles")
    void testFindAllStocks() {
        // Arrange
        StockEntity entity2 = new StockEntity();
        entity2.setTicker("GOOGL");
        entity2.setCompanyProfile(testCompanyProfile);

        Stock stock2 = Stock.builder()
                .ticker("GOOGL")
                .logoUrl("https://example.com/logo.png")
                .currentPrice(new BigDecimal("100.00"))
                .build();

        when(jpaRepository.findAllWithProfile()).thenReturn(Arrays.asList(testEntity, entity2));
        when(mapper.toDomain(testEntity)).thenReturn(testStock);
        when(mapper.toDomain(entity2)).thenReturn(stock2);

        // Act
        List<Stock> result = sqlRepository.findAllStocks();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2).extracting(Stock::getTicker).containsExactlyInAnyOrder("AAPL", "GOOGL");
        assertThat(result.get(0).getLogoUrl()).isEqualTo("https://example.com/logo.png");
        verify(jpaRepository, times(1)).findAllWithProfile();
    }

    @Test
    @DisplayName("Should return empty list when no stocks exist")
    void testFindAllStocksEmpty() {
        // Arrange
        when(jpaRepository.findAllWithProfile()).thenReturn(Arrays.asList());

        // Act
        List<Stock> result = sqlRepository.findAllStocks();

        // Assert
        assertThat(result).isNotNull().isEmpty();
        verify(jpaRepository, times(1)).findAllWithProfile();
    }

    @Test
    @DisplayName("Should save new stock data")
    void testSaveNewStockData() {
        // Arrange
        when(jpaRepository.findByTicker("AAPL")).thenReturn(Optional.empty());
        when(mapper.toEntity(testStock)).thenReturn(testEntity);
        when(jpaCompanyProfileRepository.findByTicker("AAPL")).thenReturn(Optional.of(testCompanyProfile));
        when(jpaRepository.save(any(StockEntity.class))).thenReturn(testEntity);

        // Act
        sqlRepository.saveStockData(testStock);

        // Assert
        verify(jpaRepository, times(1)).findByTicker("AAPL");
        verify(mapper, times(1)).toEntity(testStock);
        verify(jpaCompanyProfileRepository, times(1)).findByTicker("AAPL");
        verify(jpaRepository, times(1)).save(any(StockEntity.class));
    }

    @Test
    @DisplayName("Should update existing stock data")
    void testSaveExistingStockData() {
        // Arrange
        StockEntity existingEntity = new StockEntity();
        existingEntity.setId(2L);
        existingEntity.setTicker("AAPL");
        existingEntity.setCompanyProfile(testCompanyProfile);

        when(jpaRepository.findByTicker("AAPL")).thenReturn(Optional.of(existingEntity));
        when(mapper.toEntity(testStock)).thenReturn(testEntity);
        when(jpaRepository.save(any(StockEntity.class))).thenReturn(testEntity);

        // Act
        sqlRepository.saveStockData(testStock);

        // Assert
        verify(jpaRepository, times(1)).findByTicker("AAPL");
        verify(mapper, times(1)).toEntity(testStock);
        verify(jpaRepository, times(1)).save(any(StockEntity.class));
    }

    @Test
    @DisplayName("Should find stock by ticker")
    void testFindByTicker() {
        // Arrange
        when(jpaRepository.findByTicker("AAPL")).thenReturn(Optional.of(testEntity));
        when(mapper.toDomain(testEntity)).thenReturn(testStock);

        // Act
        Optional<Stock> result = sqlRepository.findByTicker("AAPL");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTicker()).isEqualTo("AAPL");
        assertThat(result.get().getCurrentPrice()).isEqualByComparingTo(new BigDecimal("150.50"));
        verify(jpaRepository, times(1)).findByTicker("AAPL");
        verify(mapper, times(1)).toDomain(testEntity);
    }

    @Test
    @DisplayName("Should return empty when stock not found")
    void testFindByTickerNotFound() {
        // Arrange
        when(jpaRepository.findByTicker("NOTFOUND")).thenReturn(Optional.empty());

        // Act
        Optional<Stock> result = sqlRepository.findByTicker("NOTFOUND");

        // Assert
        assertThat(result).isEmpty();
        verify(jpaRepository, times(1)).findByTicker("NOTFOUND");
    }

    @Test
    @DisplayName("Should update stock data")
    void testUpdateStockData() {
        // Arrange
        when(jpaRepository.findByTicker("AAPL")).thenReturn(Optional.of(testEntity));
        when(mapper.toEntity(testStock)).thenReturn(testEntity);

        // Act
        sqlRepository.updateStockData(testStock);

        // Assert
        verify(jpaRepository, times(1)).findByTicker("AAPL");
        verify(mapper, times(1)).toEntity(testStock);
    }

    @Test
    @DisplayName("Should not update when stock not found")
    void testUpdateStockDataNotFound() {
        // Arrange
        Stock notFoundStock = Stock.builder()
                .ticker("NOTFOUND")
                .currentPrice(new BigDecimal("100.00"))
                .build();

        when(jpaRepository.findByTicker("NOTFOUND")).thenReturn(Optional.empty());

        // Act
        sqlRepository.updateStockData(notFoundStock);

        // Assert
        verify(jpaRepository, times(1)).findByTicker("NOTFOUND");
    }

    @Test
    @DisplayName("Should delete stock by ticker")
    void testDeleteByTicker() {
        // Act
        sqlRepository.deleteByTicker("AAPL");

        // Assert
        verify(jpaRepository, times(1)).deleteByTicker("AAPL");
    }
}
