package com.corebanking.modules.investment.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PositionResponse {
    private String symbol;
    private String instrumentName;
    private BigDecimal shares;
    private BigDecimal avgCost;
    private BigDecimal currentPrice;
    private BigDecimal currentValue;
    private BigDecimal invested;
    private BigDecimal pnl;
    private BigDecimal pnlPercent;
}
