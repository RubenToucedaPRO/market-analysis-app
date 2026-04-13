package com.market.analysis.unit.application.usecase;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.application.dto.StrategyDTO;
import com.market.analysis.application.mapper.RuleDefinitionDTOMapper;
import com.market.analysis.application.mapper.StrategyDTOMapper;
import com.market.analysis.application.usecase.ManageStrategyService;
import com.market.analysis.domain.model.ObjectiveType;
import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.model.StrategyObjective;
import com.market.analysis.domain.port.out.RuleDefinitionRepository;
import com.market.analysis.domain.port.out.StockDataRepository;
import com.market.analysis.domain.port.out.StrategyRepository;
import com.market.analysis.domain.service.EvaluateStrategyService;

/**
 * P0 regression tests for ManageStrategyService.
 * Verifies that strategies with rules that cannot be evaluated at runtime
 * are rejected before reaching the persistence layer.
 */
@DisplayName("ManageStrategyService P0 Rule Validation Tests")
@ExtendWith(MockitoExtension.class)
class ManageStrategyServiceP0Test {

    @Mock
    private StrategyRepository strategyRepository;
    @Mock
    private RuleDefinitionRepository ruleDefinitionRepository;
    @Mock
    private StockDataRepository stockDataRepository;
    @Mock
    private StrategyDTOMapper strategyMapper;
    @Mock
    private RuleDefinitionDTOMapper ruleDefinitionDTOMapper;
    @Mock
    private EvaluateStrategyService evaluateStrategyService;

    @InjectMocks
    private ManageStrategyService service;

    private static final StrategyObjective VALID_OBJECTIVE = StrategyObjective.builder()
            .targetType(ObjectiveType.PERCENTAGE)
            .stopLossType(ObjectiveType.PERCENTAGE)
            .targetValue(BigDecimal.valueOf(5.0))
            .stopLossValue(BigDecimal.valueOf(2.0))
            .capitalToRisk(BigDecimal.valueOf(1000.0))
            .description("Test objective")
            .build();

    // -------------------------------------------------------------------------
    // Unsupported indicator codes
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should reject strategy with unsupported subject code")
    void testRejectsUnsupportedSubjectCode() {
        Strategy strategy = buildStrategy(Rule.builder()
                .subjectCode("VWAP")   // not in catalog
                .operator(">")
                .targetCode("PRICE")
                .build());

        StrategyDTO dto = StrategyDTO.builder().name("S").description("D").build();
        when(strategyMapper.toDomain(dto)).thenReturn(strategy);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createStrategy(dto));
        assertTrue(ex.getMessage().contains("VWAP"));
    }

    @Test
    @DisplayName("Should reject strategy with unsupported target code")
    void testRejectsUnsupportedTargetCode() {
        Strategy strategy = buildStrategy(Rule.builder()
                .subjectCode("PRICE")
                .operator(">")
                .targetCode("STOCH")   // not in catalog
                .build());

        StrategyDTO dto = StrategyDTO.builder().name("S").description("D").build();
        when(strategyMapper.toDomain(dto)).thenReturn(strategy);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createStrategy(dto));
        assertTrue(ex.getMessage().contains("STOCH"));
    }

    // -------------------------------------------------------------------------
    // Invalid parameter values
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should reject strategy where SMA is used with unsupported period 100")
    void testRejectsSmaWithInvalidPeriod() {
        Strategy strategy = buildStrategy(Rule.builder()
                .subjectCode("PRICE")
                .operator(">")
                .targetCode("SMA")
                .targetParam(100.0)   // not allowed: only 20, 50, 200
                .build());

        StrategyDTO dto = StrategyDTO.builder().name("S").description("D").build();
        when(strategyMapper.toDomain(dto)).thenReturn(strategy);

        assertThrows(IllegalArgumentException.class, () -> service.createStrategy(dto));
    }

    @Test
    @DisplayName("Should reject strategy where EMA is used without required param")
    void testRejectsEmaMissingParam() {
        Strategy strategy = buildStrategy(Rule.builder()
                .subjectCode("EMA")
                .subjectParam(null)   // param required for EMA
                .operator(">")
                .targetCode("PRICE")
                .build());

        StrategyDTO dto = StrategyDTO.builder().name("S").description("D").build();
        when(strategyMapper.toDomain(dto)).thenReturn(strategy);

        assertThrows(IllegalArgumentException.class, () -> service.createStrategy(dto));
    }

    @Test
    @DisplayName("Should reject strategy where CONSTANT is missing its numeric value")
    void testRejectsConstantWithoutParam() {
        Strategy strategy = buildStrategy(Rule.builder()
                .subjectCode("PRICE")
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(null)   // CONSTANT requires a param
                .build());

        StrategyDTO dto = StrategyDTO.builder().name("S").description("D").build();
        when(strategyMapper.toDomain(dto)).thenReturn(strategy);

        assertThrows(IllegalArgumentException.class, () -> service.createStrategy(dto));
    }

    @Test
    @DisplayName("Should reject strategy where no-param indicator receives an unexpected param")
    void testRejectsPriceWithUnexpectedParam() {
        Strategy strategy = buildStrategy(Rule.builder()
                .subjectCode("PRICE")
                .subjectParam(10.0)  // PRICE has no param
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(50.0)
                .build());

        StrategyDTO dto = StrategyDTO.builder().name("S").description("D").build();
        when(strategyMapper.toDomain(dto)).thenReturn(strategy);

        assertThrows(IllegalArgumentException.class, () -> service.createStrategy(dto));
    }

    // -------------------------------------------------------------------------
    // Invalid operators
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should reject strategy with unsupported operator CROSS_ABOVE")
    void testRejectsUnsupportedOperator() {
        Strategy strategy = buildStrategy(Rule.builder()
                .subjectCode("PRICE")
                .operator("CROSS_ABOVE")
                .targetCode("SMA")
                .targetParam(50.0)
                .build());

        StrategyDTO dto = StrategyDTO.builder().name("S").description("D").build();
        when(strategyMapper.toDomain(dto)).thenReturn(strategy);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createStrategy(dto));
        assertTrue(ex.getMessage().contains("CROSS_ABOVE"));
    }

    // -------------------------------------------------------------------------
    // Valid rules
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Should accept strategy with PRICE > SMA200")
    void testAcceptsValidPriceAboveSma200() {
        Rule rule = Rule.builder()
                .subjectCode("PRICE")
                .operator(">")
                .targetCode("SMA")
                .targetParam(200.0)
                .build();

        Strategy strategy = buildStrategy(rule);
        StrategyDTO dto = StrategyDTO.builder().name("S").description("D").build();
        when(strategyMapper.toDomain(dto)).thenReturn(strategy);
        when(strategyRepository.save(strategy)).thenReturn(strategy);
        when(strategyMapper.toDTO(strategy)).thenReturn(dto);
        when(stockDataRepository.findAllByStrategyId(org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(List.of());

        // Should not throw
        service.createStrategy(dto);
    }

    @Test
    @DisplayName("Should accept strategy with RSI14 < CONSTANT 30")
    void testAcceptsValidRsiBelowThreshold() {
        Rule rule = Rule.builder()
                .subjectCode("RSI")
                .subjectParam(14.0)
                .operator("<")
                .targetCode("CONSTANT")
                .targetParam(30.0)
                .build();

        Strategy strategy = buildStrategy(rule);
        StrategyDTO dto = StrategyDTO.builder().name("S").description("D").build();
        when(strategyMapper.toDomain(dto)).thenReturn(strategy);
        when(strategyRepository.save(strategy)).thenReturn(strategy);
        when(strategyMapper.toDTO(strategy)).thenReturn(dto);
        when(stockDataRepository.findAllByStrategyId(org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(List.of());

        // Should not throw
        service.createStrategy(dto);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Strategy buildStrategy(Rule rule) {
        return Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Test Description")
                .rules(List.of(rule))
                .objective(VALID_OBJECTIVE)
                .build();
    }
}
