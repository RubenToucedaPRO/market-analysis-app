package com.market.analysis.unit.presentation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.market.analysis.application.dto.RuleDefinitionDTO;
import com.market.analysis.application.mapper.RuleDefinitionDTOMapper;
import com.market.analysis.domain.port.in.ManageRuleDefinitionUseCase;
import com.market.analysis.presentation.controller.RuleDefinitionController;

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

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private RuleDefinitionController ruleDefinitionController;

    private RuleDefinitionDTO testRuleDefinitionDTO;

    @BeforeEach
    void setUp() {

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
        List<RuleDefinitionDTO> ruleDefinitions = List.of(testRuleDefinitionDTO);
        when(manageRuleDefinitionUseCase.getAllRuleDefinitions()).thenReturn(ruleDefinitions);

        // Act
        String viewName = ruleDefinitionController.listRuleDefinitions(model);

        // Assert
        assertEquals("rule-definitions/list", viewName);
        verify(manageRuleDefinitionUseCase, times(1)).getAllRuleDefinitions();
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
        when(manageRuleDefinitionUseCase.getRuleDefinitionById(1L)).thenReturn(testRuleDefinitionDTO);

        // Act
        String viewName = ruleDefinitionController.showEditForm(1L, model);

        // Assert
        assertEquals("rule-definitions/create", viewName);
        verify(manageRuleDefinitionUseCase, times(1)).getRuleDefinitionById(1L);
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

        // Act
        String viewName = ruleDefinitionController.saveRuleDefinition(dtoWithoutId, redirectAttributes);

        // Assert
        assertEquals("redirect:/rule-definitions", viewName);
        verify(manageRuleDefinitionUseCase, times(1)).createRuleDefinition(any(RuleDefinitionDTO.class));
        verify(manageRuleDefinitionUseCase, times(0)).updateRuleDefinition(any());
        verify(redirectAttributes, times(1)).addFlashAttribute("message", "Definición de regla creada correctamente.");
        verify(redirectAttributes, times(1)).addFlashAttribute("messageType", "success");
    }

    @Test
    @DisplayName("Should update existing rule definition when id is not null")
    void testSaveRuleDefinitionUpdate() {
        // Arrange
        // testRuleDefinitionDTO has id = 1L

        // Act
        String viewName = ruleDefinitionController.saveRuleDefinition(testRuleDefinitionDTO, redirectAttributes);

        // Assert
        assertEquals("redirect:/rule-definitions", viewName);
        verify(manageRuleDefinitionUseCase, times(1)).updateRuleDefinition(any(RuleDefinitionDTO.class));
        verify(manageRuleDefinitionUseCase, times(0)).createRuleDefinition(any());
        verify(redirectAttributes, times(1)).addFlashAttribute("message", "Definición de regla actualizada correctamente.");
        verify(redirectAttributes, times(1)).addFlashAttribute("messageType", "success");
    }

    @Test
    @DisplayName("Should delete rule definition and redirect with success flash message")
    void testDeleteRuleDefinition() {
        // Act
        String viewName = ruleDefinitionController.deleteRuleDefinition(1L, redirectAttributes);

        // Assert
        assertEquals("redirect:/rule-definitions", viewName);
        verify(manageRuleDefinitionUseCase, times(1)).deleteRuleDefinition(1L);
        verify(redirectAttributes, times(1)).addFlashAttribute("message", "Definición de regla eliminada con éxito.");
        verify(redirectAttributes, times(1)).addFlashAttribute("messageType", "success");
    }

    @Test
    @DisplayName("Should propagate EntityInUseException to GlobalExceptionHandler when rule is in use")
    void testDeleteRuleDefinitionUsedInStrategy() {
        // Arrange
        String errorMsg = "No se puede eliminar la definición de regla 'SMA' porque está siendo usada en una o más estrategias.";
        doThrow(new com.market.analysis.domain.exception.EntityInUseException(errorMsg))
                .when(manageRuleDefinitionUseCase).deleteRuleDefinition(1L);

        // Act & Assert – exception propagates; GlobalExceptionHandler handles the redirect
        org.junit.jupiter.api.Assertions.assertThrows(
                com.market.analysis.domain.exception.EntityInUseException.class,
                () -> ruleDefinitionController.deleteRuleDefinition(1L, redirectAttributes));
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
        RuleDefinitionDTO dto2 = RuleDefinitionDTO.builder()
                .id(2L)
                .code("RSI")
                .name("Relative Strength Index")
                .requiresParam(true)
                .description("RSI indicator")
                .build();

        List<RuleDefinitionDTO> ruleDefinitions = List.of(testRuleDefinitionDTO, dto2);
        when(manageRuleDefinitionUseCase.getAllRuleDefinitions()).thenReturn(ruleDefinitions);

        // Act
        String viewName = ruleDefinitionController.listRuleDefinitions(model);

        // Assert
        assertEquals("rule-definitions/list", viewName);
        verify(manageRuleDefinitionUseCase, times(1)).getAllRuleDefinitions();
    }
}
