package com.market.analysis.unit.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.application.dto.RuleDTO;
import com.market.analysis.application.dto.RuleDefinitionDTO;
import com.market.analysis.application.dto.StrategyDTO;
import com.market.analysis.application.mapper.RuleDefinitionDTOMapper;
import com.market.analysis.application.mapper.StrategyDTOMapper;
import com.market.analysis.application.usecase.ManageStrategyService;
import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.domain.port.out.RuleDefinitionRepository;
import com.market.analysis.domain.port.out.StrategyRepository;

/**
 * Unit tests for ManageStrategyService.
 */
@DisplayName("ManageStrategyService Unit Tests")
@ExtendWith(MockitoExtension.class)
class ManageStrategyServiceTest {

    @Mock
    private StrategyRepository strategyRepository;

    @Mock
    private RuleDefinitionRepository ruleDefinitionRepository;

    @Mock
    private StrategyDTOMapper strategyDTOMapper;

    @Mock
    private RuleDefinitionDTOMapper ruleDefinitionDTOMapper;

    @Mock
    private com.market.analysis.domain.port.out.StockDataRepository stockDataRepository;

    @Mock
    private com.market.analysis.domain.service.EvaluateStrategyService evaluateStrategyService;

    @InjectMocks
    private ManageStrategyService manageStrategyService;

    private Strategy testStrategy;
    private Rule testRule;
    private StrategyDTO testStrategyDTO;
    private RuleDTO testRuleDTO;

    @BeforeEach
    void setUp() {
        testRule = Rule.builder()
                .id(1L)
                .name("Test Rule")
                .subjectCode("PRICE")
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .description("Price above 100")
                .build();

        testStrategy = Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Test Description")
                .rules(List.of(testRule))
                .build();

        testRuleDTO = RuleDTO.builder()
                .id(1L)
                .name("Test Rule")
                .subjectCode("PRICE")
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .description("Price above 100")
                .build();

        testStrategyDTO = StrategyDTO.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Test Description")
                .rules(List.of(testRuleDTO))
                .build();
    }

    @Test
    @DisplayName("Should create strategy successfully")
    void testCreateStrategy() {
        // Arrange
        when(stockDataRepository.findAllByStrategyId(anyLong())).thenReturn(List.of());
        when(strategyDTOMapper.toDomain(testStrategyDTO)).thenReturn(testStrategy);
        when(strategyRepository.save(any(Strategy.class))).thenReturn(testStrategy);
        when(strategyDTOMapper.toDTO(testStrategy)).thenReturn(testStrategyDTO);

        // Act
        StrategyDTO result = manageStrategyService.createStrategy(testStrategyDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testStrategyDTO.getId(), result.getId());
        assertEquals(testStrategyDTO.getName(), result.getName());
        verify(strategyRepository, times(1)).save(any(Strategy.class));
    }

    @Test
    @DisplayName("Should get all strategies")
    void testGetAllStrategies() {
        // Arrange
        List<Strategy> strategies = List.of(testStrategy);
        when(strategyRepository.findAll()).thenReturn(strategies);
        when(strategyDTOMapper.toDTO(testStrategy)).thenReturn(testStrategyDTO);

        // Act
        List<StrategyDTO> result = manageStrategyService.getAllStrategies();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testStrategy.getId(), result.get(0).getId());
        verify(strategyRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get strategy by id")
    void testGetStrategyById() {
        // Arrange
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(testStrategy));
        when(strategyDTOMapper.toDTO(testStrategy)).thenReturn(testStrategyDTO);

        // Act
        StrategyDTO result = manageStrategyService.getStrategyById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Strategy", result.getName());
        verify(strategyRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when strategy not found")
    void testGetStrategyByIdNotFound() {
        // Arrange
        when(strategyRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> manageStrategyService.getStrategyById(999L));

        assertEquals("Strategy not found with id: 999", exception.getMessage());
        verify(strategyRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should get available rule definitions")
    void testGetAvailableRuleDefinitions() {
        // Arrange
        RuleDefinition ruleDefinition = RuleDefinition.builder()
                .id(1L)
                .code("SMA")
                .name("Simple Moving Average")
                .requiresParam(true)
                .description("Moving average")
                .build();
        RuleDefinitionDTO ruleDefinitionDTO = RuleDefinitionDTO.builder()
                .id(1L)
                .code("SMA")
                .name("Simple Moving Average")
                .requiresParam(true)
                .description("Moving average")
                .build();
        when(ruleDefinitionRepository.findAll()).thenReturn(List.of(ruleDefinition));
        when(ruleDefinitionDTOMapper.toDTO(ruleDefinition)).thenReturn(ruleDefinitionDTO);

        // Act
        List<RuleDefinitionDTO> result = manageStrategyService.getAvailableRuleDefinitions();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SMA", result.get(0).getCode());
        verify(ruleDefinitionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should delete strategy")
    void testDeleteStrategy() {
        // Act
        manageStrategyService.deleteStrategy(1L);

        // Assert
        verify(strategyRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should validate strategy before creating")
    void testCreateStrategyValidation() {
        // Arrange
        StrategyDTO invalidStrategy = StrategyDTO.builder()
                .id(2L)
                .name("Invalid Strategy")
                .description("No rules")
                .rules(List.of()) // Empty rules list - invalid
                .build();

        Strategy invalidStrategyDomain = Strategy.builder()
                .id(2L)
                .name("Invalid Strategy")
                .description("No rules")
                .rules(List.of())
                .build();

        when(strategyDTOMapper.toDomain(invalidStrategy)).thenReturn(invalidStrategyDomain);

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> manageStrategyService.createStrategy(invalidStrategy));
    }
}
