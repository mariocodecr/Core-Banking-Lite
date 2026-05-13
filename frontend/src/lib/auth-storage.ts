import { TOKEN_KEYS } from "@/constants";
import type { UserInfo } from "@/types/auth.types";

const USER_KEY = "cbl_user";

const SESSION_COOKIE = "cbl_session";
const SESSION_MAX_AGE = 86400; // 24h

export function setTokens(accessToken: string, refreshToken: string): void {
  if (typeof window === "undefined") return;
  localStorage.setItem(TOKEN_KEYS.ACCESS, accessToken);
  localStorage.setItem(TOKEN_KEYS.REFRESH, refreshToken);
  // Session indicator for the Next.js middleware (not httpOnly — readable by JS)
  const secure = window.location.protocol === "https:" ? "; Secure" : "";
  document.cookie = `${SESSION_COOKIE}=1; path=/; max-age=${SESSION_MAX_AGE}; SameSite=Strict${secure}`;
}

export function clearTokens(): void {
  if (typeof window === "undefined") return;
  localStorage.removeItem(TOKEN_KEYS.ACCESS);
  localStorage.removeItem(TOKEN_KEYS.REFRESH);
  document.cookie = `${SESSION_COOKIE}=; path=/; max-age=0`;
}

export function getAccessToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(TOKEN_KEYS.ACCESS);
}

export function getRefreshToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(TOKEN_KEYS.REFRESH);
}

export function setUser(user: UserInfo): void {
  if (typeof window === "undefined") return;
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function getUser(): UserInfo | null {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try { return JSON.parse(raw) as UserInfo; }
  catch { return null; }
}

export function clearUser(): void {
  if (typeof window === "undefined") return;
  localStorage.removeItem(USER_KEY);
}
