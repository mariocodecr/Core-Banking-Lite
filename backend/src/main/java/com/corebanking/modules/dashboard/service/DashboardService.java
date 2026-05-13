package com.corebanking.modules.dashboard.service;

import com.corebanking.modules.dashboard.dto.AccountTypeStatResponse;
import com.corebanking.modules.dashboard.dto.DailyTransferStatResponse;
import com.corebanking.modules.dashboard.dto.DashboardSummaryResponse;

import java.util.List;

public interface DashboardService {

    DashboardSummaryResponse getSummary();

    List<AccountTypeStatResponse> getAccountTypeStats();

    /** Returns daily transfer count + volume for the last {@code days} calendar days. */
    List<DailyTransferStatResponse> getDailyTransferStats(int days);
}
