package com.market.analysis.unit.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.FinvizFilterMappingResult;

@DisplayName("FinvizFilterMappingResult Contract Tests")
class FinvizFilterMappingResultTest {

    @Test
    @DisplayName("Should keep defensive copies for unmappable rules and warnings")
    void shouldKeepDefensiveCopies() {
        List<String> unmappable = new ArrayList<>(List.of("RSI(14)"));
        List<String> warnings = new ArrayList<>(List.of("Rule RSI(14) marked as UNMAPPABLE"));

        FinvizFilterMappingResult result = FinvizFilterMappingResult.builder()
                .filters("ta_sma20_pa")
                .unmappableRules(unmappable)
                .warnings(warnings)
                .build();

        unmappable.add("EMA(9)");
        warnings.clear();

        assertThat(result.getUnmappableRules()).containsExactly("RSI(14)");
        assertThat(result.getWarnings()).containsExactly("Rule RSI(14) marked as UNMAPPABLE");
        assertThat(result.hasUnmappableRules()).isTrue();
    }

    @Test
    @DisplayName("Should expose empty lists when warnings or unmappable rules are null")
    void shouldExposeEmptyCollectionsWhenNull() {
        FinvizFilterMappingResult result = FinvizFilterMappingResult.builder()
                .filters("ta_sma20_pa")
                .build();

        assertThat(result.getUnmappableRules()).isEmpty();
        assertThat(result.getWarnings()).isEmpty();
        assertThat(result.hasUnmappableRules()).isFalse();
    }
}
