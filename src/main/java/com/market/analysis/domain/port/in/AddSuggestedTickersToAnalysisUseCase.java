package com.market.analysis.domain.port.in;

public interface AddSuggestedTickersToAnalysisUseCase {

    int addFromLatestSnapshot(Long strategyId);

    int refreshFromSuggestionSnapshot(Long strategyId);
}
