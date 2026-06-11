package com.market.analysis.application.usecase;

import java.util.List;
import java.util.Locale;

import com.market.analysis.application.dto.ProhibitedKeywordDTO;
import com.market.analysis.application.mapper.ProhibitedKeywordDTOMapper;
import com.market.analysis.domain.exception.DomainErrorCodes;
import com.market.analysis.domain.exception.DomainValidationException;
import com.market.analysis.domain.model.PageResult;
import com.market.analysis.domain.model.ProhibitedKeyword;
import com.market.analysis.domain.port.in.ManageProhibitedKeywordUseCase;
import com.market.analysis.domain.port.out.ProhibitedKeywordRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ManageProhibitedKeywordService implements ManageProhibitedKeywordUseCase {

    private static final int MAX_KEYWORD_LENGTH = 100;

    private final ProhibitedKeywordRepository prohibitedKeywordRepository;
    private final ProhibitedKeywordDTOMapper prohibitedKeywordMapper;

    @Override
    public PageResult<ProhibitedKeywordDTO> getProhibitedKeywords(int pageNumber, int pageSize) {
        PageResult<ProhibitedKeyword> page = prohibitedKeywordRepository.findAll(pageNumber, pageSize);
        List<ProhibitedKeywordDTO> dtos = page.content().stream()
                .map(prohibitedKeywordMapper::toDTO)
                .toList();
        return new PageResult<>(dtos, page.pageNumber(), page.pageSize(),
                page.totalElements(), page.totalPages());
    }

    @Override
    public boolean isKeywordProhibited(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        return prohibitedKeywordRepository.existsByKeyword(normalizedKeyword);
    }

    @Override
    public void addProhibitedKeyword(ProhibitedKeywordDTO prohibitedKeyword) {
        if (prohibitedKeyword == null) {
            throw new DomainValidationException(DomainErrorCodes.KEYWORD_NULL);
        }

        String normalizedKeyword = normalizeKeyword(prohibitedKeyword.getKeyword());
        if (prohibitedKeywordRepository.existsByKeyword(normalizedKeyword)) {
            throw new DomainValidationException(DomainErrorCodes.KEYWORD_EXISTS, normalizedKeyword);
        }

        log.info("Adding prohibited keyword: {}", normalizedKeyword);
        ProhibitedKeywordDTO normalizedDto = ProhibitedKeywordDTO.builder()
                .keyword(normalizedKeyword)
                .active(true)
                .createdAt(prohibitedKeyword.getCreatedAt())
                .updatedAt(prohibitedKeyword.getUpdatedAt())
                .build();

        ProhibitedKeyword domain = prohibitedKeywordMapper.toDomain(normalizedDto);
        prohibitedKeywordRepository.save(domain);
        log.info("Prohibited keyword added successfully: {}", normalizedKeyword);
    }

    @Override
    @Transactional
    public void removeProhibitedKeyword(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        log.info("Removing prohibited keyword: {}", normalizedKeyword);
        prohibitedKeywordRepository.deleteByKeyword(normalizedKeyword);
        log.info("Prohibited keyword removed successfully: {}", normalizedKeyword);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new DomainValidationException(DomainErrorCodes.KEYWORD_BLANK);
        }

        String normalizedKeyword = keyword.trim().toUpperCase(Locale.ROOT);
        if (normalizedKeyword.length() > MAX_KEYWORD_LENGTH) {
            throw new DomainValidationException(DomainErrorCodes.KEYWORD_TOO_LONG, MAX_KEYWORD_LENGTH);
        }

        return normalizedKeyword;
    }
}
