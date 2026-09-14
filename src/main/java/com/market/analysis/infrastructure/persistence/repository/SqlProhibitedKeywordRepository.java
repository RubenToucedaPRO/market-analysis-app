package com.market.analysis.infrastructure.persistence.repository;

import java.util.List;

  import org.springframework.data.domain.Page;
  import org.springframework.data.domain.PageRequest;
  import org.springframework.stereotype.Component;
  import org.springframework.transaction.annotation.Transactional;

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
     @Transactional(readOnly = true)
     public List<ProhibitedKeyword> findAll() {
        log.debug("Retrieving all prohibited keywords");
        return jpaProhibitedKeywordRepository.findAll().stream()
                .map(prohibitedKeywordMapper::toDomain)
                .toList();
    }

     @Override
     @Transactional(readOnly = true)
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
     @Transactional(readOnly = true)
     public boolean existsByKeyword(String keyword) {
        log.debug("Checking if keyword exists: {}", keyword);
        return jpaProhibitedKeywordRepository.existsByKeyword(keyword);
    }

    @Override
    public void save(ProhibitedKeyword prohibitedKeyword) {
        String keyword = prohibitedKeyword.getKeyword();
        log.debug("Saving prohibited keyword: {}", keyword);

        if (!jpaProhibitedKeywordRepository.existsByKeyword(keyword)) {
            ProhibitedKeywordEntity entity = prohibitedKeywordMapper.toEntity(prohibitedKeyword);
            jpaProhibitedKeywordRepository.save(entity);
            log.debug("Prohibited keyword saved successfully: {}", keyword);
        } else {
            log.debug("Prohibited keyword already exists, skipping save: {}", keyword);
        }
    }

    @Override
    public void deleteByKeyword(String keyword) {
        log.debug("Deleting prohibited keyword: {}", keyword);
        jpaProhibitedKeywordRepository.deleteByKeyword(keyword);
        log.debug("Prohibited keyword deleted successfully: {}", keyword);
    }
}
