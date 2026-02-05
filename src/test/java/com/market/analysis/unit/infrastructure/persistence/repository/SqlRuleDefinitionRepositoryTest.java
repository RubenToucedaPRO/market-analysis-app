package com.market.analysis.unit.infrastructure.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

import com.market.analysis.domain.model.RuleDefinition;
import com.market.analysis.infrastructure.persistence.entity.RuleDefinitionEntity;
import com.market.analysis.infrastructure.persistence.mapper.RuleDefinitionMapper;
import com.market.analysis.infrastructure.persistence.repository.JpaRuleDefinitionRepository;
import com.market.analysis.infrastructure.persistence.repository.SqlRuleDefinitionRepository;

/**
 * Unit tests for SqlRuleDefinitionRepository.
 */
@DisplayName("SqlRuleDefinitionRepository Unit Tests")
@ExtendWith(MockitoExtension.class)
class SqlRuleDefinitionRepositoryTest {

    @Mock
    private JpaRuleDefinitionRepository jpaRepository;

    @Mock
    private RuleDefinitionMapper mapper;

    @InjectMocks
    private SqlRuleDefinitionRepository sqlRepository;

    private RuleDefinition testRuleDefinition;
    private RuleDefinitionEntity testEntity;

    @BeforeEach
    void setUp() {
        testRuleDefinition = RuleDefinition.builder()
                .id(1L)
                .code("SMA")
                .name("Simple Moving Average")
                .requiresParam(true)
                .description("Moving average over a period")
                .build();

        testEntity = new RuleDefinitionEntity();
        testEntity.setId(1L);
        testEntity.setCode("SMA");
        testEntity.setName("Simple Moving Average");
        testEntity.setRequiresParam(true);
        testEntity.setDescription("Moving average over a period");
    }

    @Test
    @DisplayName("Should save rule definition")
    void testSave() {
        // Arrange
        when(mapper.toEntity(testRuleDefinition)).thenReturn(testEntity);
        when(jpaRepository.save(testEntity)).thenReturn(testEntity);
        when(mapper.toDomain(testEntity)).thenReturn(testRuleDefinition);

        // Act
        RuleDefinition result = sqlRepository.save(testRuleDefinition);

        // Assert
        assertNotNull(result);
        assertEquals(testRuleDefinition.getId(), result.getId());
        verify(mapper, times(1)).toEntity(testRuleDefinition);
        verify(jpaRepository, times(1)).save(testEntity);
        verify(mapper, times(1)).toDomain(testEntity);
    }

    @Test
    @DisplayName("Should find rule definition by id")
    void testFindById() {
        // Arrange
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(mapper.toDomain(testEntity)).thenReturn(testRuleDefinition);

        // Act
        Optional<RuleDefinition> result = sqlRepository.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testRuleDefinition.getId(), result.get().getId());
        assertEquals("SMA", result.get().getCode());
        verify(jpaRepository, times(1)).findById(1L);
        verify(mapper, times(1)).toDomain(testEntity);
    }

    @Test
    @DisplayName("Should return empty when rule definition not found by id")
    void testFindByIdNotFound() {
        // Arrange
        when(jpaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<RuleDefinition> result = sqlRepository.findById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(jpaRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should find rule definition by code")
    void testFindByCode() {
        // Arrange
        when(jpaRepository.findByCode("SMA")).thenReturn(testEntity);
        when(mapper.toDomain(testEntity)).thenReturn(testRuleDefinition);

        // Act
        Optional<RuleDefinition> result = sqlRepository.findByCode("SMA");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("SMA", result.get().getCode());
        verify(jpaRepository, times(1)).findByCode("SMA");
        verify(mapper, times(1)).toDomain(testEntity);
    }

    @Test
    @DisplayName("Should return empty when rule definition not found by code")
    void testFindByCodeNotFound() {
        // Arrange
        when(jpaRepository.findByCode("UNKNOWN")).thenReturn(null);
        when(mapper.toDomain(null)).thenReturn(null);

        // Act
        Optional<RuleDefinition> result = sqlRepository.findByCode("UNKNOWN");

        // Assert
        assertFalse(result.isPresent());
        verify(jpaRepository, times(1)).findByCode("UNKNOWN");
    }

    @Test
    @DisplayName("Should find all rule definitions")
    void testFindAll() {
        // Arrange
        List<RuleDefinitionEntity> entities = List.of(testEntity);
        when(jpaRepository.findAll()).thenReturn(entities);
        when(mapper.toDomain(testEntity)).thenReturn(testRuleDefinition);

        // Act
        List<RuleDefinition> result = sqlRepository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SMA", result.get(0).getCode());
        verify(jpaRepository, times(1)).findAll();
        verify(mapper, times(1)).toDomain(testEntity);
    }

    @Test
    @DisplayName("Should delete rule definition by id")
    void testDeleteById() {
        // Act
        sqlRepository.deleteById(1L);

        // Assert
        verify(jpaRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should check if rule definition exists by id")
    void testExistsById() {
        // Arrange
        when(jpaRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean result = sqlRepository.existsById(1L);

        // Assert
        assertTrue(result);
        verify(jpaRepository, times(1)).existsById(1L);
    }

    @Test
    @DisplayName("Should return false when rule definition does not exist by id")
    void testExistsByIdNotFound() {
        // Arrange
        when(jpaRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean result = sqlRepository.existsById(999L);

        // Assert
        assertFalse(result);
        verify(jpaRepository, times(1)).existsById(999L);
    }

    @Test
    @DisplayName("Should check if rule definition exists by code")
    void testExistsByCode() {
        // Arrange
        when(jpaRepository.existsByCode("SMA")).thenReturn(true);

        // Act
        boolean result = sqlRepository.existsByCode("SMA");

        // Assert
        assertTrue(result);
        verify(jpaRepository, times(1)).existsByCode("SMA");
    }

    @Test
    @DisplayName("Should return false when rule definition does not exist by code")
    void testExistsByCodeNotFound() {
        // Arrange
        when(jpaRepository.existsByCode("UNKNOWN")).thenReturn(false);

        // Act
        boolean result = sqlRepository.existsByCode("UNKNOWN");

        // Assert
        assertFalse(result);
        verify(jpaRepository, times(1)).existsByCode("UNKNOWN");
    }
}
