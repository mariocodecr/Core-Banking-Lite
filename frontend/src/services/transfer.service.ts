import api from "@/lib/axios";
import type { PagedResponse } from "@/types";
import type { CreateTransferRequest, Transfer } from "@/types/transfer.types";

export const transferService = {
  getAll: (params?: { page?: number; size?: number }) =>
    api.get<PagedResponse<Transfer>>("/v1/transfers", { params }),

  getById: (id: string) =>
    api.get<Transfer>(`/v1/transfers/${id}`),

  getMine: (params?: { page?: number; size?: number }) =>
    api.get<PagedResponse<Transfer>>("/v1/transfers/me", { params }),

  create: (data: CreateTransferRequest) =>
    api.post<Transfer>("/v1/transfers", data),
};
