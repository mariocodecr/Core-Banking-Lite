export const APP_NAME = "Core Banking Lite";

export const ROUTES = {
  HOME: "/",
  LOGIN: "/login",
  DASHBOARD: "/dashboard",
  CUSTOMERS: "/dashboard/customers",
  ACCOUNTS: "/dashboard/accounts",
  TRANSFERS: "/dashboard/transfers",
  SAVINGS: "/dashboard/savings",
  AUDIT: "/dashboard/audit",
} as const;

export const PAGINATION_DEFAULTS = {
  PAGE: 0,
  SIZE: 10,
  SORT: "createdAt,desc",
} as const;

export const TOKEN_KEYS = {
  ACCESS: "cbl_access_token",
  REFRESH: "cbl_refresh_token",
} as const;
