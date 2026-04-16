package com.market.analysis.unit.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StrategyEvaluation;
import com.market.analysis.domain.service.PromptBuilder;

@DisplayName("PromptBuilder Domain Service Tests")
class PromptBuilderTest {

    private final PromptBuilder promptBuilder = new PromptBuilder();

    @Test
    @DisplayName("Should build prompt with stock and evaluation data")
    void shouldBuildPromptWithStockAndEvaluationData() {
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .currentPrice(new BigDecimal("180.20"))
                .sma20(new BigDecimal("175.10"))
                .sma50(new BigDecimal("170.00"))
                .sma200(new BigDecimal("160.00"))
                .volume(1000000L)
                .averageVolume(900000L)
                .build();

        StrategyEvaluation evaluation = StrategyEvaluation.builder()
                .strategyName("Momentum")
                .complianceRate(new BigDecimal("80.50"))
                .summary("Mostly compliant")
                .riskRewardRatio(new BigDecimal("1.80"))
                .targetPrice(new BigDecimal("195.00"))
                .stopLossPrice(new BigDecimal("170.00"))
                .build();

        String prompt = promptBuilder.buildAnalysisPrompt(stock, evaluation);

        assertThat(prompt)
                .contains("Ticker: AAPL")
                .contains("Strategy Name: Momentum")
                .contains("Compliance Rate: 80.50")
                .contains("Risk/Reward Ratio: 1.80")
                .contains("respond in Spanish")
                .contains("Resumen técnico:")
                .contains("Fortalezas:")
                .contains("Riesgos:")
                .contains("Conclusión interpretativa:")
                .contains("Example response 1:")
                .contains("Example response 2:");
    }

    @Test
    @DisplayName("Should use N/A placeholders when optional values are missing")
    void shouldUseNaPlaceholdersWhenOptionalValuesAreMissing() {
        Stock stock = Stock.builder()
                .ticker("AAPL")
                .build();

        String prompt = promptBuilder.buildAnalysisPrompt(stock, null);

        assertThat(prompt)
                .contains("Ticker: AAPL")
                .contains("Strategy Name: N/A")
                .contains("Compliance Rate: N/A")
                .contains("Risk/Reward Ratio: N/A")
                .contains("Target Price: N/A")
                .contains("Stop Loss Price: N/A")
                .contains("brief and verifiable justification");
    }

    @Test
    @DisplayName("Should throw exception when stock is null")
    void shouldThrowExceptionWhenStockIsNull() {
        assertThatThrownBy(() -> promptBuilder.buildAnalysisPrompt(null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Stock cannot be null");
    }
}
