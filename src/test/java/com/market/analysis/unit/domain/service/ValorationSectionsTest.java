package com.market.analysis.unit.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.service.ValorationSections;

@DisplayName("ValorationSections Domain Service Tests")
class ValorationSectionsTest {

    private static final String VALID = """
            Resumen técnico: Precio 336.17 por encima de SMA20 (333.41), tendencia alcista en varios marcos.
            Fortalezas: Precio sobre SMA20 (+0,8%) y SMA50 (+4,3%) con medias alineadas al alza.
            Riesgos: Volumen 27% por debajo de la media, falta de convicción compradora evidente.
            Conclusión interpretativa: Contexto favorable pero con cautela por el bajo volumen negociado.
            """;

    @Test
    @DisplayName("Should recognize a well-formed valuation")
    void shouldRecognizeWellFormedValuation() {
        assertThat(ValorationSections.isStructured(VALID)).isTrue();

        List<String> parts = ValorationSections.split(VALID);

        assertThat(parts).hasSize(4);
        assertThat(parts.get(0)).contains("336.17");
        assertThat(parts.get(1)).contains("SMA20");
        assertThat(parts.get(2)).contains("Volumen");
        assertThat(parts.get(3)).contains("cautela");
    }

    @Test
    @DisplayName("Should reject echoed prompt instructions crammed under the last header")
    void shouldRejectEchoedPromptInstructions() {
        String echo = """
                Resumen técnico: brief analysis of price and trend.
                Fortalezas: positive factors with numeric data.
                Riesgos: negative factors with numeric data.
                Conclusión interpretativa: overall assessment. No extra text. Must be exactly those four lines.
                Let's craft. Resumen técnico: Precio 336.17 por encima de SMA20. Fortalezas: medias alineadas.
                """;

        assertThat(ValorationSections.isStructured(echo)).isFalse();
    }

    @Test
    @DisplayName("Should accept markdown, numbering and case variants opening lines")
    void shouldAcceptLineAnchoredVariants() {
        String response = """
                **Resumen técnico:** Precio 336.17 por encima de SMA20 (333.41), tendencia alcista en varios marcos.
                2. Fortalezas: Precio sobre SMA20 (+0,8%) y SMA50 (+4,3%) con medias alineadas al alza.
                ## RIESGOS: Volumen 27% por debajo de la media, falta de convicción compradora evidente.
                Conclusion interpretativa: Contexto favorable pero con cautela por el bajo volumen negociado.
                """;

        assertThat(ValorationSections.isStructured(response)).isTrue();

        List<String> parts = ValorationSections.split(response);

        assertThat(parts.get(0)).contains("336.17");
        assertThat(parts.get(1)).contains("SMA20");
    }

    @Test
    @DisplayName("Should reject headers buried mid-sentence")
    void shouldRejectMidSentenceHeaders() {
        String response = """
                El análisis menciona Resumen técnico: Fortalezas: Riesgos: Conclusión interpretativa: todo seguido
                sin ninguna estructura real de secciones separadas en líneas distintas del informe generado.
                """;

        assertThat(ValorationSections.isStructured(response)).isFalse();
    }

    @Test
    @DisplayName("Should reject missing headers and blank input")
    void shouldRejectMissingHeadersAndBlank() {
        assertThat(ValorationSections.isStructured("Solo texto sin cabeceras relevantes para el informe pedido.")).isFalse();
        assertThat(ValorationSections.isStructured(null)).isFalse();
        assertThat(ValorationSections.isStructured("   ")).isFalse();
        assertThat(ValorationSections.split(null)).containsExactly("", "", "", "");
    }
}
