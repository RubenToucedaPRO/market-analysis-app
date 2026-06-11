package com.market.analysis.unit.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

import com.market.analysis.application.dto.ProhibitedKeywordDTO;
import com.market.analysis.application.mapper.ProhibitedKeywordDTOMapper;
import com.market.analysis.application.usecase.ManageProhibitedKeywordService;
import com.market.analysis.domain.exception.DomainValidationException;
import com.market.analysis.domain.model.PageResult;
import com.market.analysis.domain.model.ProhibitedKeyword;
import com.market.analysis.domain.port.out.ProhibitedKeywordRepository;

@DisplayName("ManageProhibitedKeywordService Unit Tests")
@ExtendWith(MockitoExtension.class)
class ManageProhibitedKeywordServiceTest {

    @Mock
    private ProhibitedKeywordRepository prohibitedKeywordRepository;

    @Mock
    private ProhibitedKeywordDTOMapper prohibitedKeywordMapper;

    @InjectMocks
    private ManageProhibitedKeywordService manageProhibitedKeywordService;

    @Test
    @DisplayName("Should normalize keyword when checking if prohibited")
    void shouldNormalizeKeywordWhenCheckingIfProhibited() {
        when(prohibitedKeywordRepository.existsByKeyword("ETF")).thenReturn(true);

        boolean result = manageProhibitedKeywordService.isKeywordProhibited(" etf ");

        assertTrue(result);
        verify(prohibitedKeywordRepository, times(1)).existsByKeyword("ETF");
    }

    @Test
    @DisplayName("Should add prohibited keyword with normalized value")
    void shouldAddProhibitedKeywordWithNormalizedValue() {
        ProhibitedKeywordDTO inputDto = ProhibitedKeywordDTO.builder()
                .keyword(" etf ")
                .active(false)
                .createdAt(Instant.now())
                .build();
        ProhibitedKeyword domain = ProhibitedKeyword.builder().keyword("ETF").active(true).build();
        when(prohibitedKeywordRepository.existsByKeyword("ETF")).thenReturn(false);
        when(prohibitedKeywordMapper.toDomain(any(ProhibitedKeywordDTO.class))).thenReturn(domain);

        manageProhibitedKeywordService.addProhibitedKeyword(inputDto);

        ArgumentCaptor<ProhibitedKeywordDTO> captor = ArgumentCaptor.forClass(ProhibitedKeywordDTO.class);
        verify(prohibitedKeywordMapper, times(1)).toDomain(captor.capture());
        assertEquals("ETF", captor.getValue().getKeyword());
        assertTrue(captor.getValue().isActive());
        verify(prohibitedKeywordRepository, times(1)).save(domain);
    }

    @Test
    @DisplayName("Should reject duplicated prohibited keyword")
    void shouldRejectDuplicatedProhibitedKeyword() {
        ProhibitedKeywordDTO inputDto = ProhibitedKeywordDTO.builder().keyword("etf").build();
        when(prohibitedKeywordRepository.existsByKeyword("ETF")).thenReturn(true);

        DomainValidationException exception = assertThrows(DomainValidationException.class,
                () -> manageProhibitedKeywordService.addProhibitedKeyword(inputDto));

        assertEquals("validation.keyword_exists", exception.getMessage());
        verify(prohibitedKeywordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject blank keyword")
    void shouldRejectBlankKeyword() {
        DomainValidationException exception = assertThrows(DomainValidationException.class,
                () -> manageProhibitedKeywordService.isKeywordProhibited("   "));

        assertEquals("validation.keyword_blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should reject keyword longer than max length")
    void shouldRejectKeywordLongerThanMaxLength() {
        String longKeyword = "A".repeat(101);

        DomainValidationException exception = assertThrows(DomainValidationException.class,
                () -> manageProhibitedKeywordService.removeProhibitedKeyword(longKeyword));

        assertEquals("validation.keyword_too_long", exception.getMessage());
    }

    @Test
    @DisplayName("Should remove prohibited keyword using normalized value")
    void shouldRemoveProhibitedKeywordUsingNormalizedValue() {
        manageProhibitedKeywordService.removeProhibitedKeyword(" spac ");

        verify(prohibitedKeywordRepository, times(1)).deleteByKeyword("SPAC");
    }

    @Test
    @DisplayName("Should return paginated prohibited keywords")
    void shouldReturnPaginatedProhibitedKeywords() {
        ProhibitedKeyword domain = ProhibitedKeyword.builder().keyword("ETF").active(true).build();
        ProhibitedKeywordDTO dto = ProhibitedKeywordDTO.builder().keyword("ETF").active(true).build();
        PageResult<ProhibitedKeyword> page = new PageResult<>(List.of(domain), 0, 10, 1L, 1);
        when(prohibitedKeywordRepository.findAll(0, 10)).thenReturn(page);
        when(prohibitedKeywordMapper.toDTO(domain)).thenReturn(dto);

        PageResult<ProhibitedKeywordDTO> result = manageProhibitedKeywordService.getProhibitedKeywords(0, 10);

        assertEquals(1, result.content().size());
        assertEquals("ETF", result.content().get(0).getKeyword());
        assertEquals(0, result.pageNumber());
        assertEquals(10, result.pageSize());
        assertEquals(1L, result.totalElements());
        assertEquals(1, result.totalPages());
    }

    @Test
    @DisplayName("Should return empty page when no prohibited keywords exist")
    void shouldReturnEmptyPageWhenNoKeywordsExist() {
        PageResult<ProhibitedKeyword> page = new PageResult<>(List.of(), 0, 10, 0L, 0);
        when(prohibitedKeywordRepository.findAll(0, 10)).thenReturn(page);

        PageResult<ProhibitedKeywordDTO> result = manageProhibitedKeywordService.getProhibitedKeywords(0, 10);

        assertTrue(result.content().isEmpty());
    }

    @Test
    @DisplayName("Should reject null DTO when adding prohibited keyword")
    void shouldRejectNullDtoWhenAddingProhibitedKeyword() {
        DomainValidationException exception = assertThrows(DomainValidationException.class,
                () -> manageProhibitedKeywordService.addProhibitedKeyword(null));

        assertEquals("validation.keyword_null", exception.getMessage());
        verify(prohibitedKeywordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject null keyword when checking if prohibited")
    void shouldRejectNullKeywordWhenCheckingIfProhibited() {
        DomainValidationException exception = assertThrows(DomainValidationException.class,
                () -> manageProhibitedKeywordService.isKeywordProhibited(null));

        assertEquals("validation.keyword_blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should return false when keyword is not prohibited")
    void shouldReturnFalseWhenKeywordIsNotProhibited() {
        when(prohibitedKeywordRepository.existsByKeyword("ETF")).thenReturn(false);

        boolean result = manageProhibitedKeywordService.isKeywordProhibited("etf");

        assertFalse(result);
        verify(prohibitedKeywordRepository, times(1)).existsByKeyword("ETF");
    }

    @Test
    @DisplayName("Should reject null keyword when removing")
    void shouldRejectNullKeywordWhenRemoving() {
        DomainValidationException exception = assertThrows(DomainValidationException.class,
                () -> manageProhibitedKeywordService.removeProhibitedKeyword(null));

        assertEquals("validation.keyword_blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should add keyword preserving original createdAt and updatedAt")
    void shouldAddKeywordPreservingOriginalCreatedAtAndUpdatedAt() {
        Instant createdAt = Instant.parse("2025-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2025-06-01T00:00:00Z");
        ProhibitedKeywordDTO inputDto = ProhibitedKeywordDTO.builder()
                .keyword("etf")
                .active(false)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
        ProhibitedKeyword domain = ProhibitedKeyword.builder().keyword("ETF").active(true).build();
        when(prohibitedKeywordRepository.existsByKeyword("ETF")).thenReturn(false);
        when(prohibitedKeywordMapper.toDomain(any(ProhibitedKeywordDTO.class))).thenReturn(domain);

        manageProhibitedKeywordService.addProhibitedKeyword(inputDto);

        ArgumentCaptor<ProhibitedKeywordDTO> captor = ArgumentCaptor.forClass(ProhibitedKeywordDTO.class);
        verify(prohibitedKeywordMapper, times(1)).toDomain(captor.capture());
        assertEquals(createdAt, captor.getValue().getCreatedAt());
        assertEquals(updatedAt, captor.getValue().getUpdatedAt());
    }
}
