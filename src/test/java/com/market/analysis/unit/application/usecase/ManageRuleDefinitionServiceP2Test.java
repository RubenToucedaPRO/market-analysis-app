package com.market.analysis.unit.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.application.dto.RuleCapabilityDTO;
import com.market.analysis.application.dto.RuleDefinitionDTO;
import com.market.analysis.application.mapper.RuleDefinitionDTOMapper;
import com.market.analysis.application.usecase.ManageRuleDefinitionService;
import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.domain.port.out.RuleDefinitionRepository;

/**
 * P2 tests for ManageRuleDefinitionService.
 *
 * <p>Verifies that the service exposes the catalog capabilities correctly and
 * that rule definition DTOs returned by {@code getAllRuleDefinitions()} are
 * enriched with the catalog's {@code allowedParams} and
 * {@code anyParamAllowed} data.</p>
 */
@DisplayName("ManageRuleDefinitionService P2 – Catalog Capabilities Tests")
@ExtendWith(MockitoExtension.class)
class ManageRuleDefinitionServiceP2Test {

    @Mock
    private RuleDefinitionRepository ruleDefinitionRepository;

    @Mock
    private RuleDefinitionDTOMapper ruleDefinitionMapper;

    @InjectMocks
    private ManageRuleDefinitionService service;

    // -------------------------------------------------------------------------
    // getCatalogCapabilities
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getCatalogCapabilities returns an entry for every supported code")
    void testGetCatalogCapabilitiesReturnsAllCodes() {
        List<RuleCapabilityDTO> capabilities = service.getCatalogCapabilities();

        assertThat(capabilities).isNotEmpty();
        // Must include well-known codes
        assertThat(capabilities).extracting(RuleCapabilityDTO::getCode)
                .contains("SMA", "EMA", "RSI", "PRICE", "CONSTANT", "MACD_LINE");
    }

    @Test
    @DisplayName("getCatalogCapabilities is sorted alphabetically")
    void testGetCatalogCapabilitiesIsSorted() {
        List<RuleCapabilityDTO> capabilities = service.getCatalogCapabilities();

        List<String> codes = capabilities.stream().map(RuleCapabilityDTO::getCode).toList();
        List<String> sorted = codes.stream().sorted().toList();

        assertThat(codes).isEqualTo(sorted);
    }

    @Test
    @DisplayName("SMA capability has requiresParam=true and allowedParams {20,50,200}")
    void testSmaCapabilityConstraints() {
        RuleCapabilityDTO sma = service.getCatalogCapabilities().stream()
                .filter(c -> "SMA".equals(c.getCode()))
                .findFirst()
                .orElseThrow();

        assertThat(sma.isRequiresParam()).isTrue();
        assertThat(sma.isAnyParamAllowed()).isFalse();
        assertThat(sma.getAllowedParams()).containsExactlyInAnyOrder(20.0, 50.0, 200.0);
    }

    @Test
    @DisplayName("PRICE capability has requiresParam=false and empty allowedParams")
    void testPriceCapabilityConstraints() {
        RuleCapabilityDTO price = service.getCatalogCapabilities().stream()
                .filter(c -> "PRICE".equals(c.getCode()))
                .findFirst()
                .orElseThrow();

        assertThat(price.isRequiresParam()).isFalse();
        assertThat(price.isAnyParamAllowed()).isFalse();
        assertThat(price.getAllowedParams()).isEmpty();
    }

    @Test
    @DisplayName("CONSTANT capability has requiresParam=true and anyParamAllowed=true")
    void testConstantCapabilityConstraints() {
        RuleCapabilityDTO constant = service.getCatalogCapabilities().stream()
                .filter(c -> "CONSTANT".equals(c.getCode()))
                .findFirst()
                .orElseThrow();

        assertThat(constant.isRequiresParam()).isTrue();
        assertThat(constant.isAnyParamAllowed()).isTrue();
        assertThat(constant.getAllowedParams()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // getAllRuleDefinitions – enrichment
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getAllRuleDefinitions enriches SMA definition with allowedParams from catalog")
    void testGetAllRuleDefinitionsEnrichesWithAllowedParams() {
        RuleDefinition domainSma = RuleDefinition.builder()
                .id(1L).code("SMA").name("Simple Moving Average").requiresParam(true).build();
        RuleDefinitionDTO mappedSma = RuleDefinitionDTO.builder()
                .id(1L).code("SMA").name("Simple Moving Average").requiresParam(true).build();

        when(ruleDefinitionRepository.findAll()).thenReturn(List.of(domainSma));
        when(ruleDefinitionMapper.toDTO(domainSma)).thenReturn(mappedSma);

        List<RuleDefinitionDTO> result = service.getAllRuleDefinitions();

        assertThat(result).hasSize(1);
        RuleDefinitionDTO enriched = result.get(0);
        assertThat(enriched.getAllowedParams()).containsExactlyInAnyOrder(20.0, 50.0, 200.0);
        assertThat(enriched.isAnyParamAllowed()).isFalse();
    }

    @Test
    @DisplayName("getAllRuleDefinitions enriches PRICE definition with empty allowedParams")
    void testGetAllRuleDefinitionsEnrichesNoParmIndicator() {
        RuleDefinition domainPrice = RuleDefinition.builder()
                .id(2L).code("PRICE").name("Current Price").requiresParam(false).build();
        RuleDefinitionDTO mappedPrice = RuleDefinitionDTO.builder()
                .id(2L).code("PRICE").name("Current Price").requiresParam(false).build();

        when(ruleDefinitionRepository.findAll()).thenReturn(List.of(domainPrice));
        when(ruleDefinitionMapper.toDTO(domainPrice)).thenReturn(mappedPrice);

        List<RuleDefinitionDTO> result = service.getAllRuleDefinitions();

        assertThat(result).hasSize(1);
        RuleDefinitionDTO enriched = result.get(0);
        assertThat(enriched.getAllowedParams()).isEmpty();
        assertThat(enriched.isAnyParamAllowed()).isFalse();
    }

    @Test
    @DisplayName("getAllRuleDefinitions enriches CONSTANT definition with anyParamAllowed=true")
    void testGetAllRuleDefinitionsEnrichesAnyParamIndicator() {
        RuleDefinition domainConst = RuleDefinition.builder()
                .id(3L).code("CONSTANT").name("Fixed Value").requiresParam(true).build();
        RuleDefinitionDTO mappedConst = RuleDefinitionDTO.builder()
                .id(3L).code("CONSTANT").name("Fixed Value").requiresParam(true).build();

        when(ruleDefinitionRepository.findAll()).thenReturn(List.of(domainConst));
        when(ruleDefinitionMapper.toDTO(domainConst)).thenReturn(mappedConst);

        List<RuleDefinitionDTO> result = service.getAllRuleDefinitions();

        assertThat(result).hasSize(1);
        RuleDefinitionDTO enriched = result.get(0);
        assertThat(enriched.isAnyParamAllowed()).isTrue();
        assertThat(enriched.getAllowedParams()).isEmpty();
    }

    @Test
    @DisplayName("getAllRuleDefinitions returns unchanged DTO for unknown code")
    void testGetAllRuleDefinitionsUnknownCodeUnchanged() {
        RuleDefinition domainUnknown = RuleDefinition.builder()
                .id(99L).code("UNKNOWN_XYZ").name("Unknown").requiresParam(false).build();
        RuleDefinitionDTO mappedUnknown = RuleDefinitionDTO.builder()
                .id(99L).code("UNKNOWN_XYZ").name("Unknown").requiresParam(false)
                .allowedParams(Set.of()).build();

        when(ruleDefinitionRepository.findAll()).thenReturn(List.of(domainUnknown));
        when(ruleDefinitionMapper.toDTO(domainUnknown)).thenReturn(mappedUnknown);

        List<RuleDefinitionDTO> result = service.getAllRuleDefinitions();

        assertThat(result).hasSize(1);
        // Should be returned as-is since the code is not in the catalog
        assertThat(result.get(0).getCode()).isEqualTo("UNKNOWN_XYZ");
    }
}
