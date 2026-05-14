"use client";

import { useState } from "react";
import { Loader2 } from "lucide-react";
import { cn, formatCurrency } from "@/lib/utils";
import { useBuyOrder, useSellOrder } from "@/hooks/use-investments";
import type { Instrument } from "@/types/investment.types";

interface Props {
  instrument: Instrument;
  accountId:  string;
}

type Side = "BUY" | "SELL";

export function OrderForm({ instrument, accountId }: Props) {
  const [side, setSide]     = useState<Side>("BUY");
  const [shares, setShares] = useState("");

  const sharesNum   = parseFloat(shares) || 0;
  const totalCost   = instrument.lastPrice != null ? sharesNum * instrument.lastPrice : null;
  const isValidForm = sharesNum > 0;

  const { mutate: buy,  isPending: isBuying  } = useBuyOrder(() => setShares(""));
  const { mutate: sell, isPending: isSelling } = useSellOrder(() => setShares(""));
  const isPending = isBuying || isSelling;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!isValidForm) return;
    const payload = { accountId, symbol: instrument.symbol, shares: sharesNum };
    if (side === "BUY") buy(payload);
    else               sell(payload);
  };

  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-4 dark:border-slate-700 dark:bg-slate-900">
      <div className="mb-3 flex items-center justify-between">
        <div>
          <p className="text-sm font-bold text-slate-900 dark:text-white">{instrument.symbol}</p>
          <p className="text-[10px] text-slate-500">{instrument.name}</p>
        </div>
        {instrument.lastPrice != null && (
          <p className="text-sm font-semibold text-slate-700 dark:text-slate-300">
            {formatCurrency(instrument.lastPrice, "USD")}
          </p>
        )}
      </div>

      {/* Side toggle */}
      <div className="mb-3 flex rounded-xl overflow-hidden border border-slate-200 dark:border-slate-700">
        {(["BUY", "SELL"] as Side[]).map((s) => (
          <button
            key={s}
            type="button"
            onClick={() => setSide(s)}
            className={cn(
              "flex-1 py-1.5 text-xs font-semibold transition-colors",
              side === s
                ? s === "BUY"
                  ? "bg-emerald-500 text-white"
                  : "bg-red-500 text-white"
                : "bg-white text-slate-500 hover:bg-slate-50 dark:bg-slate-900 dark:text-slate-400",
            )}
          >
            {s === "BUY" ? "Comprar" : "Vender"}
          </button>
        ))}
      </div>

      <form onSubmit={handleSubmit} className="space-y-3">
        <div>
          <label className="mb-1 block text-[10px] font-medium text-slate-500">
            Cantidad de acciones
          </label>
          <input
            type="number"
            min="0.001"
            step="0.001"
            value={shares}
            onChange={(e) => setShares(e.target.value)}
            placeholder="ej. 0.5"
            className="w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-white"
          />
        </div>

        {totalCost != null && sharesNum > 0 && (
          <div className="rounded-lg bg-slate-50 px-3 py-2 dark:bg-slate-800">
            <p className="text-[10px] text-slate-500">Total estimado</p>
            <p className="text-sm font-bold text-slate-900 dark:text-white">
              {formatCurrency(totalCost, "USD")}
            </p>
          </div>
        )}

        <button
          type="submit"
          disabled={!isValidForm || isPending}
          className={cn(
            "flex w-full items-center justify-center gap-2 rounded-xl py-2.5 text-sm font-semibold text-white transition-colors disabled:opacity-50",
            side === "BUY" ? "bg-emerald-500 hover:bg-emerald-600" : "bg-red-500 hover:bg-red-600",
          )}
        >
          {isPending && <Loader2 className="h-3.5 w-3.5 animate-spin" />}
          {side === "BUY" ? "Ejecutar compra" : "Ejecutar venta"}
        </button>
      </form>
    </div>
  );
}
