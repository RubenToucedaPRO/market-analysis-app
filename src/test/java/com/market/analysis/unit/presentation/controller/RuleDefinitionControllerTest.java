package com.market.analysis.unit.presentation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.domain.port.in.ManageRuleDefinitionUseCase;
import com.market.analysis.presentation.controller.RuleDefinitionController;
import com.market.analysis.presentation.dto.RuleDefinitionDTO;
import com.market.analysis.presentation.mapper.RuleDefinitionDTOMapper;

/**
 * Unit tests for RuleDefinitionController.
 */
@DisplayName("RuleDefinitionController Unit Tests")
@ExtendWith(MockitoExtension.class)
class RuleDefinitionControllerTest {

    @Mock
    private ManageRuleDefinitionUseCase manageRuleDefinitionUseCase;

    @Mock
    private RuleDefinitionDTOMapper mapper;

    @Mock
    private Model model;

    @InjectMocks
    private RuleDefinitionController ruleDefinitionController;

    private RuleDefinition testRuleDefinition;
    private RuleDefinitionDTO testRuleDefinitionDTO;

    @BeforeEach
    void setUp() {
        testRuleDefinition = RuleDefinition.builder()
                .id(1L)
                .code("SMA")
                .name("Simple Moving Average")
                .requiresParam(true)
                .description("Moving average indicator")
                .build();

        testRuleDefinitionDTO = RuleDefinitionDTO.builder()
                .id(1L)
                .code("SMA")
                .name("Simple Moving Average")
                .requiresParam(true)
                .description("Moving average indicator")
                .build();
    }

    @Test
    @DisplayName("Should list all rule definitions")
    void testListRuleDefinitions() {
        // Arrange
        List<RuleDefinition> ruleDefinitions = List.of(testRuleDefinition);
        when(manageRuleDefinitionUseCase.getAllRuleDefinitions()).thenReturn(ruleDefinitions);
        when(mapper.toDTO(testRuleDefinition)).thenReturn(testRuleDefinitionDTO);

        // Act
        String viewName = ruleDefinitionController.listRuleDefinitions(model);

        // Assert
        assertEquals("rule-definitions/list", viewName);
        verify(manageRuleDefinitionUseCase, times(1)).getAllRuleDefinitions();
        verify(mapper, times(1)).toDTO(testRuleDefinition);
        verify(model, times(1)).addAttribute(any(String.class), any());
    }

    @Test
    @DisplayName("Should show create form with empty rule definition")
    void testShowCreateForm() {
        // Act
        String viewName = ruleDefinitionController.showCreateForm(model);

        // Assert
        assertEquals("rule-definitions/create", viewName);
        verify(model, times(1)).addAttribute(any(String.class), any(RuleDefinitionDTO.class));
        verify(model, times(1)).addAttribute("isEdit", false);
    }

    @Test
    @DisplayName("Should show edit form with existing rule definition")
    void testShowEditForm() {
        // Arrange
        when(manageRuleDefinitionUseCase.getRuleDefinitionById(1L)).thenReturn(testRuleDefinition);
        when(mapper.toDTO(testRuleDefinition)).thenReturn(testRuleDefinitionDTO);

        // Act
        String viewName = ruleDefinitionController.showEditForm(1L, model);

        // Assert
        assertEquals("rule-definitions/create", viewName);
        verify(manageRuleDefinitionUseCase, times(1)).getRuleDefinitionById(1L);
        verify(mapper, times(1)).toDTO(testRuleDefinition);
        verify(model, times(1)).addAttribute("ruleDefinition", testRuleDefinitionDTO);
        verify(model, times(1)).addAttribute("isEdit", true);
    }

    @Test
    @DisplayName("Should create new rule definition when id is null")
    void testSaveRuleDefinitionCreate() {
        // Arrange
        RuleDefinitionDTO dtoWithoutId = RuleDefinitionDTO.builder()
                .code("RSI")
                .name("Relative Strength Index")
                .requiresParam(true)
                .description("RSI indicator")
                .build();

        RuleDefinition domainWithoutId = RuleDefinition.builder()
                .code("RSI")
                .name("Relative Strength Index")
                .requiresParam(true)
                .description("RSI indicator")
                .build();

        when(mapper.toDomain(dtoWithoutId)).thenReturn(domainWithoutId);

        // Act
        String viewName = ruleDefinitionController.saveRuleDefinition(dtoWithoutId);

        // Assert
        assertEquals("redirect:/rule-definitions", viewName);
        verify(manageRuleDefinitionUseCase, times(1)).createRuleDefinition(domainWithoutId);
        verify(manageRuleDefinitionUseCase, times(0)).updateRuleDefinition(any());
    }

    @Test
    @DisplayName("Should update existing rule definition when id is not null")
    void testSaveRuleDefinitionUpdate() {
        // Arrange
        when(mapper.toDomain(testRuleDefinitionDTO)).thenReturn(testRuleDefinition);

        // Act
        String viewName = ruleDefinitionController.saveRuleDefinition(testRuleDefinitionDTO);

        // Assert
        assertEquals("redirect:/rule-definitions", viewName);
        verify(manageRuleDefinitionUseCase, times(1)).updateRuleDefinition(testRuleDefinition);
        verify(manageRuleDefinitionUseCase, times(0)).createRuleDefinition(any());
    }

    @Test
    @DisplayName("Should delete rule definition and redirect")
    void testDeleteRuleDefinition() {
        // Act
        String viewName = ruleDefinitionController.deleteRuleDefinition(1L);

        // Assert
        assertEquals("redirect:/rule-definitions", viewName);
        verify(manageRuleDefinitionUseCase, times(1)).deleteRuleDefinition(1L);
    }

    @Test
    @DisplayName("Should handle list with empty rule definitions")
    void testListRuleDefinitionsEmpty() {
        // Arrange
        when(manageRuleDefinitionUseCase.getAllRuleDefinitions()).thenReturn(List.of());

        // Act
        String viewName = ruleDefinitionController.listRuleDefinitions(model);

        // Assert
        assertEquals("rule-definitions/list", viewName);
        verify(model, times(1)).addAttribute(any(String.class), any());
    }

    @Test
    @DisplayName("Should handle multiple rule definitions in list")
    void testListMultipleRuleDefinitions() {
        // Arrange
        RuleDefinition ruleDefinition2 = RuleDefinition.builder()
                .id(2L)
                .code("RSI")
                .name("Relative Strength Index")
                .requiresParam(true)
                .description("RSI indicator")
                .build();

        RuleDefinitionDTO dto2 = RuleDefinitionDTO.builder()
                .id(2L)
                .code("RSI")
                .name("Relative Strength Index")
                .requiresParam(true)
                .description("RSI indicator")
                .build();

        List<RuleDefinition> ruleDefinitions = List.of(testRuleDefinition, ruleDefinition2);
        when(manageRuleDefinitionUseCase.getAllRuleDefinitions()).thenReturn(ruleDefinitions);
        when(mapper.toDTO(testRuleDefinition)).thenReturn(testRuleDefinitionDTO);
        when(mapper.toDTO(ruleDefinition2)).thenReturn(dto2);

        // Act
        String viewName = ruleDefinitionController.listRuleDefinitions(model);

        // Assert
        assertEquals("rule-definitions/list", viewName);
        verify(manageRuleDefinitionUseCase, times(1)).getAllRuleDefinitions();
        verify(mapper, times(2)).toDTO(any(RuleDefinition.class));
    }
}
