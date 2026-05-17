import api from "@/lib/axios";
import type { AccountTypeStat, DailyTransferStat, DashboardSummary } from "@/types/dashboard.types";

export const dashboardService = {
  getSummary: () =>
    api.get<DashboardSummary>("/v1/dashboard/summary"),

  getAccountTypeStats: () =>
    api.get<AccountTypeStat[]>("/v1/dashboard/accounts/by-type"),

  getDailyTransferStats: (days = 30) =>
    api.get<DailyTransferStat[]>("/v1/dashboard/transfers/daily", { params: { days } }),
};
