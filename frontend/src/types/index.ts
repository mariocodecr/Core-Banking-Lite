export interface ApiResponse<T> {
  data: T;
  message?: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
}

export interface ErrorResponse {
  errorCode: string;
  message: string;
  status: number;
  timestamp: string;
  path: string;
  errors?: FieldError[];
}

export interface FieldError {
  field: string;
  message: string;
  rejectedValue?: unknown;
}

export interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
}
