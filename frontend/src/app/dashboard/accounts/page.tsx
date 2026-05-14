"use client";

import { useState } from "react";
import { Plus, Search } from "lucide-react";
import { useAccounts, useMyAccounts, useCloseAccount, useFreezeAccount, useUnfreezeAccount } from "@/hooks/use-accounts";
import { AccountCard } from "@/features/accounts/account-card";
import { AccountMovements } from "@/features/accounts/account-movements";
import { CreateAccountDrawer } from "@/features/accounts/create-account-drawer";
import type { Account, AccountFilterParams } from "@/types/account.types";
import { PAGINATION_DEFAULTS } from "@/constants";
import { usePermissions } from "@/hooks/use-current-user";

export default function AccountsPage() {
  const [filters, setFilters] = useState<AccountFilterParams>({
    page: PAGINATION_DEFAULTS.PAGE,
    size: 20,
    sort: "createdAt,desc",
  });
  const [search, setSearch] = useState("");
  const [selectedAccount, setSelectedAccount] = useState<Account | null>(null);
  const [drawerOpen, setDrawerOpen] = useState(false);

  const { role, canCreateAccounts, canFreezeAccounts, canCloseAccounts } = usePermissions();
  const isClient = role === "CLIENT";

  const { data: pagedData, isLoading: pagedLoading, isError: pagedError } = useAccounts(filters, !!role && !isClient);
  const { data: myAccounts, isLoading: myLoading, isError: myError } = useMyAccounts();

  const accounts  = isClient ? (myAccounts ?? [])          : (pagedData?.content ?? []);
  const isLoading = isClient ? myLoading                   : pagedLoading;
  const isError   = isClient ? myError                     : pagedError;
  const totalLabel = isClient
    ? `${accounts.length} cuentas`
    : (pagedData ? `${pagedData.totalElements} cuentas` : "Cargando...");

  const { mutate: freeze }   = useFreezeAccount();
  const { mutate: unfreeze } = useUnfreezeAccount();
  const { mutate: close }    = useCloseAccount();

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setFilters((prev) => ({ ...prev, numeroCuenta: search || undefined, page: 0 }));
  };

  const handleAction = (action: "freeze" | "unfreeze" | "close") => {
    if (!selectedAccount) return;
    const label = { freeze: "congelar", unfreeze: "descongelar", close: "cerrar" }[action];
    if (!confirm(`¿Seguro que querés ${label} la cuenta ${selectedAccount.numeroCuenta}?`)) return;
    if (action === "freeze")   freeze(selectedAccount.id);
    if (action === "unfreeze") unfreeze(selectedAccount.id);
    if (action === "close")    close(selectedAccount.id);
  };

  return (
    <div className="flex h-full flex-col gap-0 lg:flex-row">
      {/* ── Left: account list ───────────────────────────────── */}
      <div className="flex w-full flex-col gap-4 border-r border-slate-200 p-6 dark:border-slate-800 lg:w-[420px] lg:overflow-y-auto">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-xl font-bold text-slate-900 dark:text-white">Cuentas</h1>
            <p className="mt-0.5 text-xs text-slate-500">{totalLabel}</p>
          </div>
          {canCreateAccounts && (
            <button
              onClick={() => setDrawerOpen(true)}
              className="flex items-center gap-1.5 rounded-xl bg-blue-600 px-3 py-2 text-xs font-semibold text-white hover:bg-blue-700"
            >
              <Plus className="h-3.5 w-3.5" />
              Nueva
            </button>
          )}
        </div>

        {!isClient && (
          <>
            <form onSubmit={handleSearch} className="flex gap-2">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-slate-400" />
                <input
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  placeholder="Buscar por número..."
                  className="w-full rounded-xl border border-slate-200 bg-white py-2 pl-8 pr-3 text-xs focus:border-blue-500 focus:outline-none dark:border-slate-700 dark:bg-slate-800"
                />
              </div>
              <button type="submit" className="rounded-xl border border-slate-200 bg-white px-3 text-xs font-medium text-slate-600 hover:bg-slate-50 dark:border-slate-700 dark:bg-slate-800">
                Buscar
              </button>
            </form>

            {/* Filter tabs */}
            <div className="flex gap-1.5">
              {(["", "AHORROS", "EMPRESARIAL", "CORRIENTE"] as const).map((tipo) => (
                <button
                  key={tipo}
                  onClick={() => setFilters((p) => ({ ...p, tipo: tipo || undefined, page: 0 }))}
                  className={`rounded-lg px-2.5 py-1 text-xs font-medium transition-colors ${
                    filters.tipo === tipo || (!filters.tipo && !tipo)
                      ? "bg-blue-600 text-white"
                      : "bg-slate-100 text-slate-600 hover:bg-slate-200 dark:bg-slate-800 dark:text-slate-300"
                  }`}
                >
                  {tipo || "Todas"}
                </button>
              ))}
            </div>
          </>
        )}

        {/* Account cards */}
        {isError ? (
          <div className="rounded-xl border border-red-200 bg-red-50 p-3 text-xs text-red-600">
            Error al cargar las cuentas.
          </div>
        ) : isLoading ? (
          <AccountCardsSkeleton />
        ) : (
          <div className="space-y-3">
            {accounts.map((account) => (
              <AccountCard
                key={account.id}
                account={account}
                selected={selectedAccount?.id === account.id}
                onClick={() => setSelectedAccount(account)}
              />
            ))}
            {accounts.length === 0 && (
              <div className="flex flex-col items-center justify-center py-12 text-center">
                <p className="text-sm text-slate-500">No se encontraron cuentas</p>
              </div>
            )}
          </div>
        )}
      </div>

      {/* ── Right: account detail ────────────────────────────── */}
      <div className="flex-1 overflow-y-auto p-6">
        {selectedAccount ? (
          <div className="space-y-6">
            {/* Account header */}
            <div className="flex items-start justify-between">
              <div>
                <h2 className="text-lg font-bold text-slate-900 dark:text-white">
                  {selectedAccount.numeroCuenta}
                </h2>
                <p className="mt-0.5 text-sm text-slate-500">{selectedAccount.nombreCliente}</p>
              </div>

              {/* Actions */}
              <div className="flex gap-2">
                {canFreezeAccounts && selectedAccount.estado === "ACTIVA" && (
                  <ActionButton label="Congelar" onClick={() => handleAction("freeze")}
                    className="border-blue-200 text-blue-600 hover:bg-blue-50" />
                )}
                {canFreezeAccounts && selectedAccount.estado === "CONGELADA" && (
                  <ActionButton label="Descongelar" onClick={() => handleAction("unfreeze")}
                    className="border-emerald-200 text-emerald-600 hover:bg-emerald-50" />
                )}
                {canCloseAccounts && selectedAccount.estado !== "CERRADA" && (
                  <ActionButton label="Cerrar cuenta" onClick={() => handleAction("close")}
                    className="border-red-200 text-red-500 hover:bg-red-50" />
                )}
              </div>
            </div>

            {/* Movements */}
            <div>
              <h3 className="mb-3 text-sm font-semibold text-slate-700 dark:text-slate-300">
                Historial de movimientos
              </h3>
              <AccountMovements
                accountId={selectedAccount.id}
                currency={selectedAccount.moneda}
              />
            </div>
          </div>
        ) : (
          <div className="flex h-full flex-col items-center justify-center text-center">
            <div className="rounded-2xl border-2 border-dashed border-slate-200 p-12 dark:border-slate-700">
              <p className="text-sm font-medium text-slate-500">Seleccioná una cuenta</p>
              <p className="mt-1 text-xs text-slate-400">para ver el historial de movimientos</p>
            </div>
          </div>
        )}
      </div>

      {/* Drawer */}
      <CreateAccountDrawer open={drawerOpen} onClose={() => setDrawerOpen(false)} />
    </div>
  );
}

function AccountCardsSkeleton() {
  return (
    <div className="space-y-3">
      {Array.from({ length: 3 }).map((_, i) => (
        <div key={i} className="h-36 animate-pulse rounded-2xl bg-slate-200 dark:bg-slate-700" />
      ))}
    </div>
  );
}

function ActionButton({ label, onClick, className }: { label: string; onClick: () => void; className: string }) {
  return (
    <button
      onClick={onClick}
      className={`rounded-lg border px-3 py-1.5 text-xs font-medium transition-colors ${className}`}
    >
      {label}
    </button>
  );
}
