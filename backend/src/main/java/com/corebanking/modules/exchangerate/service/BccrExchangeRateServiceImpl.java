package com.corebanking.modules.exchangerate.service;

import com.corebanking.config.CacheConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Live implementation backed by the BCCR (Banco Central de Costa Rica) web service.
 * Active whenever app.bccr.enabled=true (the default).
 *
 * Rates are cached for 4 hours to avoid hammering the external API.
 *
 * BCCR indicators used:
 *   318  — USD/CRC venta  (how many CRC per 1 USD)
 *   3501 — EUR/CRC venta  (how many CRC per 1 EUR)
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.bccr.enabled", havingValue = "true", matchIfMissing = true)
public class BccrExchangeRateServiceImpl implements ExchangeRateService {

    private static final String BCCR_URL =
            "https://gee.bccr.fi.cr/Indicadores/Suscripciones/WS/wsindicadoreseconomicos.asmx/ObtenerIndicadoresEconomicos";
    private static final int INDICADOR_USD_CRC = 318;
    private static final int INDICADOR_EUR_CRC = 3501;
    private static final Pattern NUM_VALOR_PATTERN = Pattern.compile("\"NUM_VALOR\"\\s*:\\s*([\\d.]+)");
    private static final DateTimeFormatter BCCR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Value("${app.bccr.email}")
    private String email;

    @Value("${app.bccr.token}")
    private String token;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Cacheable(value = CacheConfig.CACHE_EXCHANGE_RATES, key = "#currency")
    public BigDecimal getCrcRate(String currency) {
        if ("CRC".equals(currency)) return BigDecimal.ONE;
        int indicador = "USD".equals(currency) ? INDICADOR_USD_CRC : INDICADOR_EUR_CRC;
        BigDecimal rate = fetchRate(indicador);
        log.info("BCCR exchange rate fetched: 1 {} = {} CRC", currency, rate);
        return rate;
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private BigDecimal fetchRate(int indicador) {
        String today = LocalDate.now().format(BCCR_DATE);
        String url = UriComponentsBuilder.fromHttpUrl(BCCR_URL)
                .queryParam("Indicador", indicador)
                .queryParam("FechaInicio", today)
                .queryParam("FechaFinal", today)
                .queryParam("Nombre", "CoreBankingLite")
                .queryParam("SubNiveles", "N")
                .queryParam("CorreoElectronico", email)
                .queryParam("Token", token)
                .build(false)
                .toUriString();

        String xmlResponse = restTemplate.getForObject(url, String.class);
        String json = extractJsonFromXml(xmlResponse);
        return extractNumValor(json, indicador);
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

    private BigDecimal extractNumValor(String json, int indicador) {
        Matcher matcher = NUM_VALOR_PATTERN.matcher(json);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        throw new IllegalStateException(
                "NUM_VALOR not found in BCCR response for indicador=" + indicador);
    }
}
