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
                Resumen técnico: Tendencia alcista moderada.
                Fortalezas: Precio sobre SMA20 y SMA50.
                Riesgos: Volumen ligeramente por debajo de media.
                Conclusión interpretativa: El contexto es favorable pero con cautela.
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
    @DisplayName("Should build retry prompt with strict section instructions")
    void shouldBuildRetryPromptWithStrictSectionInstructions() {
        String retryPrompt = validator.buildRetryPrompt("Base prompt");

        assertThat(retryPrompt)
                .contains("Base prompt")
                .contains("Devuelve exactamente estas secciones")
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
