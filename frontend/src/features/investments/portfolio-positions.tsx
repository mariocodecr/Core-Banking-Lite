"use client";

import { TrendingUp, TrendingDown } from "lucide-react";
import { cn, formatCurrency, formatShares } from "@/lib/utils";
import type { Portfolio } from "@/types/investment.types";

interface Props {
  portfolio: Portfolio;
}

export function PortfolioPositions({ portfolio }: Props) {
  const { totalInvested, totalCurrentValue, totalPnl, totalPnlPercent, positions } = portfolio;
  const isPositive = totalPnl >= 0;

  return (
    <div className="space-y-4">
      {/* KPI row */}
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <KpiCard label="Invertido"     value={formatCurrency(totalInvested,     "USD")} />
        <KpiCard label="Valor actual"  value={formatCurrency(totalCurrentValue, "USD")} />
        <KpiCard
          label="P&L ($)"
          value={formatCurrency(totalPnl, "USD")}
          positive={isPositive}
          signed
        />
        <KpiCard
          label="P&L (%)"
          value={`${totalPnlPercent >= 0 ? "+" : ""}${totalPnlPercent.toFixed(2)}%`}
          positive={isPositive}
        />
      </div>

      {/* Positions table */}
      {positions.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-2xl border-2 border-dashed border-slate-200 py-10 text-center dark:border-slate-700">
          <p className="text-sm font-medium text-slate-500">No hay posiciones abiertas</p>
          <p className="mt-1 text-xs text-slate-400">Comprá tu primer ETF para empezar</p>
        </div>
      ) : (
        <div className="overflow-x-auto rounded-2xl border border-slate-200 dark:border-slate-700">
          <table className="w-full text-xs">
            <thead>
              <tr className="border-b border-slate-200 bg-slate-50 dark:border-slate-700 dark:bg-slate-800/50">
                {["Símbolo", "Acciones", "Costo prom.", "Precio actual", "Valor", "P&L"].map((h) => (
                  <th key={h} className="px-4 py-2.5 text-left font-semibold text-slate-500">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {positions.map((pos) => {
                const positive = pos.pnl >= 0;
                return (
                  <tr
                    key={pos.symbol}
                    className="border-b border-slate-100 last:border-0 dark:border-slate-800"
                  >
                    <td className="px-4 py-3">
                      <p className="font-bold text-slate-900 dark:text-white">{pos.symbol}</p>
                      <p className="text-[10px] text-slate-400 leading-tight">{pos.instrumentName}</p>
                    </td>
                    <td className="px-4 py-3 tabular-nums text-slate-700 dark:text-slate-300">
                      {formatShares(pos.shares)}
                    </td>
                    <td className="px-4 py-3 tabular-nums text-slate-700 dark:text-slate-300">
                      {formatCurrency(pos.avgCost, "USD")}
                    </td>
                    <td className="px-4 py-3 tabular-nums text-slate-700 dark:text-slate-300">
                      {formatCurrency(pos.currentPrice, "USD")}
                    </td>
                    <td className="px-4 py-3 font-semibold tabular-nums text-slate-900 dark:text-white">
                      {formatCurrency(pos.currentValue, "USD")}
                    </td>
                    <td className="px-4 py-3">
                      <div className={cn("flex items-center gap-1 font-semibold tabular-nums", positive ? "text-emerald-600" : "text-red-500")}>
                        {positive
                          ? <TrendingUp className="h-3 w-3 shrink-0" />
                          : <TrendingDown className="h-3 w-3 shrink-0" />}
                        <span>{formatCurrency(pos.pnl, "USD")}</span>
                        <span className="text-[10px] font-normal opacity-70">
                          ({pos.pnlPercent >= 0 ? "+" : ""}{pos.pnlPercent.toFixed(2)}%)
                        </span>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

function KpiCard({
  label,
  value,
  positive,
  signed,
}: {
  label: string;
  value: string;
  positive?: boolean;
  signed?: boolean;
}) {
  const colorClass =
    positive === undefined
      ? "text-slate-900 dark:text-white"
      : positive
      ? "text-emerald-600"
      : "text-red-500";

  return (
    <div className="rounded-xl border border-slate-200 bg-white p-3 dark:border-slate-700 dark:bg-slate-900">
      <p className="text-[10px] font-medium text-slate-500">{label}</p>
      <p className={cn("mt-0.5 text-sm font-bold tabular-nums", colorClass)}>{value}</p>
    </div>
  );
}
