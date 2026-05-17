export type OrderType      = "BUY" | "SELL";
export type OrderStatus    = "PENDING" | "EXECUTED" | "FAILED";
export type InstrumentType = "ETF" | "MUTUAL_FUND";

export interface Instrument {
  symbol:           string;
  name:             string;
  instrumentType:   InstrumentType;
  lastPrice:        number | null;
  lastPriceUpdated: string | null;
}

export interface Position {
  symbol:         string;
  instrumentName: string;
  shares:         number;
  avgCost:        number;
  currentPrice:   number;
  currentValue:   number;
  invested:       number;
  pnl:            number;
  pnlPercent:     number;
}

export interface Portfolio {
  id:                string;
  accountId:         string;
  numeroCuenta:      string;
  moneda:            string;
  totalInvested:     number;
  totalCurrentValue: number;
  totalPnl:          number;
  totalPnlPercent:   number;
  positions:         Position[];
  createdAt:         string;
}

export interface InvestmentOrder {
  id:             string;
  portfolioId:    string;
  symbol:         string;
  instrumentName: string;
  tipo:           OrderType;
  shares:         number;
  pricePerShare:  number;
  totalAmount:    number;
  estado:         OrderStatus;
  fechaOrden:     string;
  errorMessage:   string | null;
}

export interface InvestmentSummary {
  totalInvested:     number;
  totalCurrentValue: number;
  totalPnl:          number;
  totalPnlPercent:   number;
  activePortfolios:  number;
  totalPositions:    number;
}

export interface BuyOrderRequest {
  accountId: string;
  symbol:    string;
  shares:    number;
}

export interface SellOrderRequest {
  accountId: string;
  symbol:    string;
  shares:    number;
}
