import { apiClient } from "@/lib/axios";
import type { PagedResponse } from "@/types";
import type { Customer, CustomerFilterParams, CustomerRequest } from "@/types/customer.types";

const BASE = "/v1/customers";

export const customerService = {
  findAll: async (params: CustomerFilterParams): Promise<PagedResponse<Customer>> => {
    const { data } = await apiClient.get<PagedResponse<Customer>>(BASE, { params });
    return data;
  },

  findById: async (id: string): Promise<Customer> => {
    const { data } = await apiClient.get<Customer>(`${BASE}/${id}`);
    return data;
  },

  create: async (payload: CustomerRequest): Promise<Customer> => {
    const { data } = await apiClient.post<Customer>(BASE, payload);
    return data;
  },

  update: async (id: string, payload: CustomerRequest): Promise<Customer> => {
    const { data } = await apiClient.put<Customer>(`${BASE}/${id}`, payload);
    return data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`${BASE}/${id}`);
  },

  updateStatus: async (id: string, status: "ACTIVO" | "INACTIVO"): Promise<Customer> => {
    const { data } = await apiClient.patch<Customer>(`${BASE}/${id}/status`, null, {
      params: { status },
    });
    return data;
  },
};
