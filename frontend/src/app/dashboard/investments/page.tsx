"use client";

import { useState } from "react";
import { TrendingUp } from "lucide-react";
import { useInstruments, useCombinedPortfolio } from "@/hooks/use-investments";
import { useMyAccounts } from "@/hooks/use-accounts";
import { InstrumentList } from "@/features/investments/instrument-list";
import { OrderForm } from "@/features/investments/order-form";
import { PortfolioPositions } from "@/features/investments/portfolio-positions";
import { OrderHistory } from "@/features/investments/order-history";

export default function InvestmentsPage() {
  const [selectedAccountId, setSelectedAccountId] = useState<string>("");
  const [selectedSymbol,    setSelectedSymbol]    = useState<string | null>(null);

  const { data: myAccounts } = useMyAccounts();
  const usdAccounts = myAccounts?.filter((a) => a.moneda === "USD" && a.estado === "ACTIVA") ?? [];

  const { data: instruments, isLoading: loadingInstruments } = useInstruments();
  const { data: combinedPortfolio, isLoading: loadingPortfolio } = useCombinedPortfolio();

  const selectedInstrument = instruments?.find((i) => i.symbol === selectedSymbol) ?? null;
  const hasPositions = (combinedPortfolio?.positions?.length ?? 0) > 0;

  return (
    <div className="flex h-full flex-col gap-0 lg:flex-row">

      {/* ── Left: instruments + order form ──────────────────── */}
      <div className="flex w-full flex-col gap-4 border-r border-slate-200 p-6 dark:border-slate-800 lg:w-[380px] lg:overflow-y-auto">
        <div>
          <h1 className="text-xl font-bold text-slate-900 dark:text-white">Inversiones</h1>
          <p className="mt-0.5 text-xs text-slate-500">ETFs y fondos indexados (Alpha Vantage)</p>
        </div>

        {/* Instruments */}
        <div>
          <p className="mb-2 text-[10px] font-semibold uppercase tracking-wide text-slate-400">
            Instrumentos disponibles
          </p>
          {loadingInstruments ? (
            <div className="space-y-2">
              {Array.from({ length: 5 }).map((_, i) => (
                <div key={i} className="h-12 animate-pulse rounded-xl bg-slate-200 dark:bg-slate-700" />
              ))}
            </div>
          ) : (
            <InstrumentList
              instruments={instruments ?? []}
              selected={selectedSymbol}
              onSelect={setSelectedSymbol}
            />
          )}
        </div>

        {/* Order form — account selector only appears when user wants to operate */}
        {selectedInstrument && (
          <div className="space-y-3">
            <div>
              <label className="mb-1 block text-[10px] font-semibold uppercase tracking-wide text-slate-400">
                Cuenta USD para operar
              </label>
              {usdAccounts.length === 0 ? (
                <div className="rounded-xl border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700 dark:border-amber-900 dark:bg-amber-950 dark:text-amber-400">
                  No hay cuentas activas en USD. Creá una desde Cuentas.
                </div>
              ) : (
                <select
                  value={selectedAccountId}
                  onChange={(e) => setSelectedAccountId(e.target.value)}
                  className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs focus:border-blue-500 focus:outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-white"
                >
                  <option value="">Seleccioná una cuenta...</option>
                  {usdAccounts.map((a) => (
                    <option key={a.id} value={a.id}>
                      {a.numeroCuenta} — ${a.saldo.toLocaleString("en-US", { minimumFractionDigits: 2 })}
                    </option>
                  ))}
                </select>
              )}
            </div>

            {selectedAccountId && (
              <OrderForm instrument={selectedInstrument} accountId={selectedAccountId} />
            )}
          </div>
        )}
      </div>

      {/* ── Right: combined portfolio + history ────────────────────── */}
      <div className="flex-1 overflow-y-auto p-6">
        <div className="space-y-6">
          <div>
            <h2 className="text-sm font-bold text-slate-900 dark:text-white">Mi Portfolio</h2>
            <p className="mt-0.5 text-xs text-slate-500">Posiciones abiertas y rendimiento</p>
          </div>

          {loadingPortfolio ? (
            <div className="space-y-2">
              {Array.from({ length: 3 }).map((_, i) => (
                <div key={i} className="h-12 animate-pulse rounded-xl bg-slate-200 dark:bg-slate-700" />
              ))}
            </div>
          ) : hasPositions ? (
            <PortfolioPositions portfolio={combinedPortfolio!} />
          ) : (
            <div className="flex flex-col items-center justify-center rounded-2xl border-2 border-dashed border-slate-200 py-12 text-center dark:border-slate-700">
              <TrendingUp className="mb-2 h-8 w-8 text-slate-300" />
              <p className="text-sm font-medium text-slate-500">Sin posiciones aún</p>
              <p className="mt-1 text-xs text-slate-400">Seleccioná un instrumento y ejecutá tu primera compra</p>
            </div>
          )}

          {selectedAccountId && (
            <div>
              <h2 className="mb-3 text-sm font-bold text-slate-900 dark:text-white">
                Historial de órdenes
              </h2>
              <OrderHistory accountId={selectedAccountId} />
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
