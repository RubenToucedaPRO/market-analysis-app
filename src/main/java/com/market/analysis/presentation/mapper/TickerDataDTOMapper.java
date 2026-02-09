package com.market.analysis.presentation.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.TickerData;
import com.market.analysis.presentation.dto.TickerDataDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TickerDataDTOMapper {

    public TickerDataDTO toDTO(TickerData ticker) {
        if (ticker == null) {
            return null;
        }

        return TickerDataDTO.builder()
                .ticker(ticker.getTicker())
                .currentPrice(ticker.getCurrentPrice())
                .openPrice(ticker.getOpenPrice())
                .highOfDay(ticker.getHighOfDay())
                .lowOfDay(ticker.getLowOfDay())
                .previousClose(ticker.getPreviousClose())
                .sma20(ticker.getSma20())
                .sma50(ticker.getSma50())
                .sma200(ticker.getSma200())
                .volume(ticker.getVolume())
                .averageVolume(ticker.getAverageVolume())
                .lastUpdated(ticker.getLastUpdated())
                .build();
    }

}
