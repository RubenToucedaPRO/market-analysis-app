package com.market.analysis.unit.infrastructure.migration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.domain.port.out.RuleDefinitionRepository;
import com.market.analysis.infrastructure.migration.RuleDefinitionSanitizationRunner;

/**
 * Unit tests for {@link RuleDefinitionSanitizationRunner}.
 */
@DisplayName("RuleDefinitionSanitizationRunner Unit Tests")
@ExtendWith(MockitoExtension.class)
class RuleDefinitionSanitizationRunnerTest {

    @Mock
    private RuleDefinitionRepository ruleDefinitionRepository;

    @InjectMocks
    private RuleDefinitionSanitizationRunner runner;

    @Test
    @DisplayName("Should delete rule definitions with unsupported codes")
    void testRemovesUnsupportedCode() throws Exception {
        RuleDefinition invalid = RuleDefinition.builder()
                .id(1L).code("VWAP").name("VWAP").requiresParam(false).build();

        when(ruleDefinitionRepository.findAll()).thenReturn(List.of(invalid));

        runner.run();

        verify(ruleDefinitionRepository, times(1)).deleteById(1L);
        verify(ruleDefinitionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should not touch valid rule definitions with correct requiresParam")
    void testLeavesValidDefinitionUnchanged() throws Exception {
        RuleDefinition validSma = RuleDefinition.builder()
                .id(2L).code("SMA").name("Simple Moving Average").requiresParam(true).build();

        when(ruleDefinitionRepository.findAll()).thenReturn(List.of(validSma));

        runner.run();

        verify(ruleDefinitionRepository, never()).deleteById(any());
        verify(ruleDefinitionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should correct requiresParam when it conflicts with the catalog (SMA: false -> true)")
    void testCorrectsRequiresParamMismatch() throws Exception {
        RuleDefinition wrongFlag = RuleDefinition.builder()
                .id(3L).code("SMA").name("Simple Moving Average")
                .requiresParam(false) // wrong – SMA requires a param
                .build();

        when(ruleDefinitionRepository.findAll()).thenReturn(List.of(wrongFlag));
        when(ruleDefinitionRepository.save(any())).thenReturn(wrongFlag);

        runner.run();

        verify(ruleDefinitionRepository, never()).deleteById(any());
        verify(ruleDefinitionRepository, times(1)).save(
                argThat(saved -> saved.getCode().equals("SMA") && saved.isRequiresParam()));
    }

    @Test
    @DisplayName("Should correct requiresParam when it conflicts with the catalog (PRICE: true -> false)")
    void testCorrectsRequiresParamForNoParamIndicator() throws Exception {
        RuleDefinition wrongFlag = RuleDefinition.builder()
                .id(4L).code("PRICE").name("Current Price")
                .requiresParam(true) // wrong – PRICE has no param
                .build();

        when(ruleDefinitionRepository.findAll()).thenReturn(List.of(wrongFlag));
        when(ruleDefinitionRepository.save(any())).thenReturn(wrongFlag);

        runner.run();

        verify(ruleDefinitionRepository, never()).deleteById(any());
        verify(ruleDefinitionRepository, times(1)).save(
                argThat(saved -> saved.getCode().equals("PRICE") && !saved.isRequiresParam()));
    }

    @Test
    @DisplayName("Should handle empty repository without errors")
    void testRunsWithEmptyRepository() throws Exception {
        when(ruleDefinitionRepository.findAll()).thenReturn(List.of());

        runner.run();

        verify(ruleDefinitionRepository, never()).deleteById(any());
        verify(ruleDefinitionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should process multiple definitions – remove invalid, correct mismatched, leave valid")
    void testProcessesMixedDefinitions() throws Exception {
        RuleDefinition invalid = RuleDefinition.builder()
                .id(1L).code("STOCH").name("Stochastic").requiresParam(true).build();
        RuleDefinition wrongFlag = RuleDefinition.builder()
                .id(2L).code("EMA").name("EMA").requiresParam(false).build();
        RuleDefinition valid = RuleDefinition.builder()
                .id(3L).code("PRICE").name("Price").requiresParam(false).build();

        when(ruleDefinitionRepository.findAll()).thenReturn(List.of(invalid, wrongFlag, valid));
        when(ruleDefinitionRepository.save(any())).thenReturn(wrongFlag);

        runner.run();

        verify(ruleDefinitionRepository, times(1)).deleteById(1L);
        verify(ruleDefinitionRepository, times(1)).save(
                argThat(saved -> saved.getCode().equals("EMA") && saved.isRequiresParam()));
        // valid should not be touched
        verify(ruleDefinitionRepository, never()).deleteById(3L);
    }
}
