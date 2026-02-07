package com.market.analysis.unit.infrastructure.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        testProhibitedTicker = new ProhibitedTicker("AAPL");

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
        
        List<ProhibitedTickerEntity> entities = Arrays.asList(testEntity, entity2);
        when(jpaRepository.findAll()).thenReturn(entities);
        when(mapper.toDomain(testEntity)).thenReturn(testProhibitedTicker);
        when(mapper.toDomain(entity2)).thenReturn(new ProhibitedTicker("GOOGL"));

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
        when(mapper.toEntity(testProhibitedTicker)).thenReturn(testEntity);
        when(jpaRepository.save(testEntity)).thenReturn(testEntity);
        when(mapper.toDomain(testEntity)).thenReturn(testProhibitedTicker);

        // Act
        ProhibitedTicker result = sqlRepository.save(testProhibitedTicker);

        // Assert
        assertNotNull(result);
        assertEquals("AAPL", result.getTicker());
        verify(mapper, times(1)).toEntity(testProhibitedTicker);
        verify(jpaRepository, times(1)).save(testEntity);
        verify(mapper, times(1)).toDomain(testEntity);
    }

    @Test
    @DisplayName("Should delete prohibited ticker by id")
    void testDeleteById() {
        // Act
        sqlRepository.deleteById(1L);

        // Assert
        verify(jpaRepository, times(1)).deleteById(1L);
    }
}
