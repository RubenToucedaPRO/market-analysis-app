package com.market.analysis.domain.service;

import java.math.BigDecimal;
import java.util.Locale;

import com.market.analysis.domain.exception.DomainErrorCodes;
import com.market.analysis.domain.exception.DomainValidationException;
import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StrategyEvaluation;

public class PromptBuilder {

    private static final String NOT_AVAILABLE = "N/A";

    private static final String PROMPT_TEMPLATE = """
            Eres un analista financiero experto. Responde en español, sin texto introductorio ni despedida.

            Datos:
            Ticker: %s | Precio: %s
            SMA20: %s | SMA50: %s | SMA200: %s
            Volumen: %s (Media: %s)
            Estrategia: %s | Cumplimiento: %s%%
            Resumen estrategia: %s
            R:R: %s | Target: %s | Stop: %s

            Responde con exactamente estas 4 secciones:
            Resumen técnico: breve análisis del precio y tendencia.
            Fortalezas: factores positivos con datos numéricos.
            Riesgos: factores negativos con datos numéricos.
            Conclusión interpretativa: valoración general.
            """;

    public String buildAnalysisPrompt(Stock stock, StrategyEvaluation evaluation) {
        if (stock == null) {
            throw new DomainValidationException(DomainErrorCodes.STOCK_NULL);
        }
        String ticker = safe(stock.getTicker());

        return PROMPT_TEMPLATE.formatted(
                ticker,
                decimal(stock.getCurrentPrice()),
                decimal(stock.getSma20()),
                decimal(stock.getSma50()),
                decimal(stock.getSma200()),
                whole(stock.getVolume()),
                whole(stock.getAverageVolume()),
                safe(evaluation == null ? null : evaluation.getStrategyName()),
                decimal(evaluation == null ? null : evaluation.getComplianceRate()),
                safe(evaluation == null ? null : evaluation.getSummary()),
                decimal(evaluation == null ? null : evaluation.getRiskRewardRatio()),
                decimal(evaluation == null ? null : evaluation.getTargetPrice()),
                decimal(evaluation == null ? null : evaluation.getStopLossPrice()));
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? NOT_AVAILABLE : value;
    }

    private String decimal(BigDecimal value) {
        return value == null ? NOT_AVAILABLE : String.format(Locale.ENGLISH, "%.2f", value);
    }

    private String whole(Long value) {
        return value == null ? NOT_AVAILABLE : String.valueOf(value);
    }
}
