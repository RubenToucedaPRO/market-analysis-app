package com.market.analysis.unit.domain.port.out;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.port.out.FinvizScreenerPort;

@DisplayName("FinvizScreenerPort Contract Tests")
class FinvizScreenerPortContractTest {

    @Test
    @DisplayName("Should return tickers from filter expression contract")
    void shouldReturnTickersFromFilterExpression() {
        FinvizScreenerPort port = (filters, maxResults) -> List.of("AAPL", "MSFT").stream()
                .limit(maxResults)
                .toList();

        List<String> tickers = port.findTickers("ta_sma20_pa", 1);

        assertThat(tickers).containsExactly("AAPL");
    }
}
