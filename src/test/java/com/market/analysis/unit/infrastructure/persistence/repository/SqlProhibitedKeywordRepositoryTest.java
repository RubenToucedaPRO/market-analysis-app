package com.market.analysis.unit.infrastructure.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.market.analysis.domain.model.PageResult;
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
    @DisplayName("Should pass keyword directly to JPA when checking existence")
    void shouldPassKeywordDirectlyOnExists() {
        when(jpaRepository.existsByKeyword("ETF")).thenReturn(true);

        boolean exists = sqlRepository.existsByKeyword("ETF");

        assertTrue(exists);
        verify(jpaRepository, times(1)).existsByKeyword("ETF");
    }

    @Test
    @DisplayName("Should save keyword and skip duplicates")
    void shouldSaveAndSkipDuplicates() {
        ProhibitedKeyword prohibitedKeyword = ProhibitedKeyword.builder()
                .keyword("ETF")
                .active(true)
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
    @DisplayName("Should pass keyword directly to JPA when deleting")
    void shouldPassKeywordDirectlyOnDelete() {
        sqlRepository.deleteByKeyword("WARRANTS");

        verify(jpaRepository, times(1)).deleteByKeyword("WARRANTS");
    }

    @Test
    @DisplayName("Should save keyword as-is when createdAt is null")
    void shouldSaveKeywordWhenCreatedAtIsNull() {
        ProhibitedKeyword prohibitedKeyword = ProhibitedKeyword.builder()
                .keyword("ETF")
                .active(true)
                .createdAt(null)
                .updatedAt(null)
                .build();

        ProhibitedKeywordEntity entity = new ProhibitedKeywordEntity();
        entity.setKeyword("ETF");

        when(jpaRepository.existsByKeyword("ETF")).thenReturn(false);
        when(mapper.toEntity(any(ProhibitedKeyword.class))).thenReturn(entity);

        sqlRepository.save(prohibitedKeyword);

        ArgumentCaptor<ProhibitedKeyword> captor = ArgumentCaptor.forClass(ProhibitedKeyword.class);
        verify(mapper, times(1)).toEntity(captor.capture());
        assertEquals("ETF", captor.getValue().getKeyword());
    }

    @Test
    @DisplayName("Should return paginated prohibited keywords")
    void shouldReturnPaginatedProhibitedKeywords() {
        ProhibitedKeywordEntity entity = new ProhibitedKeywordEntity();
        entity.setKeyword("ETF");
        ProhibitedKeyword domain = ProhibitedKeyword.builder().keyword("ETF").active(true).build();

        Page<ProhibitedKeywordEntity> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
        when(jpaRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);
        when(mapper.toDomain(entity)).thenReturn(domain);

        PageResult<ProhibitedKeyword> result = sqlRepository.findAll(0, 10);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals("ETF", result.content().get(0).getKeyword());
        assertEquals(0, result.pageNumber());
        assertEquals(10, result.pageSize());
        assertEquals(1, result.totalElements());
        assertEquals(1, result.totalPages());
    }

    @Test
    @DisplayName("Should return empty paginated result")
    void shouldReturnEmptyPaginatedResult() {
        Page<ProhibitedKeywordEntity> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(jpaRepository.findAll(PageRequest.of(0, 10))).thenReturn(emptyPage);

        PageResult<ProhibitedKeyword> result = sqlRepository.findAll(0, 10);

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
    }
}
