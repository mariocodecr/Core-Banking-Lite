"use client";

import { RefreshCw, TrendingUp, AlertCircle } from "lucide-react";
import { useExchangeRate } from "@/hooks/use-exchange-rate";

function RateRow({
  flag,
  label,
  rate,
  isLoading,
}: {
  flag: string;
  label: string;
  rate: number | undefined;
  isLoading: boolean;
}) {
  if (isLoading) {
    return (
      <div className="flex items-center justify-between rounded-xl bg-slate-50 px-4 py-3 dark:bg-slate-800/60">
        <div className="flex items-center gap-3">
          <span className="text-2xl">{flag}</span>
          <div className="h-3 w-16 animate-pulse rounded bg-slate-200 dark:bg-slate-700" />
        </div>
        <div className="h-5 w-28 animate-pulse rounded bg-slate-200 dark:bg-slate-700" />
      </div>
    );
  }

  const formatted = rate
    ? rate.toLocaleString("es-CR", { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    : "—";

  return (
    <div className="flex items-center justify-between rounded-xl bg-slate-50 px-4 py-3 dark:bg-slate-800/60">
      <div className="flex items-center gap-3">
        <span className="text-2xl">{flag}</span>
        <div>
          <p className="text-xs font-semibold text-slate-700 dark:text-slate-200">{label}</p>
          <p className="text-[10px] text-slate-400">venta · BCCR</p>
        </div>
      </div>
      <div className="text-right">
        <p className="text-sm font-bold text-slate-900 dark:text-white">
          ₡ {formatted}
        </p>
        <p className="text-[10px] text-slate-400">por 1 {label.split(" ")[0]}</p>
      </div>
    </div>
  );
}

export function ExchangeRateCard() {
  const { data: usdRate, isLoading: loadingUsd, isError: errorUsd, dataUpdatedAt: usdUpdated } = useExchangeRate("USD", "CRC");
  const { data: eurRate, isLoading: loadingEur, isError: errorEur } = useExchangeRate("EUR", "CRC");

  const hasError = errorUsd || errorEur;

  const updatedAt = usdUpdated
    ? new Date(usdUpdated).toLocaleTimeString("es-CR", { hour: "2-digit", minute: "2-digit" })
    : null;

  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900">
      {/* Header */}
      <div className="mb-4 flex items-start justify-between">
        <div>
          <div className="flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-emerald-600">
              <TrendingUp className="h-4 w-4 text-white" />
            </div>
            <p className="text-sm font-semibold text-slate-700 dark:text-slate-200">
              Tipo de cambio
            </p>
          </div>
          <p className="mt-1 text-xs text-slate-400">Banco Central de Costa Rica</p>
        </div>
        {updatedAt && (
          <div className="flex items-center gap-1 text-[10px] text-slate-400">
            <RefreshCw className="h-3 w-3" />
            <span>{updatedAt}</span>
          </div>
        )}
      </div>

      {/* Error state */}
      {hasError && (
        <div className="mb-3 flex items-start gap-2 rounded-xl border border-amber-200 bg-amber-50 px-3 py-2.5 dark:border-amber-800 dark:bg-amber-900/20">
          <AlertCircle className="mt-0.5 h-3.5 w-3.5 shrink-0 text-amber-500" />
          <p className="text-xs text-amber-700 dark:text-amber-300">
            No se pudo conectar con BCCR. Verificá que el backend tenga las variables <code className="font-mono font-bold">BCCR_EMAIL</code> y <code className="font-mono font-bold">BCCR_TOKEN</code> configuradas.
          </p>
        </div>
      )}

      {/* Rates */}
      <div className="space-y-2">
        <RateRow
          flag="🇺🇸"
          label="USD Dólar"
          rate={usdRate?.rate}
          isLoading={loadingUsd}
        />
        <RateRow
          flag="🇪🇺"
          label="EUR Euro"
          rate={eurRate?.rate}
          isLoading={loadingEur}
        />
      </div>

      <p className="mt-3 text-center text-[10px] text-slate-400">
        Caché de 4 horas · Tasa de venta
      </p>
    </div>
  );
}
