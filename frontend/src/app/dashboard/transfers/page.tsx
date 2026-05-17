"use client";

import { ArrowLeftRight, History, Eye } from "lucide-react";
import { TransferForm } from "@/features/transfers/transfer-form";
import { TransferHistory } from "@/features/transfers/transfer-history";
import { usePermissions } from "@/hooks/use-current-user";

export default function TransfersPage() {
  const { canCreateTransfers } = usePermissions();

  return (
    <div className="flex h-full flex-col gap-0 lg:flex-row">
      {/* ── Left: transfer form (hidden for AUDITOR) ─────────── */}
      {canCreateTransfers ? (
        <div className="flex w-full flex-col gap-6 border-r border-slate-200 p-6 dark:border-slate-800 lg:w-[420px] lg:overflow-y-auto">
          <div>
            <div className="mb-1 flex items-center gap-2">
              <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-blue-600">
                <ArrowLeftRight className="h-3.5 w-3.5 text-white" />
              </div>
              <h1 className="text-xl font-bold text-slate-900 dark:text-white">Nueva transferencia</h1>
            </div>
            <p className="text-xs text-slate-500">
              Transferí fondos entre cuentas activas del sistema
            </p>
          </div>

          <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
            <TransferForm />
          </div>
        </div>
      ) : (
        <div className="flex w-full flex-col gap-3 border-r border-slate-200 p-6 dark:border-slate-800 lg:w-[280px]">
          <div className="flex items-center gap-2">
            <Eye className="h-4 w-4 text-amber-500" />
            <h1 className="text-base font-bold text-slate-900 dark:text-white">Transferencias</h1>
          </div>
          <div className="rounded-xl border border-amber-200 bg-amber-50 p-3 dark:border-amber-800 dark:bg-amber-900/20">
            <p className="text-xs text-amber-700 dark:text-amber-400">
              Tu rol de Auditor tiene acceso de solo lectura. No podés iniciar transferencias.
            </p>
          </div>
        </div>
      )}

      {/* ── Right: transfer history ───────────────────────────── */}
      <div className="flex-1 overflow-y-auto p-6">
        <div className="mb-4 flex items-center gap-2">
          <History className="h-4 w-4 text-slate-400" />
          <h2 className="text-sm font-semibold text-slate-700 dark:text-slate-300">
            Historial de transferencias
          </h2>
        </div>
        <TransferHistory />
      </div>
    </div>
  );
}
