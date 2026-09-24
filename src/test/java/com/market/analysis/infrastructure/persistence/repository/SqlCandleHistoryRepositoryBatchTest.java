package com.market.analysis.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.market.analysis.domain.model.Candle;
import com.market.analysis.infrastructure.persistence.mapper.CandleMapper;

/**
 * Integration tests for the native batch insert in SqlCandleHistoryRepository.
 * Runs against H2 so the real SQL (DELETE + batched INSERT) is exercised,
 * which unit tests with a mocked JdbcTemplate cannot cover.
 */
@DisplayName("Candle Batch Insert Integration Tests")
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@EntityScan("com.market.analysis.infrastructure.persistence.entity")
@EnableJpaRepositories("com.market.analysis.infrastructure.persistence.repository")
@Import({SqlCandleHistoryRepository.class, CandleMapper.class})
class SqlCandleHistoryRepositoryBatchTest {

    @Autowired
    private SqlCandleHistoryRepository sqlCandleHistoryRepository;

    @Test
    @DisplayName("Should persist every candle with the native batch insert")
    void saveCandlesForTicker_threeCandles_persistsAll() {
        String ticker = "AAPL";
        List<Candle> candles = IntStream.range(0, 3)
                .mapToObj(i -> buildCandle(ticker, i))
                .toList();

        sqlCandleHistoryRepository.saveCandlesForTicker(ticker, candles);

        List<Candle> persisted = sqlCandleHistoryRepository.findCandlesByTicker(ticker);
        assertThat(persisted).hasSize(3);
        assertThat(persisted.get(0).getOpenPrice()).isEqualByComparingTo(new BigDecimal("181.00"));
        assertThat(persisted.get(0).getVolume()).isEqualTo(55_000_000L);
        assertThat(persisted.get(2).getDateTime()).isEqualTo(Instant.parse("2024-01-15T00:00:00Z").plusSeconds(2 * 86400L));
    }

    @Test
    @DisplayName("Should replace existing candles instead of duplicating them")
    void saveCandlesForTicker_calledTwice_replacesCandles() {
        String ticker = "MSFT";

        sqlCandleHistoryRepository.saveCandlesForTicker(ticker, List.of(buildCandle(ticker, 0)));
        sqlCandleHistoryRepository.saveCandlesForTicker(ticker,
                List.of(buildCandle(ticker, 1), buildCandle(ticker, 2)));

        List<Candle> persisted = sqlCandleHistoryRepository.findCandlesByTicker(ticker);
        assertThat(persisted).hasSize(2);
    }

    @Test
    @DisplayName("Should keep existing candles when the new list is empty")
    void saveCandlesForTicker_emptyList_keepsExisting() {
        String ticker = "TSLA";
        sqlCandleHistoryRepository.saveCandlesForTicker(ticker, List.of(buildCandle(ticker, 0)));

        sqlCandleHistoryRepository.saveCandlesForTicker(ticker, List.of());

        assertThat(sqlCandleHistoryRepository.findCandlesByTicker(ticker)).hasSize(1);
    }

    private Candle buildCandle(String ticker, int dayOffset) {
        return Candle.builder()
                .ticker(ticker)
                .dateTime(Instant.parse("2024-01-15T00:00:00Z").plusSeconds((long) dayOffset * 86400L))
                .openPrice(new BigDecimal("181.00"))
                .highPrice(new BigDecimal("183.50"))
                .lowPrice(new BigDecimal("180.00"))
                .closePrice(new BigDecimal("182.75"))
                .volume(55_000_000L)
                .build();
    }
}
