package com.market.analysis.domain.service;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

import com.market.analysis.domain.model.Stock;
import com.market.analysis.domain.model.StrategyEvaluation;

public class PromptBuilder {

    private static final String NOT_AVAILABLE = "N/A";
    private static final Locale SPANISH_LOCALE = Locale.forLanguageTag("es-ES");

    public String buildAnalysisPrompt(Stock stock, StrategyEvaluation evaluation) {
        Stock safeStock = Objects.requireNonNull(stock, "Stock cannot be null");
        String ticker = safe(safeStock.getTicker());

        return """
                Actúa como experto analista financiero. Análisis interpretativo, no consejo financiero. 
                Analiza este snapshot y responde en español.

                DATOS:
                Ticker: %s | Precio: %s
                Medias: SMA20:%s, SMA50:%s, SMA200:%s
                Volumen: %s (Media: %s)
                Estrategia: %s (Cumplimiento: %s%%)
                Resumen Estrategia: %s
                R:R: %s | Target: %s | Stop: %s

                REGLAS DE RESPUESTA:
                1. Usa exactamente las secciones: "Resumen técnico:", "Fortalezas:", "Riesgos:" y "Conclusión interpretativa:".
                2. Sé breve (máximo 2 frases por sección).
                3. Justifica cada punto con los datos numéricos provistos.
                4. No añadas introducciones ni despedidas.
                """.formatted(
                ticker,
                decimal(safeStock.getCurrentPrice()),
                decimal(safeStock.getSma20()),
                decimal(safeStock.getSma50()),
                decimal(safeStock.getSma200()),
                whole(safeStock.getVolume()),
                whole(safeStock.getAverageVolume()),
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
        return value == null ? NOT_AVAILABLE : String.format(SPANISH_LOCALE,"%.2f", value);
    }

    private String whole(Long value) {
        return value == null ? NOT_AVAILABLE : String.valueOf(value);
    }
}
