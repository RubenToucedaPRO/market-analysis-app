package com.market.analysis.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.market.analysis.domain.model.PageResult;
import com.market.analysis.domain.model.ProhibitedKeyword;
import com.market.analysis.domain.port.out.ProhibitedKeywordRepository;
import com.market.analysis.infrastructure.persistence.entity.ProhibitedKeywordEntity;
import com.market.analysis.infrastructure.persistence.mapper.ProhibitedKeywordMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqlProhibitedKeywordRepository implements ProhibitedKeywordRepository {

    private final JpaProhibitedKeywordRepository jpaProhibitedKeywordRepository;
    private final ProhibitedKeywordMapper prohibitedKeywordMapper;

    @Override
    public List<ProhibitedKeyword> findAll() {
        log.debug("Retrieving all prohibited keywords");
        return jpaProhibitedKeywordRepository.findAll().stream()
                .map(prohibitedKeywordMapper::toDomain)
                .toList();
    }

    @Override
    public PageResult<ProhibitedKeyword> findAll(int pageNumber, int pageSize) {
        log.debug("Retrieving prohibited keywords page {} size {}", pageNumber, pageSize);
        Page<ProhibitedKeywordEntity> page = jpaProhibitedKeywordRepository
                .findAll(PageRequest.of(pageNumber, pageSize));
        List<ProhibitedKeyword> content = page.getContent().stream()
                .map(prohibitedKeywordMapper::toDomain)
                .toList();
        return new PageResult<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public boolean existsByKeyword(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        log.debug("Checking if keyword exists: {}", normalizedKeyword);
        return jpaProhibitedKeywordRepository.existsByKeyword(normalizedKeyword);
    }

    @Override
    public void save(ProhibitedKeyword prohibitedKeyword) {
        String normalizedKeyword = normalizeKeyword(prohibitedKeyword.getKeyword());
        log.debug("Saving prohibited keyword: {}", normalizedKeyword);

        if (!jpaProhibitedKeywordRepository.existsByKeyword(normalizedKeyword)) {
            ProhibitedKeyword normalized = ProhibitedKeyword.builder()
                    .keyword(normalizedKeyword)
                    .active(prohibitedKeyword.isActive())
                    .createdAt(resolveCreatedAt(prohibitedKeyword))
                    .updatedAt(resolveUpdatedAt(prohibitedKeyword))
                    .build();

            ProhibitedKeywordEntity entity = prohibitedKeywordMapper.toEntity(normalized);
            jpaProhibitedKeywordRepository.save(entity);
            log.debug("Prohibited keyword saved successfully: {}", normalizedKeyword);
        } else {
            log.debug("Prohibited keyword already exists, skipping save: {}", normalizedKeyword);
        }
    }

    @Override
    public void deleteByKeyword(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        log.debug("Deleting prohibited keyword: {}", normalizedKeyword);
        jpaProhibitedKeywordRepository.deleteByKeyword(normalizedKeyword);
        log.debug("Prohibited keyword deleted successfully: {}", normalizedKeyword);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Keyword must not be null or blank");
        }
        return keyword.trim().toUpperCase(Locale.ROOT);
    }

    private Instant resolveCreatedAt(ProhibitedKeyword prohibitedKeyword) {
        return prohibitedKeyword.getCreatedAt() != null ? prohibitedKeyword.getCreatedAt() : Instant.now();
    }

    private Instant resolveUpdatedAt(ProhibitedKeyword prohibitedKeyword) {
        if (prohibitedKeyword.getUpdatedAt() != null) {
            return prohibitedKeyword.getUpdatedAt();
        }
        return prohibitedKeyword.getCreatedAt() != null ? prohibitedKeyword.getCreatedAt() : Instant.now();
    }
}
