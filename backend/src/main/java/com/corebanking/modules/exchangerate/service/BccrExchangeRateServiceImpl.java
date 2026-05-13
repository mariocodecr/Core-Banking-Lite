package com.corebanking.modules.exchangerate.service;

import com.corebanking.config.CacheConfig;
import com.corebanking.modules.exchangerate.entity.ExchangeRate;
import com.corebanking.modules.exchangerate.repository.ExchangeRateRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fetches CRC exchange rates from two public APIs (no credentials required):
 *
 *   USD/CRC → apis.gometa.org/tdc/tdc.json
 *             Mirrors BCCR official venta rate, updated daily.
 *
 *   EUR/CRC → open.er-api.com/v6/latest/EUR
 *             Free tier, no API key, ~1500 req/month included.
 *
 * Flow:
 *   1. Redis cache (4h TTL) — fastest path
 *   2. External API call
 *   3. DB fallback — last known good value, updated on every successful fetch
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.bccr.enabled", havingValue = "true", matchIfMissing = true)
public class BccrExchangeRateServiceImpl implements ExchangeRateService {

    private static final String GOMETA_URL  = "https://apis.gometa.org/tdc/tdc.json";
    private static final String OPEN_ER_URL = "https://open.er-api.com/v6/latest/EUR";

    private final ExchangeRateRepository exchangeRateRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Cacheable(value = CacheConfig.CACHE_EXCHANGE_RATES, key = "#currency")
    @Transactional
    public BigDecimal getCrcRate(String currency) {
        if ("CRC".equals(currency)) return BigDecimal.ONE;

        try {
            BigDecimal rate = switch (currency) {
                case "USD" -> fetchUsdCrc();
                case "EUR" -> fetchEurCrc();
                default -> throw new IllegalArgumentException("Unsupported currency: " + currency);
            };
            persistRate(currency, rate);
            log.info("Exchange rate fetched: 1 {} = {} CRC", currency, rate);
            return rate;
        } catch (Exception ex) {
            log.warn("Exchange rate API unavailable for {} ({}), using last known rate", currency, ex.getMessage());
            return loadFallbackRate(currency);
        }
    }

    // ── fetchers ──────────────────────────────────────────────────────────────

    /** gometa.org mirrors the official BCCR venta rate for USD/CRC. */
    private BigDecimal fetchUsdCrc() {
        JsonNode resp = restTemplate.getForObject(GOMETA_URL, JsonNode.class);
        return new BigDecimal(resp.get("venta").asText().trim());
    }

    /** open.er-api.com provides EUR/CRC from ECB + cross-rate data. */
    private BigDecimal fetchEurCrc() {
        JsonNode resp = restTemplate.getForObject(OPEN_ER_URL, JsonNode.class);
        return resp.get("rates").get("CRC").decimalValue();
    }

    // ── persistence ───────────────────────────────────────────────────────────

    private void persistRate(String currency, BigDecimal rate) {
        ExchangeRate record = exchangeRateRepository.findById(currency)
                .orElse(new ExchangeRate());
        record.setCurrency(currency);
        record.setCrcRate(rate);
        record.setPublishedDate(LocalDate.now());
        record.setFetchedAt(LocalDateTime.now());
        exchangeRateRepository.save(record);
    }

    private BigDecimal loadFallbackRate(String currency) {
        return exchangeRateRepository.findById(currency)
                .map(r -> {
                    log.info("Fallback rate for {}: {} CRC (published {})", currency, r.getCrcRate(), r.getPublishedDate());
                    return r.getCrcRate();
                })
                .orElseThrow(() -> new IllegalStateException(
                        "No exchange rate available for " + currency + " — API unreachable and no fallback in DB yet"));
    }
}
