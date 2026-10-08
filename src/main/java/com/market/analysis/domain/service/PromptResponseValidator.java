package com.market.analysis.domain.service;

import java.util.Objects;

public class PromptResponseValidator {

    private static final String STRICT_RETRY_SUFFIX = """
            IMPORTANTE:
            Responde SOLO con estas 4 líneas, sin ningún otro texto:
            Resumen técnico:
            Fortalezas:
            Riesgos:
            Conclusión interpretativa:
            No añadas razonamiento, cadenas de pensamiento, ni texto fuera de esas secciones.
            """;

    /**
     * A response is valid only when it carries the four contract sections,
     * each opening its own line with a non-trivial body. See
     * {@link ValorationSections} for the shared boundary rule (also used
     * when the view splits the persisted text into cards).
     */
    public boolean isValid(String response) {
        return ValorationSections.isStructured(response);
    }

    public String buildRetryPrompt(String basePrompt) {
        return Objects.requireNonNull(basePrompt, "Base prompt cannot be null") + STRICT_RETRY_SUFFIX;
    }
}
