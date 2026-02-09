package com.market.analysis.infrastructure.external.finnhub;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.TickerData;
import com.market.analysis.infrastructure.external.finnhub.dto.QuoteData;
import com.market.analysis.infrastructure.persistence.entity.TickerEntity;

@Component
public class FinnhubMapper {

    public TickerEntity toEntity(QuoteData quote) {
        if (quote == null || !quote.isValid()) {
            return null;
        }
        TickerEntity entity = new TickerEntity();
        entity.setTicker(quote.getSymbol());
        entity.setCurrentPrice(quote.getC());
        entity.setOpenPrice(quote.getO());
        entity.setHighOfDay(quote.getH());
        entity.setLowOfDay(quote.getL());
        entity.setPreviousClose(quote.getPc());
        entity.setLastUpdated(LocalDateTime.ofEpochSecond(quote.getT(), 0, ZoneOffset.UTC));
        return entity;
    }

    public TickerData toDomain(QuoteData quote) {
        if (quote == null || !quote.isValid()) {
            return null;
        }
        return TickerData.builder()
                .ticker(quote.getSymbol())
                .currentPrice(quote.getC())
                .openPrice(quote.getO())
                .highOfDay(quote.getH())
                .lowOfDay(quote.getL())
                .previousClose(quote.getPc())
                .lastUpdated(LocalDateTime.ofEpochSecond(quote.getT(), 0, ZoneOffset.UTC))
                .build();
    }

}
