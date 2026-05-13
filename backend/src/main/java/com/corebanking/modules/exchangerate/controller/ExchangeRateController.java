package com.corebanking.modules.exchangerate.controller;

import com.corebanking.modules.exchangerate.dto.ExchangeRateResponse;
import com.corebanking.modules.exchangerate.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Tag(name = "Exchange Rates", description = "Real-time exchange rates via BCCR")
@RestController
@RequestMapping("/v1/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @Operation(summary = "Get exchange rate between two currencies")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExchangeRateResponse> getRate(
            @RequestParam String from,
            @RequestParam String to) {

        BigDecimal rate = exchangeRateService.getRate(from, to);
        return ResponseEntity.ok(new ExchangeRateResponse(from, to, rate, LocalDateTime.now()));
    }
}
