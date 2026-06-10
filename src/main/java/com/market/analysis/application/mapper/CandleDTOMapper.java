package com.market.analysis.application.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.application.dto.CandleChartDTO;
import com.market.analysis.application.dto.CandleDTO;
import com.market.analysis.domain.model.Candle;
import com.market.analysis.domain.model.Stock;

import java.util.List;

/**
 * Application-layer mapper responsible for converting domain Candle objects
 * and Stock SMA values into chart-specific DTOs.
 */
@Component
public class CandleDTOMapper {

    /**
     * Maps a single domain {@link Candle} to a {@link CandleDTO}.
     * The {@code time} field is expressed in Unix epoch seconds as required
     * by TradingView Lightweight Charts.
     *
     * @param candle the domain candle; must not be {@code null}
     * @return the corresponding {@link CandleDTO}
     */
    public CandleDTO toDTO(Candle candle) {
        return CandleDTO.builder()
                .time(candle.getDateTime().getEpochSecond())
                .open(candle.getOpenPrice())
                .high(candle.getHighPrice())
                .low(candle.getLowPrice())
                .close(candle.getClosePrice())
                .volume(candle.getVolume())
                .build();
    }

    /**
     * Builds a {@link CandleChartDTO} from a stock and its associated candle list.
     *
     * @param stock   the stock providing ticker and scalar SMA values
     * @param candles the ordered list of domain candles
     * @return the populated {@link CandleChartDTO}
     */
    public CandleChartDTO toChartDTO(Stock stock, List<Candle> candles) {
        List<CandleDTO> candleDTOs = candles.stream()
                .map(this::toDTO)
                .toList();
        return CandleChartDTO.builder()
                .ticker(stock.getTicker())
                .candles(candleDTOs)
                .sma20(stock.getSma20())
                .sma50(stock.getSma50())
                .sma200(stock.getSma200())
                .build();
    }
}
