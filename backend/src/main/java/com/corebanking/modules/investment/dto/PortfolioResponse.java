package com.corebanking.modules.investment.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class PortfolioResponse {
    private UUID id;
    private UUID accountId;
    private String numeroCuenta;
    private String moneda;
    private BigDecimal totalInvested;
    private BigDecimal totalCurrentValue;
    private BigDecimal totalPnl;
    private BigDecimal totalPnlPercent;
    private List<PositionResponse> positions;
    private LocalDateTime createdAt;
}
