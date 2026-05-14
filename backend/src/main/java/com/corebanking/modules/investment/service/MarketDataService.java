package com.corebanking.modules.investment.service;

import java.math.BigDecimal;

public interface MarketDataService {

    /**
     * Returns the latest price in USD for the given ETF symbol.
     * Uses Redis cache (1h TTL) → Alpha Vantage → DB fallback (last known price).
     *
     * @throws com.corebanking.exception.BusinessException if no price is available at all
     */
    BigDecimal getPrice(String symbol);
}
