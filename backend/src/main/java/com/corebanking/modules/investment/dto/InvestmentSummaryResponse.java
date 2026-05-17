package com.corebanking.modules.investment.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InvestmentSummaryResponse {
    private BigDecimal totalInvested;
    private BigDecimal totalCurrentValue;
    private BigDecimal totalPnl;
    private BigDecimal totalPnlPercent;
    private int        activePortfolios;
    private int        totalPositions;
}
