package com.market.analysis.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.Candle;
import com.market.analysis.infrastructure.persistence.entity.CandleEntity;

@Component
public class CandleMapper {

    public CandleEntity toEntity(Candle candle) {
        if (candle == null) {
            return null;
        }
        CandleEntity entity = new CandleEntity();
        entity.setTicker(candle.getTicker());
        entity.setDateTime(candle.getDateTime());
        entity.setOpenPrice(candle.getOpenPrice());
        entity.setHighPrice(candle.getHighPrice());
        entity.setLowPrice(candle.getLowPrice());
        entity.setClosePrice(candle.getClosePrice());
        entity.setVolume(candle.getVolume());
        return entity;
    }

    public Candle toDomain(CandleEntity entity) {
        if (entity == null) {
            return null;
        }
        return Candle.builder()
                .ticker(entity.getTicker())
                .dateTime(entity.getDateTime())
                .openPrice(entity.getOpenPrice())
                .highPrice(entity.getHighPrice())
                .lowPrice(entity.getLowPrice())
                .closePrice(entity.getClosePrice())
                .volume(entity.getVolume())
                .build();
    }
}
