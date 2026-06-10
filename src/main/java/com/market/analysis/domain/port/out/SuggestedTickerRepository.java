package com.market.analysis.domain.port.out;

public interface SuggestedTickerRepository {

    void deleteByStrategyIdAndTicker(Long strategyId,String ticker);
}
