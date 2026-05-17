package com.corebanking.modules.investment.dto;

import com.corebanking.modules.investment.entity.InstrumentType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class InstrumentResponse {
    private String symbol;
    private String name;
    private InstrumentType instrumentType;
    private BigDecimal lastPrice;
    private LocalDateTime lastPriceUpdated;
}
