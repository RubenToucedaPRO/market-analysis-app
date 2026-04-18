package com.market.analysis.domain.port.out;

import java.util.List;

/**
 * Outbound port for searching market tickers in Finviz.
 */
public interface FinvizScreenerPort {

    List<String> findTickers(String filters, int maxResults);
}
