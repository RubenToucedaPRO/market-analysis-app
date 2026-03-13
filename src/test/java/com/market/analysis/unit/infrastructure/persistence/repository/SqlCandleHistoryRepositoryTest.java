package com.market.analysis.unit.infrastructure.persistence.repository;

import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.domain.model.Candle;
import com.market.analysis.infrastructure.persistence.mapper.CandleMapper;
import com.market.analysis.infrastructure.persistence.repository.JpaCandleRepository;
import com.market.analysis.infrastructure.persistence.repository.SqlCandleHistoryRepository;

/**
 * Unit tests for SqlCandleHistoryRepository.
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

    @Test
    @DisplayName("Should accept saveCandlesForTicker call without interacting with dependencies (stub)")
    void saveCandlesForTicker_stubbed_noInteractions() {
        // Arrange
        String ticker = "AAPL";
        List<Candle> candles = List.of(
                Candle.builder()
                        .ticker(ticker)
                        .dateTime(Instant.parse("2024-01-15T00:00:00Z"))
                        .openPrice(new BigDecimal("181.00"))
                        .highPrice(new BigDecimal("183.50"))
                        .lowPrice(new BigDecimal("180.00"))
                        .closePrice(new BigDecimal("182.75"))
                        .volume(55000000L)
                        .build()
        );

        // Act — method body is intentionally empty until F1.5
        sqlCandleHistoryRepository.saveCandlesForTicker(ticker, candles);

        // Assert — no JPA or mapper interaction expected at this stage
        verifyNoInteractions(jpaCandleRepository, candleMapper);
    }

    @Test
    @DisplayName("Should accept saveCandlesForTicker call with empty list without error")
    void saveCandlesForTicker_emptyList_noError() {
        // Act — must not throw even for an empty list
        sqlCandleHistoryRepository.saveCandlesForTicker("MSFT", List.of());

        // Assert
        verifyNoInteractions(jpaCandleRepository, candleMapper);
    }
}
