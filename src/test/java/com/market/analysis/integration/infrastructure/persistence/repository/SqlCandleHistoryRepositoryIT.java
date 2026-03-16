package com.market.analysis.integration.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.market.analysis.domain.model.Candle;
import com.market.analysis.infrastructure.persistence.entity.CandleEntity;
import com.market.analysis.infrastructure.persistence.mapper.CandleMapper;
import com.market.analysis.infrastructure.persistence.repository.JpaCandleRepository;
import com.market.analysis.infrastructure.persistence.repository.SqlCandleHistoryRepository;

/**
 * JPA integration test for SqlCandleHistoryRepository (F1.10).
 *
 * <p>Uses an H2 in-memory database (via {@code @DataJpaTest}) to verify that
 * the transactional replace strategy works correctly against real JPA
 * persistence: initial inserts, full dataset replacement, and absence of
 * duplicates.</p>
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({SqlCandleHistoryRepository.class, CandleMapper.class})
@DisplayName("SqlCandleHistoryRepository Integration Tests (F1.10)")
class SqlCandleHistoryRepositoryIT {

    @Autowired
    private SqlCandleHistoryRepository sqlCandleHistoryRepository;

    @Autowired
    private JpaCandleRepository jpaCandleRepository;

    private static final String TICKER = "AAPL";
    private static final String OTHER_TICKER = "MSFT";
    private static final long SECONDS_PER_DAY = 86_400L;

    @BeforeEach
    void setUp() {
        jpaCandleRepository.deleteAll();
    }

    // -------------------------------------------------------------------------
    // Save 240 candles
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should persist 240 candles for a ticker")
    void saveCandlesForTicker_240Candles_allPersisted() {
        List<Candle> candles = buildCandles(TICKER, 240, Instant.parse("2024-01-01T00:00:00Z"));

        sqlCandleHistoryRepository.saveCandlesForTicker(TICKER, candles);

        long count = jpaCandleRepository.findByTickerOrderByDateTimeAsc(TICKER).size();
        assertThat(count).isEqualTo(240);
    }

    @Test
    @DisplayName("Should persist candles in ascending datetime order")
    void saveCandlesForTicker_240Candles_orderedAscending() {
        List<Candle> candles = buildCandles(TICKER, 240, Instant.parse("2024-01-01T00:00:00Z"));

        sqlCandleHistoryRepository.saveCandlesForTicker(TICKER, candles);

        List<Instant> dateTimes = jpaCandleRepository
                .findByTickerOrderByDateTimeAsc(TICKER)
                .stream()
                .map(CandleEntity::getDateTime)
                .toList();

        assertThat(dateTimes).isSortedAccordingTo(Instant::compareTo);
    }

    // -------------------------------------------------------------------------
    // Replace dataset for same ticker
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should replace the full dataset when called twice for the same ticker")
    void saveCandlesForTicker_calledTwice_replacesDataset() {
        List<Candle> firstBatch = buildCandles(TICKER, 240, Instant.parse("2024-01-01T00:00:00Z"));
        sqlCandleHistoryRepository.saveCandlesForTicker(TICKER, firstBatch);

        Instant newStart = Instant.parse("2024-09-01T00:00:00Z");
        List<Candle> secondBatch = buildCandles(TICKER, 120, newStart);
        sqlCandleHistoryRepository.saveCandlesForTicker(TICKER, secondBatch);

        List<Instant> persisted = jpaCandleRepository
                .findByTickerOrderByDateTimeAsc(TICKER)
                .stream()
                .map(CandleEntity::getDateTime)
                .toList();

        assertThat(persisted).hasSize(120);
        assertThat(persisted).allMatch(dt -> !dt.isBefore(newStart));
    }

    @Test
    @DisplayName("Should not retain any candles from the previous batch after replacement")
    void saveCandlesForTicker_replace_noPreviousCandlesRemain() {
        Instant oldStart = Instant.parse("2023-01-01T00:00:00Z");
        List<Candle> firstBatch = buildCandles(TICKER, 30, oldStart);
        sqlCandleHistoryRepository.saveCandlesForTicker(TICKER, firstBatch);

        Instant newStart = Instant.parse("2024-06-01T00:00:00Z");
        List<Candle> secondBatch = buildCandles(TICKER, 10, newStart);
        sqlCandleHistoryRepository.saveCandlesForTicker(TICKER, secondBatch);

        boolean anyOldCandles = jpaCandleRepository
                .findByTickerOrderByDateTimeAsc(TICKER)
                .stream()
                .anyMatch(e -> e.getDateTime().isBefore(newStart));

        assertThat(anyOldCandles).isFalse();
    }

    // -------------------------------------------------------------------------
    // No duplicates
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should not produce duplicates when saving the same batch twice")
    void saveCandlesForTicker_sameBatchTwice_noDuplicates() {
        List<Candle> candles = buildCandles(TICKER, 50, Instant.parse("2024-03-01T00:00:00Z"));

        sqlCandleHistoryRepository.saveCandlesForTicker(TICKER, candles);
        sqlCandleHistoryRepository.saveCandlesForTicker(TICKER, candles);

        long count = jpaCandleRepository.findByTickerOrderByDateTimeAsc(TICKER).size();
        assertThat(count).isEqualTo(50);
    }

    // -------------------------------------------------------------------------
    // Isolation between tickers
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should not affect candles of other tickers when replacing")
    void saveCandlesForTicker_replaceTicker_doesNotAffectOtherTicker() {
        List<Candle> aaplCandles = buildCandles(TICKER, 5, Instant.parse("2024-01-01T00:00:00Z"));
        List<Candle> msftCandles = buildCandles(OTHER_TICKER, 8, Instant.parse("2024-01-01T00:00:00Z"));

        sqlCandleHistoryRepository.saveCandlesForTicker(TICKER, aaplCandles);
        sqlCandleHistoryRepository.saveCandlesForTicker(OTHER_TICKER, msftCandles);

        // Replace AAPL only
        List<Candle> newAapl = buildCandles(TICKER, 3, Instant.parse("2024-07-01T00:00:00Z"));
        sqlCandleHistoryRepository.saveCandlesForTicker(TICKER, newAapl);

        long aaplCount = jpaCandleRepository.findByTickerOrderByDateTimeAsc(TICKER).size();
        long msftCount = jpaCandleRepository.findByTickerOrderByDateTimeAsc(OTHER_TICKER).size();

        assertThat(aaplCount).isEqualTo(3);
        assertThat(msftCount).isEqualTo(8);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a list of {@code count} candles for the given ticker, each with a
     * datetime offset by one day from {@code startTime}.
     */
    private List<Candle> buildCandles(String ticker, int count, Instant startTime) {
        List<Candle> candles = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            candles.add(Candle.builder()
                    .ticker(ticker)
                    .dateTime(startTime.plusSeconds((long) i * SECONDS_PER_DAY))
                    .openPrice(new BigDecimal("100.00"))
                    .highPrice(new BigDecimal("105.00"))
                    .lowPrice(new BigDecimal("98.00"))
                    .closePrice(new BigDecimal("103.00"))
                    .volume(1_000_000L + i)
                    .build());
        }
        return candles;
    }
}
