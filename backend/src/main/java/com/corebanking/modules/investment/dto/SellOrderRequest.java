package com.corebanking.modules.investment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class SellOrderRequest {

    @NotNull
    private UUID accountId;

    @NotBlank
    private String symbol;

    @NotNull
    @Positive
    private BigDecimal shares;
}
