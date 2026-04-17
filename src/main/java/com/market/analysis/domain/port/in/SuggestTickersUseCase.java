package com.market.analysis.domain.port.in;

import com.market.analysis.application.dto.SuggestTickersRequestDTO;
import com.market.analysis.application.dto.SuggestTickersResponseDTO;

/**
 * Inbound use case for suggesting market tickers using Finviz contracts.
 */
public interface SuggestTickersUseCase {

    SuggestTickersResponseDTO suggestTickers(SuggestTickersRequestDTO request);
}
