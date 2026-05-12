"use client";

import { ArrowDownLeft, ArrowUpRight, RefreshCw } from "lucide-react";
import { cn, formatCurrency, formatDateTime } from "@/lib/utils";
import { useAccountMovements } from "@/hooks/use-accounts";
import type { AccountMovement, MovementType } from "@/types/account.types";

const MOVEMENT_CONFIG: Record<MovementType, { icon: React.ReactNode; color: string; sign: string }> = {
  DEPOSITO:              { icon: <ArrowDownLeft className="h-3.5 w-3.5" />, color: "text-emerald-600 bg-emerald-50", sign: "+" },
  RETIRO:                { icon: <ArrowUpRight  className="h-3.5 w-3.5" />, color: "text-red-500 bg-red-50",         sign: "-" },
  TRANSFERENCIA_ENTRADA: { icon: <ArrowDownLeft className="h-3.5 w-3.5" />, color: "text-blue-600 bg-blue-50",       sign: "+" },
  TRANSFERENCIA_SALIDA:  { icon: <ArrowUpRight  className="h-3.5 w-3.5" />, color: "text-orange-500 bg-orange-50",   sign: "-" },
};

const CREDIT_TYPES: MovementType[] = ["DEPOSITO", "TRANSFERENCIA_ENTRADA"];

interface Props {
  accountId: string;
  currency?: string;
}

export function AccountMovements({ accountId, currency = "PEN" }: Props) {
  const { data, isLoading } = useAccountMovements(accountId);

  if (isLoading) {
    return (
      <div className="space-y-2">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="flex animate-pulse items-center gap-3 rounded-xl bg-slate-50 p-3">
            <div className="h-8 w-8 rounded-full bg-slate-200" />
            <div className="flex-1 space-y-1">
              <div className="h-3 w-1/2 rounded bg-slate-200" />
              <div className="h-2 w-1/4 rounded bg-slate-100" />
            </div>
            <div className="h-3 w-16 rounded bg-slate-200" />
          </div>
        ))}
      </div>
    );
  }

  if (!data?.content.length) {
    return (
      <div className="flex flex-col items-center justify-center py-10 text-center">
        <RefreshCw className="mb-2 h-6 w-6 text-slate-300" />
        <p className="text-sm text-slate-500">Sin movimientos registrados</p>
      </div>
    );
  }

  return (
    <div className="space-y-1.5">
      {data.content.map((m) => (
        <MovementRow key={m.id} movement={m} currency={currency} />
      ))}
    </div>
  );
}

function MovementRow({ movement, currency }: { movement: AccountMovement; currency: string }) {
  const config = MOVEMENT_CONFIG[movement.tipo];
  const isCredit = CREDIT_TYPES.includes(movement.tipo);

  return (
    <div className="flex items-center gap-3 rounded-xl bg-slate-50 px-3 py-2.5 dark:bg-slate-800/50">
      <div className={cn("flex h-8 w-8 shrink-0 items-center justify-center rounded-full", config.color)}>
        {config.icon}
      </div>

      <div className="min-w-0 flex-1">
        <p className="truncate text-sm font-medium text-slate-800 dark:text-slate-200">
          {movement.descripcion ?? movement.tipo}
        </p>
        <p className="text-xs text-slate-400">{formatDateTime(movement.fechaMovimiento)}</p>
      </div>

      <div className="text-right">
        <p className={cn("text-sm font-semibold", isCredit ? "text-emerald-600" : "text-red-500")}>
          {config.sign}{formatCurrency(movement.monto, currency)}
        </p>
        <p className="text-xs text-slate-400">
          Saldo: {formatCurrency(movement.saldoPosterior, currency)}
        </p>
      </div>
    </div>
  );
}
