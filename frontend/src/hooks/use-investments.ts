import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import axios from "axios";
import { investmentService } from "@/services/investment.service";
import type { BuyOrderRequest, SellOrderRequest } from "@/types/investment.types";

const KEYS = {
  summary:      ["investments", "summary"] as const,
  instruments:  ["investments", "instruments"] as const,
  portfolio:    (accountId: string) => ["investments", "portfolio", accountId] as const,
  orderHistory: (accountId: string) => ["investments", "orders", accountId] as const,
};

export function useCombinedPortfolio() {
  return useQuery({
    queryKey: ["investments", "portfolio", "combined"] as const,
    queryFn:  investmentService.getCombinedPortfolio,
    staleTime: 30_000,
  });
}

export function useInvestmentSummary(enabled = true) {
  return useQuery({
    queryKey: KEYS.summary,
    queryFn:  investmentService.getSummary,
    staleTime: 60_000,
    retry: false,
    enabled,
  });
}

export function useInstruments() {
  return useQuery({
    queryKey: KEYS.instruments,
    queryFn:  investmentService.getInstruments,
    staleTime: 60_000, // 1 min — prices don't change that fast on free tier
  });
}

export function usePortfolio(accountId: string | null) {
  return useQuery({
    queryKey: KEYS.portfolio(accountId ?? ""),
    queryFn:  () => investmentService.getPortfolio(accountId!),
    enabled:  !!accountId,
    retry:    (_, error) => !axios.isAxiosError(error) || error.response?.status !== 404,
  });
}

export function useOrderHistory(accountId: string | null, page = 0) {
  return useQuery({
    queryKey: [...KEYS.orderHistory(accountId ?? ""), page],
    queryFn:  () => investmentService.getOrderHistory(accountId!, { page, size: 15 }),
    enabled:  !!accountId,
    retry:    (_, error) => !axios.isAxiosError(error) || error.response?.status !== 404,
  });
}

export function useBuyOrder(onSuccess?: () => void) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: BuyOrderRequest) => investmentService.buy(data),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: KEYS.portfolio(vars.accountId) });
      qc.invalidateQueries({ queryKey: KEYS.orderHistory(vars.accountId) });
      toast.success("Orden de compra ejecutada");
      onSuccess?.();
    },
    onError: (error) => {
      const msg = axios.isAxiosError(error)
        ? error.response?.data?.message ?? "Error al ejecutar la compra"
        : "Error al ejecutar la compra";
      toast.error(msg);
    },
  });
}

export function useSellOrder(onSuccess?: () => void) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: SellOrderRequest) => investmentService.sell(data),
    onSuccess: (_, vars) => {
      qc.invalidateQueries({ queryKey: KEYS.portfolio(vars.accountId) });
      qc.invalidateQueries({ queryKey: KEYS.orderHistory(vars.accountId) });
      toast.success("Orden de venta ejecutada");
      onSuccess?.();
    },
    onError: (error) => {
      const msg = axios.isAxiosError(error)
        ? error.response?.data?.message ?? "Error al ejecutar la venta"
        : "Error al ejecutar la venta";
      toast.error(msg);
    },
  });
}
