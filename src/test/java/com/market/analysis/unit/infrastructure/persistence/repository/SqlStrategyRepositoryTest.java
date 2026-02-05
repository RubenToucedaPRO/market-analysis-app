package com.market.analysis.unit.infrastructure.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.domain.model.Rule;
import com.market.analysis.domain.model.Strategy;
import com.market.analysis.infrastructure.persistence.entity.RuleEntity;
import com.market.analysis.infrastructure.persistence.entity.StrategyEntity;
import com.market.analysis.infrastructure.persistence.mapper.StrategyMapper;
import com.market.analysis.infrastructure.persistence.repository.JpaStrategyRepository;
import com.market.analysis.infrastructure.persistence.repository.SqlStrategyRepository;

/**
 * Unit tests for SqlStrategyRepository.
 */
@DisplayName("SqlStrategyRepository Unit Tests")
@ExtendWith(MockitoExtension.class)
class SqlStrategyRepositoryTest {

    @Mock
    private JpaStrategyRepository jpaRepository;

    @Mock
    private StrategyMapper mapper;

    @InjectMocks
    private SqlStrategyRepository sqlStrategyRepository;

    private Strategy testStrategy;
    private StrategyEntity testEntity;
    private Rule testRule;
    private RuleEntity testRuleEntity;

    @BeforeEach
    void setUp() {
        testRule = Rule.builder()
                .id(1L)
                .name("Test Rule")
                .subjectCode("PRICE")
                .operator(">")
                .targetCode("CONSTANT")
                .targetParam(100.0)
                .description("Test")
                .build();

        testStrategy = Strategy.builder()
                .id(1L)
                .name("Test Strategy")
                .description("Test Description")
                .rules(List.of(testRule))
                .build();

        testRuleEntity = new RuleEntity();
        testRuleEntity.setId(1L);
        testRuleEntity.setName("Test Rule");
        testRuleEntity.setSubjectCode("PRICE");
        testRuleEntity.setOperator(">");
        testRuleEntity.setTargetCode("CONSTANT");
        testRuleEntity.setTargetParam(100.0);

        testEntity = new StrategyEntity();
        testEntity.setId(1L);
        testEntity.setName("Test Strategy");
        testEntity.setDescription("Test Description");
        testEntity.setRules(new ArrayList<>(List.of(testRuleEntity)));
    }

    @Test
    @DisplayName("Should save strategy")
    void testSave() {
        // Arrange
        when(mapper.toEntity(any(Strategy.class))).thenReturn(testEntity);
        when(jpaRepository.save(any(StrategyEntity.class))).thenReturn(testEntity);
        when(mapper.toDomain(any(StrategyEntity.class))).thenReturn(testStrategy);

        // Act
        Strategy result = sqlStrategyRepository.save(testStrategy);

        // Assert
        assertNotNull(result);
        assertEquals(testStrategy.getId(), result.getId());
        verify(mapper, times(1)).toEntity(testStrategy);
        verify(jpaRepository, times(1)).save(testEntity);
        verify(mapper, times(1)).toDomain(testEntity);
    }

    @Test
    @DisplayName("Should find strategy by id")
    void testFindById() {
        // Arrange
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(mapper.toDomain(any(StrategyEntity.class))).thenReturn(testStrategy);

        // Act
        Optional<Strategy> result = sqlStrategyRepository.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Test Strategy", result.get().getName());
        verify(jpaRepository, times(1)).findById(1L);
        verify(mapper, times(1)).toDomain(testEntity);
    }

    @Test
    @DisplayName("Should return empty when strategy not found by id")
    void testFindByIdNotFound() {
        // Arrange
        when(jpaRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        Optional<Strategy> result = sqlStrategyRepository.findById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(jpaRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should find strategy by name")
    void testFindByName() {
        // Arrange
        when(jpaRepository.findAll()).thenReturn(List.of(testEntity));
        when(mapper.toDomain(any(StrategyEntity.class))).thenReturn(testStrategy);

        // Act
        Optional<Strategy> result = sqlStrategyRepository.findByName("Test Strategy");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Strategy", result.get().getName());
        verify(jpaRepository, times(1)).findAll();
        verify(mapper, times(1)).toDomain(testEntity);
    }

    @Test
    @DisplayName("Should return empty when strategy not found by name")
    void testFindByNameNotFound() {
        // Arrange
        when(jpaRepository.findAll()).thenReturn(List.of(testEntity));
        when(mapper.toDomain(any(StrategyEntity.class))).thenReturn(testStrategy);

        // Act
        Optional<Strategy> result = sqlStrategyRepository.findByName("Nonexistent Strategy");

        // Assert
        assertFalse(result.isPresent());
        verify(jpaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find all strategies")
    void testFindAll() {
        // Arrange
        StrategyEntity entity2 = new StrategyEntity();
        entity2.setId(2L);
        entity2.setName("Strategy 2");
        entity2.setDescription("Description 2");
        entity2.setRules(new ArrayList<>());

        Strategy strategy2 = Strategy.builder()
                .id(2L)
                .name("Strategy 2")
                .description("Description 2")
                .rules(List.of(testRule))
                .build();

        when(jpaRepository.findAll()).thenReturn(List.of(testEntity, entity2));
        when(mapper.toDomain(testEntity)).thenReturn(testStrategy);
        when(mapper.toDomain(entity2)).thenReturn(strategy2);

        // Act
        List<Strategy> result = sqlStrategyRepository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(jpaRepository, times(1)).findAll();
        verify(mapper, times(2)).toDomain(any(StrategyEntity.class));
    }

    @Test
    @DisplayName("Should delete strategy by id")
    void testDeleteById() {
        // Act
        sqlStrategyRepository.deleteById(1L);

        // Assert
        verify(jpaRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should check if strategy exists by id")
    void testExistsById() {
        // Arrange
        when(jpaRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean result = sqlStrategyRepository.existsById(1L);

        // Assert
        assertTrue(result);
        verify(jpaRepository, times(1)).existsById(1L);
    }

    @Test
    @DisplayName("Should return false when strategy does not exist")
    void testExistsByIdNotFound() {
        // Arrange
        when(jpaRepository.existsById(anyLong())).thenReturn(false);

        // Act
        boolean result = sqlStrategyRepository.existsById(999L);

        // Assert
        assertFalse(result);
        verify(jpaRepository, times(1)).existsById(999L);
    }
}
