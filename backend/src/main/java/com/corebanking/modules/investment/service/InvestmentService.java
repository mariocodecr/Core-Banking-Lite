package com.corebanking.modules.investment.service;

import com.corebanking.modules.investment.dto.*;
import com.corebanking.shared.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface InvestmentService {

    List<InstrumentResponse> getAvailableInstruments();

    InstrumentResponse getInstrumentQuote(String symbol);

    PortfolioResponse getPortfolio(UUID accountId);

    InvestmentOrderResponse buy(BuyOrderRequest request);

    InvestmentOrderResponse sell(SellOrderRequest request);

    PagedResponse<InvestmentOrderResponse> getOrderHistory(UUID accountId, Pageable pageable);

    InvestmentSummaryResponse getInvestmentSummary();

    PortfolioResponse getCombinedPortfolio();
}
