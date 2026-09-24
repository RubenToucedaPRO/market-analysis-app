package com.market.analysis.unit.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
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
import org.springframework.jdbc.core.JdbcTemplate;

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

    @Mock
    private JdbcTemplate jdbcTemplate;

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

        verifyNoInteractions(jpaCandleRepository, candleMapper, jdbcTemplate);
    }

    @Test
    @DisplayName("Should skip persistence when candle list is null")
    void saveCandlesForTicker_nullList_noInteractions() {
        sqlCandleHistoryRepository.saveCandlesForTicker("MSFT", null);

        verifyNoInteractions(jpaCandleRepository, candleMapper, jdbcTemplate);
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should delete existing candles before inserting the new batch")
    void saveCandlesForTicker_validList_deleteBeforeSave() {
        String ticker = "AAPL";
        Candle candle = buildCandle(ticker);

        sqlCandleHistoryRepository.saveCandlesForTicker(ticker, List.of(candle));

        InOrder order = inOrder(jpaCandleRepository, jdbcTemplate);
        order.verify(jpaCandleRepository).deleteByTicker(ticker);
        order.verify(jdbcTemplate).batchUpdate(any(String.class), any(List.class));
    }

    @Test
    @DisplayName("Should batch insert every candle with values in column order")
    @SuppressWarnings("unchecked")
    void saveCandlesForTicker_validList_batchInsertsAllCandles() {
        String ticker = "AAPL";
        Candle candle1 = buildCandle(ticker);
        Candle candle2 = buildCandle(ticker);

        sqlCandleHistoryRepository.saveCandlesForTicker(ticker, List.of(candle1, candle2));

        var batchCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(jdbcTemplate).batchUpdate(
                org.mockito.ArgumentMatchers.contains("INSERT INTO candles"),
                batchCaptor.capture());

        List<Object[]> rows = (List<Object[]>) batchCaptor.getValue();
        assertThat(rows).hasSize(2);
        for (Object[] row : rows) {
            assertThat(row[0]).isEqualTo(ticker);
            assertThat(row[1]).isEqualTo(java.sql.Timestamp.from(candle1.getDateTime()));
            assertThat((java.math.BigDecimal) row[2]).isEqualByComparingTo(new BigDecimal("181.00"));
            assertThat((java.math.BigDecimal) row[3]).isEqualByComparingTo(new BigDecimal("183.50"));
            assertThat((java.math.BigDecimal) row[4]).isEqualByComparingTo(new BigDecimal("180.00"));
            assertThat((java.math.BigDecimal) row[5]).isEqualByComparingTo(new BigDecimal("182.75"));
            assertThat(row[6]).isEqualTo(55_000_000L);
        }
    }

    @Test
    @DisplayName("Should delete candles exactly once for the specified ticker")
    void saveCandlesForTicker_validList_deletesExactlyOnceForTicker() {
        String ticker = "TSLA";
        Candle candle = buildCandle(ticker);

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
    // findCandlesByTicker
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findCandlesByTicker: should throw when ticker is null")
    void findCandlesByTicker_nullTicker_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> sqlCandleHistoryRepository.findCandlesByTicker(null));
    }

    @Test
    @DisplayName("findCandlesByTicker: should throw when ticker is blank")
    void findCandlesByTicker_blankTicker_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> sqlCandleHistoryRepository.findCandlesByTicker("  "));
    }

    @Test
    @DisplayName("findCandlesByTicker: should return empty list when JPA returns no results")
    void findCandlesByTicker_noEntities_returnsEmptyList() {
        when(jpaCandleRepository.findByTickerOrderByDateTimeAsc("AAPL")).thenReturn(List.of());

        List<Candle> result = sqlCandleHistoryRepository.findCandlesByTicker("AAPL");

        assertThat(result).isEmpty();
        verify(candleMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("findCandlesByTicker: should map every entity and return domain candles")
    void findCandlesByTicker_twoEntities_returnsTwoMappedCandles() {
        String ticker = "AAPL";
        CandleEntity entity1 = new CandleEntity();
        CandleEntity entity2 = new CandleEntity();
        Candle candle1 = buildCandle(ticker);
        Candle candle2 = buildCandle(ticker);

        when(jpaCandleRepository.findByTickerOrderByDateTimeAsc(ticker)).thenReturn(List.of(entity1, entity2));
        when(candleMapper.toDomain(entity1)).thenReturn(candle1);
        when(candleMapper.toDomain(entity2)).thenReturn(candle2);

        List<Candle> result = sqlCandleHistoryRepository.findCandlesByTicker(ticker);

        assertThat(result).containsExactly(candle1, candle2);
        verify(candleMapper, times(2)).toDomain(any(CandleEntity.class));
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
