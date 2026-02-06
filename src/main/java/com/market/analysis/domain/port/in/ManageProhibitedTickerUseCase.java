package com.market.analysis.domain.port.in;

import java.util.List;

import com.market.analysis.domain.model.ProhibitedTicker;

public interface ManageProhibitedTickerUseCase {

    List<ProhibitedTicker> getAllProhibitedTickers();

    boolean isTickerProhibited(String ticker);

    void addProhibitedTicker(ProhibitedTicker ticker);

    void removeProhibitedTicker(Long id);

}
