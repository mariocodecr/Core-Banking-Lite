package com.corebanking.modules.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DailyTransferStatResponse {
    private LocalDate date;
    private long count;
    private BigDecimal volume;
}
