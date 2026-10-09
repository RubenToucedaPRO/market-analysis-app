package com.market.analysis.unit.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.application.dto.ValorationSectionsDTO;

@DisplayName("ValorationSectionsDTO Tests")
class ValorationSectionsDTOTest {

    private static final String STRUCTURED = """
            Resumen técnico: Precio 336.17 por encima de SMA20, tendencia alcista clara.
            Fortalezas: Precio sobre SMA20 y medias alineadas al alza.
            Riesgos: Volumen por debajo de la media, falta de convicción compradora.
            Conclusión interpretativa: Contexto favorable pero con cautela por el bajo volumen negociado.
            """;

    private static final String FALLBACK =
            "No se pudo generar una valoración interpretativa válida en este momento. Reintenta más tarde.";

    @Test
    @DisplayName("Should split a structured valuation and keep the raw text")
    void shouldSplitStructuredValuation() {
        ValorationSectionsDTO dto = ValorationSectionsDTO.from(STRUCTURED);

        assertThat(dto.isStructured()).isTrue();
        assertThat(dto.getResumen()).contains("336.17");
        assertThat(dto.getRaw()).isEqualTo(STRUCTURED);
    }

    @Test
    @DisplayName("Should expose fallback text as raw when not structured")
    void shouldExposeFallbackAsRaw() {
        ValorationSectionsDTO dto = ValorationSectionsDTO.from(FALLBACK);

        assertThat(dto.isStructured()).isFalse();
        assertThat(dto.getResumen()).isEmpty();
        assertThat(dto.getRaw()).isEqualTo(FALLBACK);
    }
}
