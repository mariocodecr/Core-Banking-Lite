package com.corebanking.modules.investment.service;

import com.corebanking.config.CacheConfig;
import com.corebanking.exception.BusinessException;
import com.corebanking.exception.ErrorCode;
import com.corebanking.modules.investment.entity.Instrument;
import com.corebanking.modules.investment.repository.InstrumentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Fetches real-time ETF prices from Alpha Vantage GLOBAL_QUOTE endpoint.
 *
 * Flow:
 *   1. Redis cache (1h TTL) — fastest path
 *   2. Alpha Vantage API call
 *   3. DB fallback — Instrument.lastPrice (updated on every successful fetch)
 *
 * Free tier limits: 25 req/day, 5 req/min. The 1h cache keeps us well within limits
 * even with an active portfolio of 10 positions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.alpha-vantage.enabled", havingValue = "true", matchIfMissing = true)
public class AlphaVantageMarketDataServiceImpl implements MarketDataService {

    private static final String QUOTE_URL =
            "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol={symbol}&apikey={apikey}";

    @Value("${app.alpha-vantage.api-key}")
    private String apiKey;

    private final InstrumentRepository instrumentRepository;
    private final RestTemplate          restTemplate = new RestTemplate();

    @Override
    @Cacheable(value = CacheConfig.CACHE_MARKET_DATA, key = "#symbol")
    @Transactional
    public BigDecimal getPrice(String symbol) {
        try {
            BigDecimal price = fetchFromApi(symbol);
            persistLastPrice(symbol, price);
            log.info("Market data fetched: {} = ${}", symbol, price);
            return price;
        } catch (Exception ex) {
            log.warn("Alpha Vantage unavailable for {} ({}), using last known price", symbol, ex.getMessage());
            return loadFallbackPrice(symbol);
        }
    }

    // ── private ──────────────────────────────────────────────────────────────

    private BigDecimal fetchFromApi(String symbol) {
        JsonNode root = restTemplate.getForObject(QUOTE_URL, JsonNode.class, symbol, apiKey);

        // Alpha Vantage returns {"Note": "..."} or {"Information": "..."} on rate limit / bad key
        if (root == null || root.has("Note") || root.has("Information")) {
            throw new IllegalStateException("Alpha Vantage rate limit or invalid key for symbol: " + symbol);
        }

        JsonNode quote = root.get("Global Quote");
        if (quote == null || quote.isEmpty() || !quote.has("05. price")) {
            throw new IllegalStateException("Empty Global Quote response for symbol: " + symbol);
        }

        String rawPrice = quote.get("05. price").asText().trim();
        return new BigDecimal(rawPrice);
    }

    @CachePut(value = CacheConfig.CACHE_MARKET_DATA, key = "#symbol")
    private void persistLastPrice(String symbol, BigDecimal price) {
        instrumentRepository.findById(symbol).ifPresent(instrument -> {
            instrument.setLastPrice(price);
            instrument.setLastPriceUpdated(LocalDateTime.now());
            instrumentRepository.save(instrument);
        });
    }

    private BigDecimal loadFallbackPrice(String symbol) {
        return instrumentRepository.findById(symbol)
                .filter(i -> i.getLastPrice() != null)
                .map(instrument -> {
                    log.info("Fallback price for {}: ${} (updated {})",
                            symbol, instrument.getLastPrice(), instrument.getLastPriceUpdated());
                    return instrument.getLastPrice();
                })
                .orElseThrow(() -> new BusinessException(ErrorCode.MARKET_DATA_UNAVAILABLE,
                        "No price available for " + symbol + " — API unreachable and no fallback in DB"));
    }
}
