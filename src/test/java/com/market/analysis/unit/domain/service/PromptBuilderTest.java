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
                .contains("Estrategia: Momentum (Cumplimiento: 80.50%)")
                .contains("R:R: 1.80 | Target: 195.00 | Stop: 170.00")
                .contains("responde en español")
                .contains("Resumen técnico:")
                .contains("Fortalezas:")
                .contains("Riesgos:")
                .contains("Conclusión interpretativa:")
                .contains("Justifica cada punto con los datos numéricos provistos.");
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
                .contains("Estrategia: N/A (Cumplimiento: N/A%)")
                .contains("R:R: N/A | Target: N/A | Stop: N/A")
                .contains("Justifica cada punto con los datos numéricos provistos.");
    }

    @Test
    @DisplayName("Should throw exception when stock is null")
    void shouldThrowExceptionWhenStockIsNull() {
        assertThatThrownBy(() -> promptBuilder.buildAnalysisPrompt(null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Stock cannot be null");
    }
}
