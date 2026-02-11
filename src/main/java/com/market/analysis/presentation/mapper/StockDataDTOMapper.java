package com.market.analysis.presentation.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.presentation.dto.StockDataDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockDataDTOMapper {

    public StockDataDTO toDTO(Stock stock) {
        if (stock == null) {
            return null;
        }

        return StockDataDTO.builder()
                .ticker(stock.getTicker())
                .logoUrl(stock.getLogoUrl())
                .currentPrice(stock.getCurrentPrice())
                .openPrice(stock.getOpenPrice())
                .highOfDay(stock.getHighOfDay())
                .lowOfDay(stock.getLowOfDay())
                .previousClose(stock.getPreviousClose())
                .sma20(stock.getSma20())
                .sma50(stock.getSma50())
                .sma200(stock.getSma200())
                .volume(stock.getVolume())
                .averageVolume(stock.getAverageVolume())
                .lastUpdated(stock.getLastUpdated())
                .build();
    }

}
