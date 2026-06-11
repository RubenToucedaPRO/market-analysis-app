package com.market.analysis.domain.port.out;

import java.util.List;

import com.market.analysis.domain.model.PageResult;
import com.market.analysis.domain.model.ProhibitedKeyword;

public interface ProhibitedKeywordRepository {

    List<ProhibitedKeyword> findAll();

    /**
     * Retrieves a paginated subset of prohibited keywords from the database.
     *
     * @param pageNumber zero-based page index
     * @param pageSize   number of items per page
     * @return PageResult containing the page content and pagination metadata
     */
    PageResult<ProhibitedKeyword> findAll(int pageNumber, int pageSize);

    boolean existsByKeyword(String keyword);

    void save(ProhibitedKeyword prohibitedKeyword);

    void deleteByKeyword(String keyword);
}
