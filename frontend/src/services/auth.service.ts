import { apiClient } from "@/lib/axios";
import type { LoginRequest, LoginResponse, RefreshTokenRequest } from "@/types/auth.types";

const BASE = "/v1/auth";

export const authService = {
  login: async (data: LoginRequest): Promise<LoginResponse> => {
    const response = await apiClient.post<LoginResponse>(`${BASE}/login`, data);
    return response.data;
  },

  refresh: async (data: RefreshTokenRequest): Promise<LoginResponse> => {
    const response = await apiClient.post<LoginResponse>(`${BASE}/refresh`, data);
    return response.data;
  },

  logout: async (): Promise<void> => {
    await apiClient.post(`${BASE}/logout`);
  },
};
