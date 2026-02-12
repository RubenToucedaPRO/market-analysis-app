package com.market.analysis.domain.port.in;

import java.util.List;

import com.market.analysis.application.dto.ProhibitedTickerDTO;

public interface ManageProhibitedTickerUseCase {

    List<ProhibitedTickerDTO> getAllProhibitedTickers();

    boolean isTickerProhibited(String ticker);

    void addProhibitedTicker(ProhibitedTickerDTO ticker);

    void removeProhibitedTicker(String ticker);

}
