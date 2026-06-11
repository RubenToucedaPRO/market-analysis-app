package com.market.analysis.unit.infrastructure.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.market.analysis.domain.model.PageResult;
import com.market.analysis.domain.model.ProhibitedTicker;
import com.market.analysis.infrastructure.persistence.entity.ProhibitedTickerEntity;
import com.market.analysis.infrastructure.persistence.mapper.ProhibitedTickerMapper;
import com.market.analysis.infrastructure.persistence.repository.JpaProhibitedTickerRepository;
import com.market.analysis.infrastructure.persistence.repository.SqlProhibitedTickerRepository;

/**
 * Unit tests for SqlProhibitedTickerRepository.
 */
@DisplayName("SqlProhibitedTickerRepository Unit Tests")
@ExtendWith(MockitoExtension.class)
class SqlProhibitedTickerRepositoryTest {

    @Mock
    private JpaProhibitedTickerRepository jpaRepository;

    @Mock
    private ProhibitedTickerMapper mapper;

    @InjectMocks
    private SqlProhibitedTickerRepository sqlRepository;

    private ProhibitedTicker testProhibitedTicker;
    private ProhibitedTickerEntity testEntity;

    @BeforeEach
    void setUp() {
        testProhibitedTicker = new ProhibitedTicker("AAPL", "Test reason",
                Instant.now().minus(31, java.time.temporal.ChronoUnit.DAYS));

        testEntity = new ProhibitedTickerEntity();
        testEntity.setId(1L);
        testEntity.setTicker("AAPL");
        testEntity.setReason("Test reason");
    }

    @Test
    @DisplayName("Should find all prohibited tickers")
    void testFindAll() {
        // Arrange
        ProhibitedTickerEntity entity2 = new ProhibitedTickerEntity();
        entity2.setId(2L);
        entity2.setTicker("GOOGL");
        entity2.setReason("Test reason 2");

        List<ProhibitedTickerEntity> entities = Arrays.asList(testEntity, entity2);
        when(jpaRepository.findAll()).thenReturn(entities);
        when(mapper.toDomain(testEntity)).thenReturn(testProhibitedTicker);
        when(mapper.toDomain(entity2)).thenReturn(new ProhibitedTicker("GOOGL", "Test reason 2", null));

        // Act
        List<ProhibitedTicker> result = sqlRepository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).getTicker());
        assertEquals("GOOGL", result.get(1).getTicker());
        verify(jpaRepository, times(1)).findAll();
        verify(mapper, times(2)).toDomain(org.mockito.ArgumentMatchers.any(ProhibitedTickerEntity.class));
    }

    @Test
    @DisplayName("Should return empty list when no prohibited tickers exist")
    void testFindAllEmpty() {
        // Arrange
        when(jpaRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<ProhibitedTicker> result = sqlRepository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(jpaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should check if ticker exists")
    void testExistsByTicker() {
        // Arrange
        when(jpaRepository.existsByTicker("AAPL")).thenReturn(true);

        // Act
        boolean result = sqlRepository.existsByTicker("AAPL");

        // Assert
        assertTrue(result);
        verify(jpaRepository, times(1)).existsByTicker("AAPL");
    }

    @Test
    @DisplayName("Should return false when ticker does not exist")
    void testExistsByTickerNotFound() {
        // Arrange
        when(jpaRepository.existsByTicker("MSFT")).thenReturn(false);

        // Act
        boolean result = sqlRepository.existsByTicker("MSFT");

        // Assert
        assertFalse(result);
        verify(jpaRepository, times(1)).existsByTicker("MSFT");
    }

    @Test
    @DisplayName("Should save prohibited ticker")
    void testSave() {
        // Arrange
        when(jpaRepository.existsByTicker("AAPL")).thenReturn(false);
        when(mapper.toEntity(testProhibitedTicker)).thenReturn(testEntity);
        when(jpaRepository.save(testEntity)).thenReturn(testEntity);

        // Act
        sqlRepository.save(testProhibitedTicker);

        // Assert
        verify(mapper, times(1)).toEntity(testProhibitedTicker);
        verify(jpaRepository, times(1)).save(testEntity);
    }

    @Test
    @DisplayName("Should delete prohibited ticker by ticker")
    void deleteByTicker() {
        // Act
        sqlRepository.deleteByTicker("AAPL");

        // Assert
        verify(jpaRepository, times(1)).deleteByTicker("AAPL");
    }

    @Test
    @DisplayName("Should return paginated prohibited tickers")
    void shouldReturnPaginatedProhibitedTickers() {
        ProhibitedTickerEntity entity2 = new ProhibitedTickerEntity();
        entity2.setId(2L);
        entity2.setTicker("GOOGL");
        entity2.setReason("Test reason 2");

        Page<ProhibitedTickerEntity> page = new PageImpl<>(Arrays.asList(testEntity, entity2), PageRequest.of(0, 10), 2);
        when(jpaRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);
        when(mapper.toDomain(testEntity)).thenReturn(testProhibitedTicker);
        when(mapper.toDomain(entity2)).thenReturn(new ProhibitedTicker("GOOGL", "Test reason 2", null));

        PageResult<ProhibitedTicker> result = sqlRepository.findAll(0, 10);

        assertNotNull(result);
        assertEquals(2, result.content().size());
        assertEquals("AAPL", result.content().get(0).getTicker());
        assertEquals("GOOGL", result.content().get(1).getTicker());
        assertEquals(0, result.pageNumber());
        assertEquals(10, result.pageSize());
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());
    }

    @Test
    @DisplayName("Should return empty paginated result when no tickers exist")
    void shouldReturnEmptyPaginatedResult() {
        Page<ProhibitedTickerEntity> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
        when(jpaRepository.findAll(PageRequest.of(0, 10))).thenReturn(emptyPage);

        PageResult<ProhibitedTicker> result = sqlRepository.findAll(0, 10);

        assertNotNull(result);
        assertEquals(0, result.content().size());
        assertEquals(0, result.totalElements());
    }

    @Test
    @DisplayName("Should skip save when ticker already exists")
    void shouldSkipSaveWhenTickerAlreadyExists() {
        when(jpaRepository.existsByTicker("AAPL")).thenReturn(true);

        sqlRepository.save(testProhibitedTicker);

        verify(mapper, never()).toEntity(testProhibitedTicker);
        verify(jpaRepository, never()).save(testEntity);
    }
}
