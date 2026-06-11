package com.market.analysis.domain.port.in;

import com.market.analysis.application.dto.ProhibitedTickerDTO;
import com.market.analysis.domain.model.PageResult;

public interface ManageProhibitedTickerUseCase {

    PageResult<ProhibitedTickerDTO> getProhibitedTickers(int pageNumber, int pageSize);

    boolean isTickerProhibited(String ticker);

    void addProhibitedTicker(ProhibitedTickerDTO ticker);

    void removeProhibitedTicker(String ticker);

}
