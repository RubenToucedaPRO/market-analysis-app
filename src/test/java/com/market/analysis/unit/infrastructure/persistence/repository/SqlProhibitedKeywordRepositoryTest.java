package com.market.analysis.unit.infrastructure.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.market.analysis.domain.model.ProhibitedKeyword;
import com.market.analysis.infrastructure.persistence.entity.ProhibitedKeywordEntity;
import com.market.analysis.infrastructure.persistence.mapper.ProhibitedKeywordMapper;
import com.market.analysis.infrastructure.persistence.repository.JpaProhibitedKeywordRepository;
import com.market.analysis.infrastructure.persistence.repository.SqlProhibitedKeywordRepository;

@DisplayName("SqlProhibitedKeywordRepository Unit Tests")
@ExtendWith(MockitoExtension.class)
class SqlProhibitedKeywordRepositoryTest {

    @Mock
    private JpaProhibitedKeywordRepository jpaRepository;

    @Mock
    private ProhibitedKeywordMapper mapper;

    @InjectMocks
    private SqlProhibitedKeywordRepository sqlRepository;

    @Test
    @DisplayName("Should find all prohibited keywords")
    void shouldFindAll() {
        ProhibitedKeywordEntity entity = new ProhibitedKeywordEntity();
        entity.setKeyword("ETF");
        ProhibitedKeyword domain = ProhibitedKeyword.builder().keyword("ETF").active(true).build();

        when(jpaRepository.findAll()).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        List<ProhibitedKeyword> result = sqlRepository.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ETF", result.get(0).getKeyword());
        verify(jpaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should normalize keyword when checking existence")
    void shouldNormalizeKeywordOnExists() {
        when(jpaRepository.existsByKeyword("ETF")).thenReturn(true);

        boolean exists = sqlRepository.existsByKeyword(" etf ");

        assertTrue(exists);
        verify(jpaRepository, times(1)).existsByKeyword("ETF");
    }

    @Test
    @DisplayName("Should save keyword normalized and skip duplicates")
    void shouldSaveNormalizedAndSkipDuplicates() {
        ProhibitedKeyword prohibitedKeyword = ProhibitedKeyword.builder()
                .keyword(" etf ")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        ProhibitedKeywordEntity entity = new ProhibitedKeywordEntity();
        entity.setKeyword("ETF");

        when(jpaRepository.existsByKeyword("ETF")).thenReturn(false, true);
        when(mapper.toEntity(any(ProhibitedKeyword.class))).thenReturn(entity);

        sqlRepository.save(prohibitedKeyword);
        sqlRepository.save(prohibitedKeyword);

        ArgumentCaptor<ProhibitedKeyword> captor = ArgumentCaptor.forClass(ProhibitedKeyword.class);
        verify(mapper, times(1)).toEntity(captor.capture());
        assertEquals("ETF", captor.getValue().getKeyword());
        verify(jpaRepository, times(1)).save(entity);
    }

    @Test
    @DisplayName("Should normalize keyword when deleting")
    void shouldNormalizeKeywordOnDelete() {
        sqlRepository.deleteByKeyword(" warrants ");

        verify(jpaRepository, times(1)).deleteByKeyword("WARRANTS");
    }

    @Test
    @DisplayName("Should reject blank keyword on save")
    void shouldRejectBlankKeywordOnSave() {
        ProhibitedKeyword prohibitedKeyword = ProhibitedKeyword.builder()
                .keyword("   ")
                .active(true)
                .build();

        assertThrows(IllegalArgumentException.class, () -> sqlRepository.save(prohibitedKeyword));
        verify(mapper, never()).toEntity(any(ProhibitedKeyword.class));
    }
}
