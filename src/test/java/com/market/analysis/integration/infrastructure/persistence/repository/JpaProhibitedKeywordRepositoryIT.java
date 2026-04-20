package com.market.analysis.integration.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.market.analysis.infrastructure.persistence.entity.ProhibitedKeywordEntity;
import com.market.analysis.infrastructure.persistence.repository.JpaProhibitedKeywordRepository;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("JpaProhibitedKeywordRepository Integration Tests")
class JpaProhibitedKeywordRepositoryIT {

    @Autowired
    private JpaProhibitedKeywordRepository jpaProhibitedKeywordRepository;

    @BeforeEach
    void setUp() {
        jpaProhibitedKeywordRepository.deleteAll();
    }

    @Test
    @DisplayName("Should enforce unique constraint for keyword")
    void shouldEnforceUniqueKeyword() {
        jpaProhibitedKeywordRepository.saveAndFlush(newKeywordEntity("ETF"));

        assertThrows(DataIntegrityViolationException.class,
                this::saveDuplicateKeyword);
    }

    private void saveDuplicateKeyword() {
        jpaProhibitedKeywordRepository.saveAndFlush(newKeywordEntity("ETF"));
    }

    @Test
    @DisplayName("Should find existing keyword by exact value")
    void shouldFindExistingKeywordByExactValue() {
        jpaProhibitedKeywordRepository.saveAndFlush(newKeywordEntity("SPAC"));

        assertThat(jpaProhibitedKeywordRepository.existsByKeyword("SPAC")).isTrue();
        assertThat(jpaProhibitedKeywordRepository.existsByKeyword("spac")).isFalse();
    }

    @Test
    @DisplayName("Should delete keyword by value")
    void shouldDeleteKeywordByValue() {
        jpaProhibitedKeywordRepository.saveAndFlush(newKeywordEntity("WARRANTS"));
        jpaProhibitedKeywordRepository.saveAndFlush(newKeywordEntity("ETF"));

        jpaProhibitedKeywordRepository.deleteByKeyword("WARRANTS");

        assertThat(jpaProhibitedKeywordRepository.existsByKeyword("WARRANTS")).isFalse();
        assertThat(jpaProhibitedKeywordRepository.existsByKeyword("ETF")).isTrue();
    }

    private ProhibitedKeywordEntity newKeywordEntity(String keyword) {
        ProhibitedKeywordEntity entity = new ProhibitedKeywordEntity();
        entity.setKeyword(keyword);
        entity.setActive(true);
        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }
}
