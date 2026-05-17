import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { accountService } from "@/services/account.service";
import type { AccountFilterParams, CreateAccountRequest } from "@/types/account.types";

const QUERY_KEY = "accounts";

export function useAccounts(params: AccountFilterParams, enabled = true) {
  return useQuery({
    queryKey: [QUERY_KEY, params],
    queryFn: () => accountService.findAll(params),
    enabled,
  });
}

export function useMyAccounts() {
  return useQuery({
    queryKey: [QUERY_KEY, "me"],
    queryFn: accountService.findMine,
  });
}

export function useAccountsByCustomer(customerId: string) {
  return useQuery({
    queryKey: [QUERY_KEY, "customer", customerId],
    queryFn: () => accountService.findByCustomerId(customerId),
    enabled: !!customerId,
  });
}

export function useAccountMovements(accountId: string, page = 0) {
  return useQuery({
    queryKey: [QUERY_KEY, accountId, "movements", page],
    queryFn: () => accountService.getMovements(accountId, { page, size: 15 }),
    enabled: !!accountId,
  });
}

export function useCreateAccount(onSuccess?: () => void) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateAccountRequest) => accountService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
      toast.success("Cuenta creada exitosamente");
      onSuccess?.();
    },
    onError: () => {
      toast.error("Error al crear la cuenta.");
    },
  });
}

export function useFreezeAccount() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => accountService.freeze(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
      toast.success("Cuenta congelada");
    },
  });
}

export function useUnfreezeAccount() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => accountService.unfreeze(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
      toast.success("Cuenta descongelada");
    },
  });
}

export function useCloseAccount() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => accountService.close(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
      toast.success("Cuenta cerrada");
    },
    onError: () => {
      toast.error("No se puede cerrar la cuenta. Verificá que el saldo sea cero.");
    },
  });
}
