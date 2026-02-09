package com.market.analysis.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.TickerData;
import com.market.analysis.infrastructure.persistence.entity.TickerEntity;

@Component
public class TickerMapper {

    public TickerEntity toEntity(TickerData domain) {
        if (domain == null)
            return null;

        TickerEntity entity = new TickerEntity();
        entity.setTicker(domain.getTicker());
        entity.setCurrentPrice(domain.getCurrentPrice());
        entity.setOpenPrice(domain.getOpenPrice());
        entity.setHighOfDay(domain.getHighOfDay());
        entity.setLowOfDay(domain.getLowOfDay());
        entity.setPreviousClose(domain.getPreviousClose());
        entity.setSma20(domain.getSma20());
        entity.setSma50(domain.getSma50());
        entity.setSma200(domain.getSma200());
        entity.setVolume(domain.getVolume());

        return entity;
    }

    public TickerData toDomain(TickerEntity entity) {
        if (entity == null)
            return null;

        return TickerData.builder()
                .ticker(entity.getTicker())
                .currentPrice(entity.getCurrentPrice())
                .openPrice(entity.getOpenPrice())
                .highOfDay(entity.getHighOfDay())
                .lowOfDay(entity.getLowOfDay())
                .previousClose(entity.getPreviousClose())
                .previousClose(entity.getPreviousClose())
                .sma20(entity.getSma20())
                .sma50(entity.getSma50())
                .sma200(entity.getSma200())
                .volume(entity.getVolume())
                .averageVolume(entity.getAverageVolume())
                .lastUpdated(entity.getLastUpdated())
                .build();
    }
}
