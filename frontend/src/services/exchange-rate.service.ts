import apiClient from "@/lib/axios";

export interface ExchangeRateResponse {
  from: string;
  to: string;
  rate: number;
  timestamp: string;
}

export async function getExchangeRate(from: string, to: string): Promise<ExchangeRateResponse> {
  const { data } = await apiClient.get<ExchangeRateResponse>("/v1/exchange-rates", {
    params: { from, to },
  });
  return data;
}
