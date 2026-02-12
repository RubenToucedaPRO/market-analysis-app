package com.market.analysis.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.infrastructure.persistence.entity.StockEntity;

@Component
public class StockMapper {

    public StockEntity toEntity(Stock domain) {
        if (domain == null)
            return null;

        StockEntity entity = new StockEntity();
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
        entity.setStrategyId(domain.getStrategyId());
        entity.setAverageVolume(domain.getAverageVolume());
        entity.setLastUpdated(domain.getLastUpdated());

        return entity;
    }

    public Stock toDomain(StockEntity entity) {
        if (entity == null)
            return null;

        return Stock.builder()
                .ticker(entity.getTicker())
                .logoUrl(entity.getCompanyProfile().getLogo())
                .currentPrice(entity.getCurrentPrice())
                .openPrice(entity.getOpenPrice())
                .highOfDay(entity.getHighOfDay())
                .lowOfDay(entity.getLowOfDay())
                .previousClose(entity.getPreviousClose())
                .sma20(entity.getSma20())
                .sma50(entity.getSma50())
                .sma200(entity.getSma200())
                .volume(entity.getVolume())
                .averageVolume(entity.getAverageVolume())
                .lastUpdated(entity.getLastUpdated())
                .strategyId(entity.getStrategyId())
                .build();
    }
}
