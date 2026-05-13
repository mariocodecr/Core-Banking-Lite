package com.corebanking.modules.exchangerate.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ExchangeRateResponse {
    private String from;
    private String to;
    private BigDecimal rate;
    private LocalDateTime timestamp;
}
