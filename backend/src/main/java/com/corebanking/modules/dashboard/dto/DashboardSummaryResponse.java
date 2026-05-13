package com.corebanking.modules.dashboard.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Getter
@Builder
@Jacksonized
public class DashboardSummaryResponse {

    private long totalCustomers;
    private long activeCustomers;

    private long totalAccounts;
    private BigDecimal totalBalancePEN;
    private BigDecimal totalBalanceUSD;

    private long totalTransfersToday;
    private BigDecimal transferVolumeToday;
    private long totalTransfersThisMonth;
    private BigDecimal transferVolumeThisMonth;
}
