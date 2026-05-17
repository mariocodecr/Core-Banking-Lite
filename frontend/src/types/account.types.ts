import type { PaginationParams } from "@/types";

export type AccountType   = "AHORROS" | "EMPRESARIAL" | "CORRIENTE";
export type AccountStatus = "ACTIVA" | "CONGELADA" | "CERRADA";
export type MovementType  = "DEPOSITO" | "RETIRO" | "TRANSFERENCIA_ENTRADA" | "TRANSFERENCIA_SALIDA" | "COMPRA_INVERSION" | "VENTA_INVERSION";

export interface Account {
  id:           string;
  numeroCuenta: string;
  customerId:   string;
  nombreCliente: string;
  tipo:         AccountType;
  estado:       AccountStatus;
  saldo:        number;
  moneda:       string;
  fechaApertura: string;
  fechaCierre:  string | null;
  createdAt:    string;
  version:      number;
}

export interface AccountMovement {
  id:              string;
  tipo:            MovementType;
  monto:           number;
  saldoAnterior:   number;
  saldoPosterior:  number;
  descripcion:     string | null;
  referencia:      string | null;
  fechaMovimiento: string;
}

export interface CreateAccountRequest {
  customerId:    string;
  tipo:          AccountType;
  moneda:        string;
  saldoInicial?: number;
}

export interface AccountFilterParams extends PaginationParams {
  customerId?:   string;
  tipo?:         AccountType;
  estado?:       AccountStatus;
  numeroCuenta?: string;
}
