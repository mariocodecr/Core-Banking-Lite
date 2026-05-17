import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { transferService } from "@/services/transfer.service";
import type { CreateTransferRequest } from "@/types/transfer.types";

const QUERY_KEY = "transfers";

export function useTransfers(params?: { page?: number; size?: number }, enabled = true) {
  return useQuery({
    queryKey: [QUERY_KEY, params],
    queryFn: () => transferService.getAll(params).then((r) => r.data),
    enabled,
  });
}

export function useMyTransfers(params?: { page?: number; size?: number }, enabled = true) {
  return useQuery({
    queryKey: [QUERY_KEY, "me", params],
    queryFn: () => transferService.getMine(params).then((r) => r.data),
    enabled,
  });
}

export function useTransfer(id: string) {
  return useQuery({
    queryKey: [QUERY_KEY, id],
    queryFn: () => transferService.getById(id).then((r) => r.data),
    enabled: !!id,
  });
}

export function useCreateTransfer() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateTransferRequest) =>
      transferService.create(data).then((r) => r.data),
    onSuccess: (transfer) => {
      qc.invalidateQueries({ queryKey: [QUERY_KEY] });
      qc.invalidateQueries({ queryKey: ["accounts"] });
      toast.success(`Transferencia ${transfer.referencia} completada`);
    },
    onError: (err: { response?: { data?: { message?: string } } }) => {
      toast.error(err.response?.data?.message ?? "Error al procesar la transferencia");
    },
  });
}
