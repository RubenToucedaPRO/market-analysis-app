package com.market.analysis.unit.infrastructure.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.market.analysis.domain.model.ProhibitedKeyword;
import com.market.analysis.infrastructure.persistence.entity.ProhibitedKeywordEntity;
import com.market.analysis.infrastructure.persistence.mapper.ProhibitedKeywordMapper;

@DisplayName("ProhibitedKeywordMapper Unit Tests")
class ProhibitedKeywordMapperTest {

    private final ProhibitedKeywordMapper mapper = new ProhibitedKeywordMapper();

    @Test
    @DisplayName("Should map domain to entity")
    void shouldMapDomainToEntity() {
        Instant now = Instant.now();
        ProhibitedKeyword domain = ProhibitedKeyword.builder()
                .keyword("ETF")
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        ProhibitedKeywordEntity entity = mapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals("ETF", entity.getKeyword());
        assertTrue(entity.isActive());
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getUpdatedAt());
    }

    @Test
    @DisplayName("Should map entity to domain")
    void shouldMapEntityToDomain() {
        Instant now = Instant.now();
        ProhibitedKeywordEntity entity = new ProhibitedKeywordEntity();
        entity.setKeyword("WARRANTS");
        entity.setActive(false);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        ProhibitedKeyword domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals("WARRANTS", domain.getKeyword());
        assertFalse(domain.isActive());
        assertEquals(now, domain.getCreatedAt());
        assertEquals(now, domain.getUpdatedAt());
    }
}
