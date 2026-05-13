import { useQuery } from "@tanstack/react-query";
import { dashboardService } from "@/services/dashboard.service";

export function useDashboardSummary() {
  return useQuery({
    queryKey: ["dashboard", "summary"],
    queryFn: () => dashboardService.getSummary().then((r) => r.data),
    refetchInterval: 60_000,
  });
}

export function useAccountTypeStats() {
  return useQuery({
    queryKey: ["dashboard", "accounts-by-type"],
    queryFn: () => dashboardService.getAccountTypeStats().then((r) => r.data),
  });
}

export function useDailyTransferStats(days = 30) {
  return useQuery({
    queryKey: ["dashboard", "daily-transfers", days],
    queryFn: () => dashboardService.getDailyTransferStats(days).then((r) => r.data),
  });
}
