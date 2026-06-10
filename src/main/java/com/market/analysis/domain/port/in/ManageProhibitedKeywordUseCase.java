package com.market.analysis.domain.port.in;

import java.util.List;

import com.market.analysis.application.dto.ProhibitedKeywordDTO;

public interface ManageProhibitedKeywordUseCase {

    List<ProhibitedKeywordDTO> getAllProhibitedKeywords();

    boolean isKeywordProhibited(String keyword);

    void addProhibitedKeyword(ProhibitedKeywordDTO prohibitedKeyword);

    void removeProhibitedKeyword(String keyword);
}
