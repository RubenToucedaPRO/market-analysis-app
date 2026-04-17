package com.market.analysis.unit.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.FinvizFilterMappingResult;
import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.service.FinvizFilterMapper;

@DisplayName("FinvizFilterMapper Domain Service Tests")
class FinvizFilterMapperTest {

    private final FinvizFilterMapper mapper = new FinvizFilterMapper();

    @Test
    @DisplayName("Should map supported rules to Finviz filters")
    void shouldMapSupportedRulesToFinvizFilters() {
        List<Rule> rules = List.of(
                rule("PRICE", null, ">", "SMA", 20.0),
                rule("VOLUME", null, ">", "AVG_VOLUME", null),
                rule("SMA", 20.0, ">", "SMA", 50.0));

        FinvizFilterMappingResult result = mapper.map(rules);

        assertThat(result.getFilters()).isEqualTo("ta_sma20_pa,sh_relvol_o1,ta_sma20_sa50");
        assertThat(result.getUnmappableRules()).isEmpty();
        assertThat(result.getWarnings()).isEmpty();
        assertThat(result.hasUnmappableRules()).isFalse();
    }

    @Test
    @DisplayName("Should support evaluator operator aliases")
    void shouldSupportEvaluatorOperatorAliases() {
        List<Rule> rules = List.of(
                rule("PRICE", null, "GREATER_THAN", "SMA", 50.0),
                rule("SMA", 50.0, "LESS_THAN", "SMA", 200.0));

        FinvizFilterMappingResult result = mapper.map(rules);

        assertThat(result.getFilters()).isEqualTo("ta_sma50_pa,ta_sma50_sb200");
        assertThat(result.getUnmappableRules()).isEmpty();
        assertThat(result.getWarnings()).isEmpty();
    }

    @Test
    @DisplayName("Should mark unsupported combinations as unmappable")
    void shouldMarkUnsupportedCombinationsAsUnmappable() {
        List<Rule> rules = List.of(
                rule("PRICE", null, ">=", "SMA", 20.0),
                rule("RSI", 14.0, ">", "CONSTANT", 70.0));

        FinvizFilterMappingResult result = mapper.map(rules);

        assertThat(result.getFilters()).isEmpty();
        assertThat(result.getUnmappableRules())
                .containsExactly("PRICE >= SMA(20)", "RSI(14) > CONSTANT(70)");
        assertThat(result.getWarnings())
                .containsExactly(
                        "Rule 'PRICE >= SMA(20)' cannot be mapped to Finviz filters.",
                        "Rule 'RSI(14) > CONSTANT(70)' cannot be mapped to Finviz filters.");
        assertThat(result.hasUnmappableRules()).isTrue();
    }

    @Test
    @DisplayName("Should deduplicate repeated mapped filters")
    void shouldDeduplicateRepeatedMappedFilters() {
        List<Rule> rules = List.of(
                rule("PRICE", null, ">", "SMA", 20.0),
                rule("PRICE", null, ">", "SMA", 20.0));

        FinvizFilterMappingResult result = mapper.map(rules);

        assertThat(result.getFilters()).isEqualTo("ta_sma20_pa");
        assertThat(result.getUnmappableRules()).isEmpty();
        assertThat(result.getWarnings()).isEmpty();
    }

    @Test
    @DisplayName("Should handle null and empty rule lists")
    void shouldHandleNullAndEmptyRuleLists() {
        FinvizFilterMappingResult nullResult = mapper.map(null);
        FinvizFilterMappingResult emptyResult = mapper.map(List.of());

        assertThat(nullResult.getFilters()).isEmpty();
        assertThat(nullResult.getUnmappableRules()).isEmpty();
        assertThat(nullResult.getWarnings()).isEmpty();
        assertThat(emptyResult.getFilters()).isEmpty();
        assertThat(emptyResult.getUnmappableRules()).isEmpty();
        assertThat(emptyResult.getWarnings()).isEmpty();
    }

    @Test
    @DisplayName("Should mark null rules as unmappable")
    void shouldMarkNullRulesAsUnmappable() {
        List<Rule> rules = new ArrayList<>();
        rules.add(rule("PRICE", null, "<", "SMA", 200.0));
        rules.add(null);

        FinvizFilterMappingResult result = mapper.map(rules);

        assertThat(result.getFilters()).isEqualTo("ta_sma200_pb");
        assertThat(result.getUnmappableRules()).containsExactly("NULL_RULE");
        assertThat(result.getWarnings()).containsExactly("Rule 'NULL_RULE' cannot be mapped to Finviz filters.");
    }

    private Rule rule(String subjectCode, Double subjectParam, String operator, String targetCode, Double targetParam) {
        return Rule.builder()
                .subjectCode(subjectCode)
                .subjectParam(subjectParam)
                .operator(operator)
                .targetCode(targetCode)
                .targetParam(targetParam)
                .build();
    }
}
