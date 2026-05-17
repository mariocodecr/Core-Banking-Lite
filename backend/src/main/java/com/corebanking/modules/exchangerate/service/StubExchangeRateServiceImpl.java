package com.corebanking.modules.exchangerate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Stub used in test profiles (app.bccr.enabled=false).
 * Returns fixed rates so integration tests never call the real BCCR API.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.bccr.enabled", havingValue = "false")
public class StubExchangeRateServiceImpl implements ExchangeRateService {

    @Override
    public BigDecimal getCrcRate(String currency) {
        return switch (currency) {
            case "USD" -> new BigDecimal("507.50");
            case "EUR" -> new BigDecimal("555.00");
            default    -> BigDecimal.ONE;
        };
    }
}
