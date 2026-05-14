package com.corebanking.modules.investment.dto;

import com.corebanking.modules.investment.entity.OrderStatus;
import com.corebanking.modules.investment.entity.OrderType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class InvestmentOrderResponse {
    private UUID id;
    private UUID portfolioId;
    private String symbol;
    private String instrumentName;
    private OrderType tipo;
    private BigDecimal shares;
    private BigDecimal pricePerShare;
    private BigDecimal totalAmount;
    private OrderStatus estado;
    private LocalDateTime fechaOrden;
    private String errorMessage;
}
