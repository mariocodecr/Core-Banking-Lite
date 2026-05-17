import { apiClient } from "@/lib/axios";
import type { PagedResponse } from "@/types";
import type {
  Instrument,
  Portfolio,
  InvestmentOrder,
  InvestmentSummary,
  BuyOrderRequest,
  SellOrderRequest,
} from "@/types/investment.types";

const BASE = "/v1/investments";

export const investmentService = {
  getCombinedPortfolio: async (): Promise<Portfolio> => {
    const { data } = await apiClient.get<Portfolio>(`${BASE}/portfolio`);
    return data;
  },

  getSummary: async (): Promise<InvestmentSummary> => {
    const { data } = await apiClient.get<InvestmentSummary>(`${BASE}/summary`);
    return data;
  },

  getInstruments: async (): Promise<Instrument[]> => {
    const { data } = await apiClient.get<Instrument[]>(`${BASE}/instruments`);
    return data;
  },

  getQuote: async (symbol: string): Promise<Instrument> => {
    const { data } = await apiClient.get<Instrument>(`${BASE}/instruments/${symbol}/quote`);
    return data;
  },

  getPortfolio: async (accountId: string): Promise<Portfolio> => {
    const { data } = await apiClient.get<Portfolio>(`${BASE}/portfolios/${accountId}`);
    return data;
  },

  buy: async (payload: BuyOrderRequest): Promise<InvestmentOrder> => {
    const { data } = await apiClient.post<InvestmentOrder>(`${BASE}/orders/buy`, payload);
    return data;
  },

  sell: async (payload: SellOrderRequest): Promise<InvestmentOrder> => {
    const { data } = await apiClient.post<InvestmentOrder>(`${BASE}/orders/sell`, payload);
    return data;
  },

  getOrderHistory: async (
    accountId: string,
    params?: { page?: number; size?: number },
  ): Promise<PagedResponse<InvestmentOrder>> => {
    const { data } = await apiClient.get<PagedResponse<InvestmentOrder>>(
      `${BASE}/orders/${accountId}`,
      { params },
    );
    return data;
  },
};
