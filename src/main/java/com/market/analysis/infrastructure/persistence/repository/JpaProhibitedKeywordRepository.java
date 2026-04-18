package com.market.analysis.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.market.analysis.infrastructure.persistence.entity.ProhibitedKeywordEntity;

@Repository
public interface JpaProhibitedKeywordRepository extends JpaRepository<ProhibitedKeywordEntity, Long> {

    boolean existsByKeyword(String keyword);

    void deleteByKeyword(String keyword);
}
