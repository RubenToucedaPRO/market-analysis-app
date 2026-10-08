package com.market.analysis.unit.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.service.PromptResponseValidator;

@DisplayName("PromptResponseValidator Domain Service Tests")
class PromptResponseValidatorTest {

    private final PromptResponseValidator validator = new PromptResponseValidator();

    @Test
    @DisplayName("Should validate response containing all required sections")
    void shouldValidateResponseContainingAllRequiredSections() {
        String response = """
                Resumen técnico: Tendencia alcista moderada con precio por encima de SMA20 y SMA50.
                Fortalezas: Precio sobre SMA20 y SMA50 con volumen ligeramente por encima de media.
                Riesgos: Volumen ligeramente por debajo de media en las últimas sesiones diarias.
                Conclusión interpretativa: El contexto es favorable pero con cautela por el volumen.
                """;

        assertThat(validator.isValid(response)).isTrue();
    }

    @Test
    @DisplayName("Should invalidate response missing required sections")
    void shouldInvalidateResponseMissingRequiredSections() {
        String response = """
                Resumen técnico: Tendencia lateral.
                Fortalezas: Volumen alto.
                """;

        assertThat(validator.isValid(response)).isFalse();
    }

    @Test
    @DisplayName("Should accept markdown bold and headings around sections")
    void shouldAcceptMarkdownVariants() {
        String response = """
                **Resumen técnico:** Tendencia alcista moderada con precio sobre las medias móviles.
                ## Fortalezas:
                Precio sobre SMA20 y SMA50 con volumen ligeramente por encima de media diaria.
                > Riesgos: Volumen bajo en las últimas sesiones con falta de convicción compradora.
                `Conclusión interpretativa:` Favorable con cautela por el bajo volumen negociado.
                """;

        assertThat(validator.isValid(response)).isTrue();
    }

    @Test
    @DisplayName("Should accept case and accent variants of sections")
    void shouldAcceptCaseAndAccentVariants() {
        String response = """
                RESUMEN TÉCNICO: Tendencia alcista moderada con precio sobre las medias móviles.
                fortalezas: Precio sobre SMA20 y SMA50 con volumen por encima de media diaria.
                Riesgos: Volumen bajo en las últimas sesiones con falta de convicción compradora.
                Conclusion interpretativa: Favorable con cautela por el bajo volumen negociado.
                """;

        assertThat(validator.isValid(response)).isTrue();
    }

    @Test
    @DisplayName("Should still reject invented section names")
    void shouldRejectInventedSectionNames() {
        String response = """
                Resumen: Tendencia lateral.
                Cosas buenas: Volumen alto.
                Peligros: Ninguno.
                Veredicto: Comprar.
                """;

        assertThat(validator.isValid(response)).isFalse();
    }

    @Test
    @DisplayName("Should reject blank responses")
    void testBlankResponses() {
        assertThat(validator.isValid(null)).isFalse();
        assertThat(validator.isValid("   ")).isFalse();
    }

    @Test
    @DisplayName("Should reject echoed prompt instructions crammed under the last header")
    void shouldRejectEchoedPromptInstructions() {
        String echo = """
                Resumen técnico: brief analysis of price and trend.
                Fortalezas: positive factors with numeric data.
                Riesgos: negative factors with numeric data.
                Conclusión interpretativa: overall assessment. Must be exactly those four lines.
                Let's craft. Resumen técnico: Precio 336.17 por encima de SMA20. Fortalezas: medias alineadas.
                """;

        assertThat(validator.isValid(echo)).isFalse();
    }

    @Test
    @DisplayName("Should build retry prompt with strict section instructions")
    void shouldBuildRetryPromptWithStrictSectionInstructions() {
        String retryPrompt = validator.buildRetryPrompt("Base prompt");

        assertThat(retryPrompt)
                .contains("Base prompt")
                .contains("Responde SOLO con estas 4 líneas")
                .contains("Conclusión interpretativa:");
    }

    @Test
    @DisplayName("Should reject null base prompt when building retry prompt")
    void shouldRejectNullBasePromptWhenBuildingRetryPrompt() {
        assertThatThrownBy(() -> validator.buildRetryPrompt(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Base prompt cannot be null");
    }
}
