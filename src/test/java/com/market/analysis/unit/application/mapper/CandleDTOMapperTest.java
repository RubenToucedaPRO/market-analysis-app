package com.market.analysis.unit.application.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.application.dto.CandleChartDTO;
import com.market.analysis.application.dto.CandleDTO;
import com.market.analysis.application.mapper.CandleDTOMapper;
import com.market.analysis.domain.model.Candle;
import com.market.analysis.domain.model.Stock;

/**
 * Unit tests for CandleDTOMapper (F2.3 / F2.4).
 */
@DisplayName("CandleDTOMapper Unit Tests")
class CandleDTOMapperTest {

    private CandleDTOMapper mapper;

    private static final Instant CANDLE_TIME = Instant.parse("2024-01-15T00:00:00Z");

    @BeforeEach
    void setUp() {
        mapper = new CandleDTOMapper();
    }

    // -------------------------------------------------------------------------
    // toDTO
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("toDTO: should map all OHLCV fields from domain Candle")
    void toDTO_allFields_mappedCorrectly() {
        Candle candle = Candle.builder()
                .ticker("AAPL")
                .dateTime(CANDLE_TIME)
                .openPrice(new BigDecimal("181.00"))
                .highPrice(new BigDecimal("183.50"))
                .lowPrice(new BigDecimal("180.00"))
                .closePrice(new BigDecimal("182.75"))
                .volume(55_000_000L)
                .build();

        CandleDTO dto = mapper.toDTO(candle);

        assertThat(dto).isNotNull();
        assertThat(dto.getTime()).isEqualTo(CANDLE_TIME.getEpochSecond());
        assertThat(dto.getOpen()).isEqualByComparingTo(new BigDecimal("181.00"));
        assertThat(dto.getHigh()).isEqualByComparingTo(new BigDecimal("183.50"));
        assertThat(dto.getLow()).isEqualByComparingTo(new BigDecimal("180.00"));
        assertThat(dto.getClose()).isEqualByComparingTo(new BigDecimal("182.75"));
        assertThat(dto.getVolume()).isEqualTo(55_000_000L);
    }

    @Test
    @DisplayName("toDTO: time field should use epoch seconds")
    void toDTO_timeField_usesEpochSeconds() {
        Instant instant = Instant.parse("2024-06-01T12:00:00Z");
        Candle candle = Candle.builder()
                .ticker("AAPL")
                .dateTime(instant)
                .openPrice(BigDecimal.ONE)
                .highPrice(BigDecimal.ONE)
                .lowPrice(BigDecimal.ONE)
                .closePrice(BigDecimal.ONE)
                .volume(1L)
                .build();

        CandleDTO dto = mapper.toDTO(candle);

        assertThat(dto.getTime()).isEqualTo(instant.getEpochSecond());
    }

    // -------------------------------------------------------------------------
    // toChartDTO
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("toChartDTO: should set ticker and SMA scalars from stock")
    void toChartDTO_stockFields_mappedCorrectly() {
        Stock stock = Stock.builder()
                .ticker("MSFT")
                .sma20(new BigDecimal("300.00"))
                .sma50(new BigDecimal("295.00"))
                .sma200(new BigDecimal("280.00"))
                .build();

        CandleChartDTO result = mapper.toChartDTO(stock, List.of());

        assertThat(result.getTicker()).isEqualTo("MSFT");
        assertThat(result.getSma20()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(result.getSma50()).isEqualByComparingTo(new BigDecimal("295.00"));
        assertThat(result.getSma200()).isEqualByComparingTo(new BigDecimal("280.00"));
        assertThat(result.getCandles()).isEmpty();
    }

    @Test
    @DisplayName("toChartDTO: should map each candle in the list")
    void toChartDTO_withCandles_mapsAllCandles() {
        Stock stock = Stock.builder().ticker("AAPL").build();
        Candle c1 = buildCandle(Instant.parse("2024-01-15T00:00:00Z"), "181.00", "183.00", "180.00", "182.00");
        Candle c2 = buildCandle(Instant.parse("2024-01-16T00:00:00Z"), "183.00", "185.00", "182.00", "184.00");

        CandleChartDTO result = mapper.toChartDTO(stock, List.of(c1, c2));

        assertThat(result.getCandles()).hasSize(2);
        assertThat(result.getCandles().get(0).getClose()).isEqualByComparingTo(new BigDecimal("182.00"));
        assertThat(result.getCandles().get(1).getClose()).isEqualByComparingTo(new BigDecimal("184.00"));
    }

    @Test
    @DisplayName("toChartDTO: SMA values should be null when stock has no SMA data")
    void toChartDTO_nullSmas_remainsNull() {
        Stock stock = Stock.builder().ticker("TSLA").sma20(null).sma50(null).sma200(null).build();

        CandleChartDTO result = mapper.toChartDTO(stock, List.of());

        assertThat(result.getSma20()).isNull();
        assertThat(result.getSma50()).isNull();
        assertThat(result.getSma200()).isNull();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Candle buildCandle(Instant dateTime, String open, String high, String low, String close) {
        return Candle.builder()
                .ticker("AAPL")
                .dateTime(dateTime)
                .openPrice(new BigDecimal(open))
                .highPrice(new BigDecimal(high))
                .lowPrice(new BigDecimal(low))
                .closePrice(new BigDecimal(close))
                .volume(1_000_000L)
                .build();
    }
}
