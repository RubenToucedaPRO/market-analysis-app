package com.market.analysis.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.infrastructure.persistence.entity.StockEntity;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockMapper {

    private final StrategyEvaluationMapper strategyEvaluationMapper;

    public StockEntity toEntity(Stock domain) {
        if (domain == null)
            return null;

        StockEntity entity = new StockEntity();
        entity.setId(domain.getId());
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
        entity.setOrigin(domain.getOrigin());
        entity.setAverageVolume(domain.getAverageVolume());
        entity.setLastUpdate(domain.getLastUpdated());
        entity.setStrategyEvaluation(domain.getStrategyEvaluation() != null
                ? strategyEvaluationMapper.toEntity(domain.getStrategyEvaluation())
                : null);
        entity.setValorationIA(domain.getValorationIA());

        // EMA
        entity.setEma9(domain.getEma9());
        entity.setEma12(domain.getEma12());
        entity.setEma20(domain.getEma20());
        entity.setEma26(domain.getEma26());
        entity.setEma50(domain.getEma50());
        entity.setEma200(domain.getEma200());

        // RSI
        entity.setRsi14(domain.getRsi14());
        entity.setRsi30(domain.getRsi30());

        // MACD
        entity.setMacdLine(domain.getMacdLine());
        entity.setMacdSignal(domain.getMacdSignal());
        entity.setMacdHistogram(domain.getMacdHistogram());

        // Bollinger Bands
        entity.setBbUpper20(domain.getBbUpper20());
        entity.setBbLower20(domain.getBbLower20());

        // ATR
        entity.setAtr14(domain.getAtr14());

        if (entity.getCompanyProfile() != null) {
            entity.getCompanyProfile().setLogo(domain.getLogoUrl());
        }

        return entity;
    }

    public Stock toDomain(StockEntity entity) {
        if (entity == null)
            return null;
        if(entity.getStrategyEvaluation() != null) {
            entity.getStrategyEvaluation().setStock(entity);
        }

        return Stock.builder()
                .id(entity.getId())
                .ticker(entity.getTicker())
                .logoUrl(entity.getCompanyProfile() != null ? entity.getCompanyProfile().getLogo() : null)
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
                .lastUpdated(entity.getLastUpdate())
                .strategyId(entity.getStrategyId())
                .origin(entity.getOrigin())
                .strategyEvaluation(entity.getStrategyEvaluation() != null
                        ? strategyEvaluationMapper.toDomain(entity.getStrategyEvaluation())
                        : null)
                .valorationIA(entity.getValorationIA())
                // EMA
                .ema9(entity.getEma9())
                .ema12(entity.getEma12())
                .ema20(entity.getEma20())
                .ema26(entity.getEma26())
                .ema50(entity.getEma50())
                .ema200(entity.getEma200())
                // RSI
                .rsi14(entity.getRsi14())
                .rsi30(entity.getRsi30())
                // MACD
                .macdLine(entity.getMacdLine())
                .macdSignal(entity.getMacdSignal())
                .macdHistogram(entity.getMacdHistogram())
                // Bollinger Bands
                .bbUpper20(entity.getBbUpper20())
                .bbLower20(entity.getBbLower20())
                // ATR
                .atr14(entity.getAtr14())
                .build();
    }
}
