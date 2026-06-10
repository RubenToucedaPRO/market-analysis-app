package com.market.analysis.unit.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import com.market.analysis.application.dto.RuleDefinitionDTO;
import com.market.analysis.application.mapper.RuleDefinitionDTOMapper;
import com.market.analysis.application.usecase.ManageRuleDefinitionService;
import com.market.analysis.domain.exception.DomainValidationException;
import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.domain.port.out.RuleDefinitionRepository;

/**
 * Unit tests for ManageRuleDefinitionService.
 */
@DisplayName("ManageRuleDefinitionService Unit Tests")
@ExtendWith(MockitoExtension.class)
class ManageRuleDefinitionServiceTest {

    @Mock
    private RuleDefinitionRepository ruleDefinitionRepository;

    @Mock
    private RuleDefinitionDTOMapper ruleDefinitionDTOMapper;

    @InjectMocks
    private ManageRuleDefinitionService manageRuleDefinitionService;

    private RuleDefinition testRuleDefinition;
    private RuleDefinitionDTO testRuleDefinitionDTO;

    @BeforeEach
    void setUp() {
        testRuleDefinition = RuleDefinition.builder()
                .id(1L)
                .code("SMA")
                .name("Simple Moving Average")
                .requiresParam(true)
                .description("Moving average over a period")
                .build();

        testRuleDefinitionDTO = RuleDefinitionDTO.builder()
                .id(1L)
                .code("SMA")
                .name("Simple Moving Average")
                .requiresParam(true)
                .description("Moving average over a period")
                .build();
    }

    @Test
    @DisplayName("Should create rule definition successfully")
    void testCreateRuleDefinition() {
        // Arrange
        when(ruleDefinitionRepository.existsByCode("SMA")).thenReturn(false);
        when(ruleDefinitionDTOMapper.toDomain(testRuleDefinitionDTO)).thenReturn(testRuleDefinition);
        when(ruleDefinitionRepository.save(any(RuleDefinition.class))).thenReturn(testRuleDefinition);
        when(ruleDefinitionDTOMapper.toDTO(testRuleDefinition)).thenReturn(testRuleDefinitionDTO);

        // Act
        RuleDefinitionDTO result = manageRuleDefinitionService.createRuleDefinition(testRuleDefinitionDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testRuleDefinitionDTO.getId(), result.getId());
        assertEquals(testRuleDefinitionDTO.getCode(), result.getCode());
        assertEquals(testRuleDefinitionDTO.getName(), result.getName());
        verify(ruleDefinitionRepository, times(1)).existsByCode("SMA");
        verify(ruleDefinitionRepository, times(1)).save(any(RuleDefinition.class));
    }

    @Test
    @DisplayName("Should throw exception when creating rule definition with null object")
    void testCreateRuleDefinitionWithNull() {
        // Act & Assert
        DomainValidationException exception = assertThrows(DomainValidationException.class,
                () -> manageRuleDefinitionService.createRuleDefinition(null));

        assertEquals("validation.rd_null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when creating rule definition with null code")
    void testCreateRuleDefinitionWithNullCode() {
        // Arrange
        RuleDefinitionDTO invalidRuleDefinition = RuleDefinitionDTO.builder()
                .id(1L)
                .code(null)
                .name("Test")
                .requiresParam(false)
                .build();

        // Act & Assert
        DomainValidationException exception = assertThrows(DomainValidationException.class,
                () -> manageRuleDefinitionService.createRuleDefinition(invalidRuleDefinition));

        assertEquals("validation.rd_code_null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when creating rule definition with duplicate code")
    void testCreateRuleDefinitionWithDuplicateCode() {
        // Arrange
        when(ruleDefinitionRepository.existsByCode("SMA")).thenReturn(true);

        // Act & Assert
        DomainValidationException exception = assertThrows(DomainValidationException.class,
                () -> manageRuleDefinitionService.createRuleDefinition(testRuleDefinitionDTO));

        assertEquals("validation.rd_exists", exception.getMessage());
        verify(ruleDefinitionRepository, times(1)).existsByCode("SMA");
    }

    @Test
    @DisplayName("Should get all rule definitions")
    void testGetAllRuleDefinitions() {
        // Arrange
        List<RuleDefinition> ruleDefinitions = List.of(testRuleDefinition);
        when(ruleDefinitionRepository.findAll()).thenReturn(ruleDefinitions);
        when(ruleDefinitionDTOMapper.toDTO(testRuleDefinition)).thenReturn(testRuleDefinitionDTO);

        // Act
        List<RuleDefinitionDTO> result = manageRuleDefinitionService.getAllRuleDefinitions();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRuleDefinition.getId(), result.get(0).getId());
        verify(ruleDefinitionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get rule definition by id")
    void testGetRuleDefinitionById() {
        // Arrange
        when(ruleDefinitionRepository.findById(1L)).thenReturn(Optional.of(testRuleDefinition));
        when(ruleDefinitionDTOMapper.toDTO(testRuleDefinition)).thenReturn(testRuleDefinitionDTO);

        // Act
        RuleDefinitionDTO result = manageRuleDefinitionService.getRuleDefinitionById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("SMA", result.getCode());
        assertEquals("Simple Moving Average", result.getName());
        verify(ruleDefinitionRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return null when rule definition not found")
    void testGetRuleDefinitionByIdNotFound() {
        // Arrange
        when(ruleDefinitionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        RuleDefinitionDTO result = manageRuleDefinitionService.getRuleDefinitionById(999L);

        // Assert
        assertNull(result);
        verify(ruleDefinitionRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should update rule definition successfully")
    void testUpdateRuleDefinition() {
        // Arrange
        when(ruleDefinitionRepository.existsById(1L)).thenReturn(true);
        when(ruleDefinitionDTOMapper.toDomain(testRuleDefinitionDTO)).thenReturn(testRuleDefinition);
        when(ruleDefinitionRepository.save(testRuleDefinition)).thenReturn(testRuleDefinition);
        when(ruleDefinitionDTOMapper.toDTO(testRuleDefinition)).thenReturn(testRuleDefinitionDTO);

        // Act
        RuleDefinitionDTO result = manageRuleDefinitionService.updateRuleDefinition(testRuleDefinitionDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testRuleDefinitionDTO.getId(), result.getId());
        verify(ruleDefinitionRepository, times(1)).existsById(1L);
        verify(ruleDefinitionRepository, times(1)).save(testRuleDefinition);
    }

    @Test
    @DisplayName("Should throw exception when updating rule definition with null object")
    void testUpdateRuleDefinitionWithNull() {
        // Act & Assert
        DomainValidationException exception = assertThrows(DomainValidationException.class,
                () -> manageRuleDefinitionService.updateRuleDefinition(null));

        assertEquals("validation.rd_null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when updating rule definition with null id")
    void testUpdateRuleDefinitionWithNullId() {
        // Arrange
        RuleDefinitionDTO invalidRuleDefinition = RuleDefinitionDTO.builder()
                .id(null)
                .code("SMA")
                .name("Test")
                .requiresParam(false)
                .build();

        // Act & Assert
        DomainValidationException exception = assertThrows(DomainValidationException.class,
                () -> manageRuleDefinitionService.updateRuleDefinition(invalidRuleDefinition));

        assertEquals("validation.rd_id_null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent rule definition")
    void testUpdateRuleDefinitionNotFound() {
        // Arrange
        when(ruleDefinitionRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        DomainValidationException exception = assertThrows(DomainValidationException.class,
                () -> manageRuleDefinitionService.updateRuleDefinition(testRuleDefinitionDTO));

        assertEquals("ruledefinition.not_found", exception.getMessage());
        verify(ruleDefinitionRepository, times(1)).existsById(1L);
    }

    @Test
    @DisplayName("Should delete rule definition successfully")
    void testDeleteRuleDefinition() {
        // Arrange
        when(ruleDefinitionRepository.existsById(1L)).thenReturn(true);

        // Act
        manageRuleDefinitionService.deleteRuleDefinition(1L);

        // Assert
        verify(ruleDefinitionRepository, times(1)).existsById(1L);
        verify(ruleDefinitionRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent rule definition")
    void testDeleteRuleDefinitionNotFound() {
        // Arrange
        when(ruleDefinitionRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        DomainValidationException exception = assertThrows(DomainValidationException.class,
                () -> manageRuleDefinitionService.deleteRuleDefinition(999L));

        assertEquals("ruledefinition.not_found", exception.getMessage());
        verify(ruleDefinitionRepository, times(1)).existsById(999L);
    }
}
