import { apiClient } from "@/lib/axios";
import type { PagedResponse } from "@/types";
import type {
  Account,
  AccountFilterParams,
  AccountMovement,
  CreateAccountRequest,
} from "@/types/account.types";

const BASE = "/v1/accounts";

export const accountService = {
  findAll: async (params: AccountFilterParams): Promise<PagedResponse<Account>> => {
    const { data } = await apiClient.get<PagedResponse<Account>>(BASE, { params });
    return data;
  },

  findById: async (id: string): Promise<Account> => {
    const { data } = await apiClient.get<Account>(`${BASE}/${id}`);
    return data;
  },

  findByCustomerId: async (customerId: string): Promise<Account[]> => {
    const { data } = await apiClient.get<Account[]>(`${BASE}/customer/${customerId}`);
    return data;
  },

  create: async (payload: CreateAccountRequest): Promise<Account> => {
    const { data } = await apiClient.post<Account>(BASE, payload);
    return data;
  },

  getMovements: async (
    accountId: string,
    params?: { page?: number; size?: number },
  ): Promise<PagedResponse<AccountMovement>> => {
    const { data } = await apiClient.get<PagedResponse<AccountMovement>>(
      `${BASE}/${accountId}/movements`,
      { params },
    );
    return data;
  },

  freeze: async (id: string): Promise<Account> => {
    const { data } = await apiClient.patch<Account>(`${BASE}/${id}/freeze`);
    return data;
  },

  unfreeze: async (id: string): Promise<Account> => {
    const { data } = await apiClient.patch<Account>(`${BASE}/${id}/unfreeze`);
    return data;
  },

  close: async (id: string): Promise<Account> => {
    const { data } = await apiClient.patch<Account>(`${BASE}/${id}/close`);
    return data;
  },
};
