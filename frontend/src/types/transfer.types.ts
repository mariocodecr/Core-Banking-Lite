export type TransferStatus = "COMPLETADA" | "FALLIDA";

export interface Transfer {
  id: string;
  numeroCuentaOrigen: string;
  nombreClienteOrigen: string;
  numeroCuentaDestino: string;
  nombreClienteDestino: string;
  monto: number;
  moneda: string;
  descripcion: string;
  estado: TransferStatus;
  idempotencyKey: string;
  referencia: string;
  motivoFallo?: string;
  fechaTransferencia: string;
  createdAt: string;
}

export interface CreateTransferRequest {
  cuentaOrigenId: string;
  cuentaDestinoId: string;
  monto: number;
  descripcion: string;
  idempotencyKey: string;
}
