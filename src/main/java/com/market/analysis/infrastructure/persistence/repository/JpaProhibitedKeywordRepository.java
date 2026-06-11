package com.market.analysis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.market.analysis.infrastructure.persistence.entity.ProhibitedKeywordEntity;
public interface JpaProhibitedKeywordRepository extends JpaRepository<ProhibitedKeywordEntity, Long> {

    boolean existsByKeyword(String keyword);

    @Transactional
    void deleteByKeyword(String keyword);
}
