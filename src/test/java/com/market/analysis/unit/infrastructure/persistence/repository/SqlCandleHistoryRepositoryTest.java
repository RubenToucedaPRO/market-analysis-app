package com.market.analysis.unit.infrastructure.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.domain.model.Candle;
import com.market.analysis.infrastructure.persistence.entity.CandleEntity;
import com.market.analysis.infrastructure.persistence.mapper.CandleMapper;
import com.market.analysis.infrastructure.persistence.repository.JpaCandleRepository;
import com.market.analysis.infrastructure.persistence.repository.SqlCandleHistoryRepository;

/**
 * Unit tests for SqlCandleHistoryRepository (F1.5 — transactional replace).
 */
@DisplayName("SqlCandleHistoryRepository Unit Tests")
@ExtendWith(MockitoExtension.class)
class SqlCandleHistoryRepositoryTest {

    @Mock
    private JpaCandleRepository jpaCandleRepository;

    @Mock
    private CandleMapper candleMapper;

    @InjectMocks
    private SqlCandleHistoryRepository sqlCandleHistoryRepository;

    // -------------------------------------------------------------------------
    // Guard conditions
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should throw when ticker is null")
    void saveCandlesForTicker_nullTicker_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> sqlCandleHistoryRepository.saveCandlesForTicker(null, List.of()));
    }

    @Test
    @DisplayName("Should throw when ticker is blank")
    void saveCandlesForTicker_blankTicker_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> sqlCandleHistoryRepository.saveCandlesForTicker("  ", List.of()));
    }

    @Test
    @DisplayName("Should skip persistence when candle list is empty")
    void saveCandlesForTicker_emptyList_noInteractions() {
        sqlCandleHistoryRepository.saveCandlesForTicker("MSFT", List.of());

        verifyNoInteractions(jpaCandleRepository, candleMapper);
    }

    @Test
    @DisplayName("Should skip persistence when candle list is null")
    void saveCandlesForTicker_nullList_noInteractions() {
        sqlCandleHistoryRepository.saveCandlesForTicker("MSFT", null);

        verifyNoInteractions(jpaCandleRepository, candleMapper);
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should delete existing candles before inserting the new batch")
    void saveCandlesForTicker_validList_deleteBeforeSave() {
        String ticker = "AAPL";
        Candle candle = buildCandle(ticker);
        CandleEntity entity = new CandleEntity();
        when(candleMapper.toEntity(candle)).thenReturn(entity);

        sqlCandleHistoryRepository.saveCandlesForTicker(ticker, List.of(candle));

        InOrder order = inOrder(jpaCandleRepository);
        order.verify(jpaCandleRepository).deleteByTicker(ticker);
        order.verify(jpaCandleRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should map every candle to an entity and persist it")
    void saveCandlesForTicker_validList_mapsAndSavesAllCandles() {
        String ticker = "AAPL";
        Candle candle1 = buildCandle(ticker);
        Candle candle2 = buildCandle(ticker);
        CandleEntity entity1 = new CandleEntity();
        CandleEntity entity2 = new CandleEntity();

        when(candleMapper.toEntity(candle1)).thenReturn(entity1);
        when(candleMapper.toEntity(candle2)).thenReturn(entity2);

        sqlCandleHistoryRepository.saveCandlesForTicker(ticker, List.of(candle1, candle2));

        verify(candleMapper, times(2)).toEntity(any(Candle.class));
        verify(jpaCandleRepository).saveAll(List.of(entity1, entity2));
    }

    @Test
    @DisplayName("Should delete candles exactly once for the specified ticker")
    void saveCandlesForTicker_validList_deletesExactlyOnceForTicker() {
        String ticker = "TSLA";
        Candle candle = buildCandle(ticker);
        when(candleMapper.toEntity(candle)).thenReturn(new CandleEntity());

        sqlCandleHistoryRepository.saveCandlesForTicker(ticker, List.of(candle));

        verify(jpaCandleRepository, times(1)).deleteByTicker(ticker);
    }

    // -------------------------------------------------------------------------
    // deleteCandlesByTicker
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deleteCandlesByTicker: should throw when ticker is null")
    void deleteCandlesByTicker_nullTicker_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> sqlCandleHistoryRepository.deleteCandlesByTicker(null));
    }

    @Test
    @DisplayName("deleteCandlesByTicker: should throw when ticker is blank")
    void deleteCandlesByTicker_blankTicker_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> sqlCandleHistoryRepository.deleteCandlesByTicker("  "));
    }

    @Test
    @DisplayName("deleteCandlesByTicker: should delegate to JPA repository exactly once")
    void deleteCandlesByTicker_validTicker_delegatesToJpa() {
        String ticker = "AAPL";

        sqlCandleHistoryRepository.deleteCandlesByTicker(ticker);

        verify(jpaCandleRepository, times(1)).deleteByTicker(ticker);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Candle buildCandle(String ticker) {
        return Candle.builder()
                .ticker(ticker)
                .dateTime(Instant.parse("2024-01-15T00:00:00Z"))
                .openPrice(new BigDecimal("181.00"))
                .highPrice(new BigDecimal("183.50"))
                .lowPrice(new BigDecimal("180.00"))
                .closePrice(new BigDecimal("182.75"))
                .volume(55_000_000L)
                .build();
    }
}
