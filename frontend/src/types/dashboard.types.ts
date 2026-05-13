import type { AccountType } from "./account.types";

export interface DashboardSummary {
  totalCustomers: number;
  activeCustomers: number;
  totalAccounts: number;
  totalBalancePEN: number;
  totalBalanceUSD: number;
  totalTransfersToday: number;
  transferVolumeToday: number;
  totalTransfersThisMonth: number;
  transferVolumeThisMonth: number;
}

export interface AccountTypeStat {
  tipo: AccountType;
  count: number;
  totalBalance: number;
}

export interface DailyTransferStat {
  date: string;
  count: number;
  volume: number;
}
