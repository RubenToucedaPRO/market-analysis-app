package com.market.analysis.domain.service;

import java.util.List;
import java.util.Objects;

public class PromptResponseValidator {

    private static final List<String> REQUIRED_SECTIONS = List.of(
            "Resumen técnico:",
            "Fortalezas:",
            "Riesgos:",
            "Conclusión interpretativa:");

    private static final String STRICT_RETRY_SUFFIX = """
            IMPORTANTE:
            Responde SOLO con estas 4 líneas, sin ningún otro texto:
            Resumen técnico:
            Fortalezas:
            Riesgos:
            Conclusión interpretativa:
            No añadas razonamiento, cadenas de pensamiento, ni texto fuera de esas secciones.
            """;

    public boolean isValid(String response) {
        if (response == null || response.isBlank()) {
            return false;
        }
        return REQUIRED_SECTIONS.stream().allMatch(response::contains);
    }

    public String buildRetryPrompt(String basePrompt) {
        return Objects.requireNonNull(basePrompt, "Base prompt cannot be null") + STRICT_RETRY_SUFFIX;
    }
}
