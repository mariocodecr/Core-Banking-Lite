package com.corebanking.modules.exchangerate.service;

import com.corebanking.config.CacheConfig;
import com.corebanking.modules.exchangerate.entity.ExchangeRate;
import com.corebanking.modules.exchangerate.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Live implementation backed by the BCCR (Banco Central de Costa Rica) web service.
 * Active whenever app.bccr.enabled=true (the default).
 *
 * Flow:
 *   1. Redis cache (4h TTL) — fastest layer
 *   2. BCCR API — fetches last 7 days, takes the most recent rate
 *   3. DB fallback — last known good value persisted on every successful BCCR fetch
 *
 * This means even if BCCR is down or hasn't published today's rates yet,
 * the system always has a valid rate to work with.
 *
 * BCCR indicators:
 *   318  — USD/CRC venta  (CRC per 1 USD)
 *   3501 — EUR/CRC venta  (CRC per 1 EUR)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.bccr.enabled", havingValue = "true", matchIfMissing = true)
public class BccrExchangeRateServiceImpl implements ExchangeRateService {

    private static final String BCCR_URL =
            "https://gee.bccr.fi.cr/Indicadores/Suscripciones/WS/wsindicadoreseconomicos.asmx/ObtenerIndicadoresEconomicos";
    private static final int INDICADOR_USD_CRC = 318;
    private static final int INDICADOR_EUR_CRC = 3501;
    // BCCR uses comma OR period as decimal separator depending on locale
    private static final Pattern NUM_VALOR_PATTERN = Pattern.compile("\"NUM_VALOR\"\\s*:\\s*([\\d.,]+)");
    private static final DateTimeFormatter BCCR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Value("${app.bccr.email}")
    private String email;

    @Value("${app.bccr.token}")
    private String token;

    private final ExchangeRateRepository exchangeRateRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Cacheable(value = CacheConfig.CACHE_EXCHANGE_RATES, key = "#currency")
    @Transactional
    public BigDecimal getCrcRate(String currency) {
        if ("CRC".equals(currency)) return BigDecimal.ONE;

        int indicador = "USD".equals(currency) ? INDICADOR_USD_CRC : INDICADOR_EUR_CRC;

        try {
            BigDecimal rate = fetchFromBccr(indicador);
            persistRate(currency, rate);
            log.info("BCCR rate fetched: 1 {} = {} CRC", currency, rate);
            return rate;
        } catch (Exception ex) {
            log.warn("BCCR unavailable for {} ({}), falling back to last known rate", currency, ex.getMessage());
            return loadFallbackRate(currency);
        }
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private BigDecimal fetchFromBccr(int indicador) {
        // 7-day window so we always get the latest published rate even on weekends,
        // holidays, or before BCCR publishes today's rates (typically after ~9 AM CR time).
        String fechaFinal  = LocalDate.now().format(BCCR_DATE);
        String fechaInicio = LocalDate.now().minusDays(7).format(BCCR_DATE);

        String url = UriComponentsBuilder.fromHttpUrl(BCCR_URL)
                .queryParam("Indicador", indicador)
                .queryParam("FechaInicio", fechaInicio)
                .queryParam("FechaFinal", fechaFinal)
                .queryParam("Nombre", "CoreBankingLite")
                .queryParam("SubNiveles", "N")
                .queryParam("CorreoElectronico", email)
                .queryParam("Token", token)
                .build(false)
                .toUriString();

        log.debug("Fetching BCCR: indicador={}, from={}, to={}", indicador, fechaInicio, fechaFinal);

        String xmlResponse = restTemplate.getForObject(url, String.class);
        String json = extractJsonFromXml(xmlResponse);
        return extractLastNumValor(json, indicador);
    }

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
                    log.info("Using last known BCCR rate for {}: {} CRC (from {})", currency, r.getCrcRate(), r.getPublishedDate());
                    return r.getCrcRate();
                })
                .orElseThrow(() -> new IllegalStateException(
                        "No exchange rate available for " + currency + " — BCCR unreachable and no fallback in DB"));
    }

    private String extractJsonFromXml(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));
            return doc.getDocumentElement().getTextContent();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse BCCR XML response: " + e.getMessage(), e);
        }
    }

    /**
     * BCCR returns one entry per day in the window. Take the LAST match = most recent rate.
     * Normalises comma decimal separator (Costa Rican locale) to period before parsing.
     */
    private BigDecimal extractLastNumValor(String json, int indicador) {
        Matcher matcher = NUM_VALOR_PATTERN.matcher(json);
        String lastValue = null;
        while (matcher.find()) {
            lastValue = matcher.group(1);
        }
        if (lastValue == null) {
            log.warn("No NUM_VALOR in BCCR response for indicador={}. JSON: {}", indicador, json);
            throw new IllegalStateException("No rate data in BCCR response for indicador=" + indicador);
        }
        return new BigDecimal(lastValue.replace(',', '.'));
    }
}
