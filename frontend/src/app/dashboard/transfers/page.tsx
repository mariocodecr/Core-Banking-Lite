"use client";

import { ArrowLeftRight, History } from "lucide-react";
import { TransferForm } from "@/features/transfers/transfer-form";
import { TransferHistory } from "@/features/transfers/transfer-history";

export default function TransfersPage() {
  return (
    <div className="flex h-full flex-col gap-0 lg:flex-row">
      {/* ── Left: transfer form ───────────────────────────────── */}
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
