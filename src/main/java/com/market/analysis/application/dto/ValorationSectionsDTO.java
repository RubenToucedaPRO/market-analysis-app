package com.market.analysis.application.dto;

import java.util.List;

import com.market.analysis.domain.service.ValorationSections;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * View model with an AI valuation already split into its four contract
 * sections. Built with the same boundary rule the validator enforces, so
 * the view can never assign chunks to the wrong card.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValorationSectionsDTO {

    private boolean structured;
    private String resumen;
    private String fortalezas;
    private String riesgos;
    private String conclusion;
    /**
     * Raw stored text, always populated. Shown as-is when the valuation is not
     * structured (e.g. the friendly fallback persisted when every LLM failed),
     * so the view never renders an empty card.
     */
    private String raw;

    public static ValorationSectionsDTO from(String raw) {
        List<String> parts = ValorationSections.split(raw);
        boolean structured = ValorationSections.isStructured(raw);
        return ValorationSectionsDTO.builder()
                .structured(structured)
                .resumen(structured ? parts.get(0) : "")
                .fortalezas(structured ? parts.get(1) : "")
                .riesgos(structured ? parts.get(2) : "")
                .conclusion(structured ? parts.get(3) : "")
                .raw(raw)
                .build();
    }
}
