"use client";

import { useState } from "react";
import { cn, formatCurrency } from "@/lib/utils";
import type { Instrument, InstrumentType } from "@/types/investment.types";

interface Props {
  instruments: Instrument[];
  selected:    string | null;
  onSelect:    (symbol: string) => void;
}

const TYPE_TABS: { label: string; value: InstrumentType | "ALL" }[] = [
  { label: "Todos",          value: "ALL" },
  { label: "ETFs",           value: "ETF" },
  { label: "Fondos indexados", value: "MUTUAL_FUND" },
];

export function InstrumentList({ instruments, selected, onSelect }: Props) {
  const [typeFilter, setTypeFilter] = useState<InstrumentType | "ALL">("ALL");

  const visible = typeFilter === "ALL"
    ? instruments
    : instruments.filter((i) => i.instrumentType === typeFilter);

  return (
    <div className="space-y-2">
      {/* Type filter tabs */}
      <div className="flex gap-1">
        {TYPE_TABS.map((tab) => (
          <button
            key={tab.value}
            onClick={() => setTypeFilter(tab.value)}
            className={cn(
              "rounded-lg px-2.5 py-1 text-[10px] font-semibold transition-colors",
              typeFilter === tab.value
                ? "bg-blue-600 text-white"
                : "bg-slate-100 text-slate-600 hover:bg-slate-200 dark:bg-slate-800 dark:text-slate-300",
            )}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Instrument rows */}
      <div className="space-y-0.5">
        {visible.map((inst) => (
          <button
            key={inst.symbol}
            onClick={() => onSelect(inst.symbol)}
            className={cn(
              "flex w-full items-center justify-between rounded-xl px-3 py-2.5 text-left transition-colors",
              selected === inst.symbol
                ? "bg-blue-50 ring-1 ring-blue-200 dark:bg-blue-950 dark:ring-blue-800"
                : "hover:bg-slate-100 dark:hover:bg-slate-800",
            )}
          >
            <div className="min-w-0">
              <div className="flex items-center gap-1.5">
                <p className="text-sm font-semibold text-slate-900 dark:text-white">{inst.symbol}</p>
                <TypeBadge type={inst.instrumentType} />
              </div>
              <p className="truncate text-[10px] text-slate-500 leading-tight">{inst.name}</p>
            </div>
            <div className="ml-2 shrink-0 text-right">
              {inst.lastPrice != null ? (
                <p className="text-sm font-semibold text-slate-900 dark:text-white">
                  {formatCurrency(inst.lastPrice, "USD")}
                </p>
              ) : (
                <p className="text-xs text-slate-400">—</p>
              )}
            </div>
          </button>
        ))}
      </div>
    </div>
  );
}

function TypeBadge({ type }: { type: InstrumentType }) {
  return type === "MUTUAL_FUND" ? (
    <span className="rounded-full bg-violet-100 px-1.5 py-0.5 text-[9px] font-semibold text-violet-700 dark:bg-violet-900/40 dark:text-violet-400">
      FONDO
    </span>
  ) : (
    <span className="rounded-full bg-blue-100 px-1.5 py-0.5 text-[9px] font-semibold text-blue-600 dark:bg-blue-900/40 dark:text-blue-400">
      ETF
    </span>
  );
}
