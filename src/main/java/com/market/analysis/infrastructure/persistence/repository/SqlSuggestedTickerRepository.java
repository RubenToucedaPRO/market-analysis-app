package com.market.analysis.infrastructure.persistence.repository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.market.analysis.domain.port.out.SuggestedTickerRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SqlSuggestedTickerRepository  implements SuggestedTickerRepository {

    private final JpaSuggestedTickerRepository jpaSuggestedTickerRepository;

    @Override
    @Transactional
    public void deleteByStrategyIdAndTicker(Long strategyId,String ticker) {
        jpaSuggestedTickerRepository.deleteByStrategyIdAndTicker(strategyId,ticker);
    }
    
}
