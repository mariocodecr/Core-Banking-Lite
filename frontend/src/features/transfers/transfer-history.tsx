"use client";

import { CheckCircle2, XCircle, ArrowRight } from "lucide-react";
import { cn, formatCurrency, formatDateTime } from "@/lib/utils";
import { useTransfers } from "@/hooks/use-transfers";
import type { Transfer } from "@/types/transfer.types";

export function TransferHistory() {
  const { data, isLoading } = useTransfers({ size: 50 });

  if (isLoading) {
    return (
      <div className="space-y-2">
        {Array.from({ length: 5 }).map((_, i) => (
          <div key={i} className="h-16 animate-pulse rounded-xl bg-slate-100 dark:bg-slate-800" />
        ))}
      </div>
    );
  }

  if (!data?.content.length) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-center">
        <ArrowRight className="mb-3 h-8 w-8 text-slate-300" />
        <p className="text-sm font-medium text-slate-500">Sin transferencias registradas</p>
        <p className="mt-1 text-xs text-slate-400">Las transferencias aparecerán aquí</p>
      </div>
    );
  }

  return (
    <div className="space-y-2">
      {data.content.map((t) => (
        <TransferRow key={t.id} transfer={t} />
      ))}
    </div>
  );
}

function TransferRow({ transfer: t }: { transfer: Transfer }) {
  const isOk = t.estado === "COMPLETADA";

  return (
    <div className="flex items-center gap-3 rounded-xl bg-slate-50 px-4 py-3 dark:bg-slate-800/50">
      <div
        className={cn(
          "flex h-8 w-8 shrink-0 items-center justify-center rounded-full",
          isOk ? "bg-emerald-50 text-emerald-600" : "bg-red-50 text-red-500",
        )}
      >
        {isOk ? (
          <CheckCircle2 className="h-4 w-4" />
        ) : (
          <XCircle className="h-4 w-4" />
        )}
      </div>

      <div className="min-w-0 flex-1">
        <div className="flex items-center gap-1.5">
          <span className="truncate font-mono text-xs font-medium text-slate-700 dark:text-slate-200">
            {t.numeroCuentaOrigen}
          </span>
          <ArrowRight className="h-3 w-3 shrink-0 text-slate-400" />
          <span className="truncate font-mono text-xs font-medium text-slate-700 dark:text-slate-200">
            {t.numeroCuentaDestino}
          </span>
        </div>
        <p className="mt-0.5 truncate text-xs text-slate-400">{t.descripcion}</p>
        <p className="mt-0.5 text-[10px] text-slate-400">
          {formatDateTime(t.fechaTransferencia)} · Ref: {t.referencia}
        </p>
      </div>

      <div className="text-right">
        <p className={cn("text-sm font-bold", isOk ? "text-slate-800 dark:text-white" : "text-red-500 line-through")}>
          {formatCurrency(t.monto, t.moneda)}
        </p>
        <span
          className={cn(
            "inline-block rounded-full px-2 py-0.5 text-[10px] font-semibold",
            isOk
              ? "bg-emerald-100 text-emerald-700 dark:bg-emerald-900/40 dark:text-emerald-400"
              : "bg-red-100 text-red-600 dark:bg-red-900/40 dark:text-red-400",
          )}
        >
          {isOk ? "Completada" : "Fallida"}
        </span>
      </div>
    </div>
  );
}
