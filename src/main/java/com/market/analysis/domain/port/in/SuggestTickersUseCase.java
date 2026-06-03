package com.market.analysis.domain.port.in;

import java.util.Optional;

import com.market.analysis.application.dto.SuggestTickersRequestDTO;
import com.market.analysis.application.dto.SuggestTickersResponseDTO;

/**
 * Inbound use case for suggesting market tickers using Finviz contracts.
 */
public interface SuggestTickersUseCase {

    SuggestTickersResponseDTO suggestTickers(SuggestTickersRequestDTO request);

    Optional<SuggestTickersResponseDTO> getLatestSuggestionSnapshot(Long strategyId);

    int convertSuggestedTickersToAnalysis(long strategyId);
}
