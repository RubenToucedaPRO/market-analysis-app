package com.market.analysis.unit.application.usecase;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.application.dto.RuleDefinitionDTO;
import com.market.analysis.application.mapper.RuleDefinitionDTOMapper;
import com.market.analysis.application.usecase.ManageRuleDefinitionService;
import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.domain.port.out.RuleDefinitionRepository;

/**
 * P0 regression tests for ManageRuleDefinitionService.
 * Verifies that the canonical catalog is enforced when creating or updating
 * rule definitions, preventing unsupported codes from reaching the database.
 */
@DisplayName("ManageRuleDefinitionService P0 Catalog Validation Tests")
@ExtendWith(MockitoExtension.class)
class ManageRuleDefinitionServiceP0Test {

    @Mock
    private RuleDefinitionRepository ruleDefinitionRepository;

    @Mock
    private RuleDefinitionDTOMapper ruleDefinitionDTOMapper;

    @InjectMocks
    private ManageRuleDefinitionService service;

    // -------------------------------------------------------------------------
    // createRuleDefinition – catalog validation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should reject createRuleDefinition with unsupported code")
    void testCreateRejectsUnsupportedCode() {
        RuleDefinitionDTO dto = RuleDefinitionDTO.builder()
                .code("VWAP")
                .name("VWAP indicator")
                .requiresParam(false)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createRuleDefinition(dto));
        assertTrue(ex.getMessage().contains("VWAP"));
        verify(ruleDefinitionRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Should reject createRuleDefinition when requiresParam is inconsistent with catalog (SMA expects true)")
    void testCreateRejectsInconsistentRequiresParam() {
        RuleDefinitionDTO dto = RuleDefinitionDTO.builder()
                .code("SMA")
                .name("Simple Moving Average")
                .requiresParam(false) // wrong – SMA requires a param
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createRuleDefinition(dto));
        assertTrue(ex.getMessage().contains("requiresParam"));
    }

    @Test
    @DisplayName("Should reject createRuleDefinition when requiresParam is inconsistent with catalog (PRICE expects false)")
    void testCreateRejectsInconsistentRequiresParamForNoParamIndicator() {
        RuleDefinitionDTO dto = RuleDefinitionDTO.builder()
                .code("PRICE")
                .name("Current Price")
                .requiresParam(true) // wrong – PRICE has no param
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createRuleDefinition(dto));
        assertTrue(ex.getMessage().contains("requiresParam"));
    }

    @Test
    @DisplayName("Should accept createRuleDefinition for all valid no-param codes")
    void testCreateAcceptsValidNoParamCode() {
        for (String code : new String[]{"PRICE", "VOLUME", "OPEN", "HIGH", "LOW", "PREV_CLOSE",
                "MACD_LINE", "MACD_SIGNAL", "MACD_HIST", "AVG_VOLUME"}) {
            RuleDefinitionDTO dto = RuleDefinitionDTO.builder()
                    .code(code)
                    .name(code)
                    .requiresParam(false)
                    .build();
            when(ruleDefinitionRepository.existsByCode(code)).thenReturn(false);
            when(ruleDefinitionDTOMapper.toDomain(dto)).thenReturn(
                    RuleDefinition.builder().code(code).name(code).requiresParam(false).build());
            when(ruleDefinitionRepository.save(org.mockito.ArgumentMatchers.any()))
                    .thenReturn(RuleDefinition.builder().id(1L).code(code).name(code).requiresParam(false).build());
            when(ruleDefinitionDTOMapper.toDTO(org.mockito.ArgumentMatchers.any())).thenReturn(dto);

            // Should not throw
            service.createRuleDefinition(dto);
        }
    }

    @Test
    @DisplayName("Should accept createRuleDefinition for SMA with requiresParam=true")
    void testCreateAcceptsValidSma() {
        RuleDefinitionDTO dto = RuleDefinitionDTO.builder()
                .code("SMA")
                .name("Simple Moving Average")
                .requiresParam(true)
                .build();

        RuleDefinition domainObj = RuleDefinition.builder()
                .id(1L).code("SMA").name("Simple Moving Average").requiresParam(true).build();

        when(ruleDefinitionRepository.existsByCode("SMA")).thenReturn(false);
        when(ruleDefinitionDTOMapper.toDomain(dto)).thenReturn(domainObj);
        when(ruleDefinitionRepository.save(org.mockito.ArgumentMatchers.any())).thenReturn(domainObj);
        when(ruleDefinitionDTOMapper.toDTO(domainObj)).thenReturn(dto);

        // Should not throw
        service.createRuleDefinition(dto);
    }

    // -------------------------------------------------------------------------
    // updateRuleDefinition – catalog validation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should reject updateRuleDefinition with unsupported code")
    void testUpdateRejectsUnsupportedCode() {
        RuleDefinitionDTO dto = RuleDefinitionDTO.builder()
                .id(1L)
                .code("STOCH")
                .name("Stochastic Oscillator")
                .requiresParam(true)
                .build();

        when(ruleDefinitionRepository.existsById(1L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateRuleDefinition(dto));
        assertTrue(ex.getMessage().contains("STOCH"));
    }

    @Test
    @DisplayName("Should reject updateRuleDefinition when requiresParam conflicts with catalog")
    void testUpdateRejectsInconsistentRequiresParam() {
        RuleDefinitionDTO dto = RuleDefinitionDTO.builder()
                .id(1L)
                .code("EMA")
                .name("Exponential Moving Average")
                .requiresParam(false) // wrong – EMA requires a param
                .build();

        when(ruleDefinitionRepository.existsById(1L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateRuleDefinition(dto));
        assertTrue(ex.getMessage().contains("requiresParam"));
    }
}
