"use client";

import { useState, useEffect } from "react";
import { getUser } from "@/lib/auth-storage";
import type { UserInfo, Role } from "@/types/auth.types";

export function useCurrentUser(): UserInfo | null {
  const [user, setUser] = useState<UserInfo | null>(null);

  useEffect(() => {
    setUser(getUser());
  }, []);

  return user;
}

export function usePermissions() {
  const user = useCurrentUser();
  const role: Role | undefined = user?.role;

  return {
    role,
    // Nav visibility
    canViewDashboard:    true,
    canViewCustomers:    role !== "CLIENT",
    canViewSavings:      role !== "CLIENT",
    canViewAudit:        role !== "CLIENT",
    // Customer actions
    canManageCustomers:  role === "ADMIN" || role === "ADVISOR",
    canDeleteCustomers:  role === "ADMIN",
    // Account actions
    canCreateAccounts:   role === "ADMIN" || role === "ADVISOR",
    canFreezeAccounts:   role === "ADMIN" || role === "ADVISOR",
    canCloseAccounts:    role === "ADMIN",
    // Transfer actions
    canCreateTransfers:  role !== "AUDITOR",
  };
}
