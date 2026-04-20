package com.market.analysis.domain.port.out;

import java.util.List;

import com.market.analysis.domain.model.ProhibitedKeyword;

public interface ProhibitedKeywordRepository {

    List<ProhibitedKeyword> findAll();

    boolean existsByKeyword(String keyword);

    void save(ProhibitedKeyword prohibitedKeyword);

    void deleteByKeyword(String keyword);
}
