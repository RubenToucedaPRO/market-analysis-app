package com.market.analysis.domain.port.in;

import com.market.analysis.application.dto.ProhibitedKeywordDTO;
import com.market.analysis.domain.model.PageResult;

public interface ManageProhibitedKeywordUseCase {

    PageResult<ProhibitedKeywordDTO> getProhibitedKeywords(int pageNumber, int pageSize);

    boolean isKeywordProhibited(String keyword);

    void addProhibitedKeyword(ProhibitedKeywordDTO prohibitedKeyword);

    void removeProhibitedKeyword(String keyword);
}
