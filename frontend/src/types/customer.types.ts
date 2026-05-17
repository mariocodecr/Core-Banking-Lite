import type { PaginationParams } from "@/types";

export type DocumentType = "DNI" | "CE" | "RUC" | "PASAPORTE";
export type CustomerStatus = "ACTIVO" | "INACTIVO";

export interface Customer {
  id: string;
  tipoDocumento: DocumentType;
  numeroDocumento: string;
  nombres: string;
  apellidos: string;
  nombreCompleto: string;
  email: string | null;
  telefono: string | null;
  fechaNacimiento: string | null;
  estado: CustomerStatus;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
}

export interface CustomerRequest {
  tipoDocumento: DocumentType;
  numeroDocumento: string;
  nombres: string;
  apellidos: string;
  email?: string;
  telefono?: string;
  fechaNacimiento?: string;
}

export interface CustomerFilterParams extends PaginationParams {
  nombre?: string;
  numeroDocumento?: string;
  email?: string;
  estado?: CustomerStatus;
}
