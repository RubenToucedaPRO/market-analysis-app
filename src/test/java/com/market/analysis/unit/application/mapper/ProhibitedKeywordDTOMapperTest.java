package com.market.analysis.unit.application.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.application.dto.ProhibitedKeywordDTO;
import com.market.analysis.application.mapper.ProhibitedKeywordDTOMapper;
import com.market.analysis.domain.model.ProhibitedKeyword;

@DisplayName("ProhibitedKeywordDTOMapper Unit Tests")
class ProhibitedKeywordDTOMapperTest {

    private final ProhibitedKeywordDTOMapper mapper = new ProhibitedKeywordDTOMapper();

    @Test
    @DisplayName("Should map domain to DTO")
    void shouldMapDomainToDto() {
        ProhibitedKeyword domain = ProhibitedKeyword.builder()
                .keyword("ETF")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        ProhibitedKeywordDTO dto = mapper.toDTO(domain);

        assertNotNull(dto);
        assertEquals("ETF", dto.getKeyword());
        assertTrue(dto.isActive());
    }

    @Test
    @DisplayName("Should map DTO to domain")
    void shouldMapDtoToDomain() {
        ProhibitedKeywordDTO dto = ProhibitedKeywordDTO.builder()
                .keyword("SPAC")
                .active(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        ProhibitedKeyword domain = mapper.toDomain(dto);

        assertNotNull(domain);
        assertEquals("SPAC", domain.getKeyword());
        assertTrue(domain.isActive());
    }

    @Test
    @DisplayName("Should return null when domain is null")
    void shouldReturnNullWhenDomainIsNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    @DisplayName("Should return null when DTO is null")
    void shouldReturnNullWhenDtoIsNull() {
        assertNull(mapper.toDomain(null));
    }
}
