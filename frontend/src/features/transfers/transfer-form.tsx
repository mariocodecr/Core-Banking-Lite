"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Loader2, ArrowRight, AlertTriangle } from "lucide-react";
import { useCreateTransfer } from "@/hooks/use-transfers";
import { useAccounts } from "@/hooks/use-accounts";
import { formatCurrency } from "@/lib/utils";
import type { Account } from "@/types/account.types";

const schema = z
  .object({
    cuentaOrigenId:  z.string().uuid("Seleccioná una cuenta de origen"),
    cuentaDestinoId: z.string().uuid("Seleccioná una cuenta de destino"),
    monto:           z.coerce.number().min(0.01, "El monto mínimo es 0.01"),
    descripcion:     z.string().min(1, "La descripción es requerida").max(255),
  })
  .refine((d) => d.cuentaOrigenId !== d.cuentaDestinoId, {
    message: "Las cuentas de origen y destino deben ser distintas",
    path: ["cuentaDestinoId"],
  });

type FormData = z.infer<typeof schema>;

export function TransferForm({ onSuccess }: { onSuccess?: () => void }) {
  const { data: accountsPage } = useAccounts({ size: 100 });
  const activeAccounts = accountsPage?.content.filter((a) => a.estado === "ACTIVA") ?? [];

  const { mutate: create, isPending } = useCreateTransfer();

  const {
    register,
    handleSubmit,
    watch,
    reset,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) });

  const origenId  = watch("cuentaOrigenId");
  const destinoId = watch("cuentaDestinoId");

  const origenAccount  = activeAccounts.find((a) => a.id === origenId);
  const destinoAccount = activeAccounts.find((a) => a.id === destinoId);
  const currencyMismatch = !!(origenAccount && destinoAccount && origenAccount.moneda !== destinoAccount.moneda);

  const onSubmit = (data: FormData) => {
    create(
      { ...data, idempotencyKey: crypto.randomUUID() },
      { onSuccess: () => { reset(); onSuccess?.(); } },
    );
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
      {/* Origin account */}
      <div className="space-y-1.5">
        <label className="text-xs font-semibold uppercase tracking-wide text-slate-500">
          Cuenta origen
        </label>
        <select
          {...register("cuentaOrigenId")}
          className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-900 focus:border-blue-500 focus:outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-white"
        >
          <option value="">Seleccioná la cuenta de origen</option>
          {activeAccounts.map((a) => (
            <option key={a.id} value={a.id} disabled={a.id === destinoId}>
              {a.numeroCuenta} — {a.nombreCliente} ({formatCurrency(a.saldo, a.moneda)})
            </option>
          ))}
        </select>
        {errors.cuentaOrigenId && (
          <p className="text-xs text-red-500">{errors.cuentaOrigenId.message}</p>
        )}
        {origenAccount && (
          <AccountPreview account={origenAccount} label="Disponible" />
        )}
      </div>

      {/* Arrow */}
      <div className="flex items-center justify-center">
        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-blue-50 dark:bg-blue-950">
          <ArrowRight className="h-4 w-4 text-blue-600" />
        </div>
      </div>

      {/* Destination account */}
      <div className="space-y-1.5">
        <label className="text-xs font-semibold uppercase tracking-wide text-slate-500">
          Cuenta destino
        </label>
        <select
          {...register("cuentaDestinoId")}
          className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-900 focus:border-blue-500 focus:outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-white"
        >
          <option value="">Seleccioná la cuenta de destino</option>
          {activeAccounts.map((a) => (
            <option key={a.id} value={a.id} disabled={a.id === origenId}>
              {a.numeroCuenta} — {a.nombreCliente} ({a.moneda} {a.saldo.toLocaleString("es-CR", { minimumFractionDigits: 2 })})
            </option>
          ))}
        </select>
        {errors.cuentaDestinoId && (
          <p className="text-xs text-red-500">{errors.cuentaDestinoId.message}</p>
        )}
        {destinoAccount && (
          <AccountPreview account={destinoAccount} label="Saldo actual" />
        )}
        {currencyMismatch && (
          <div className="flex items-start gap-2 rounded-xl border border-red-200 bg-red-50 p-3 dark:border-red-800 dark:bg-red-900/20">
            <AlertTriangle className="mt-0.5 h-3.5 w-3.5 shrink-0 text-red-500" />
            <p className="text-xs text-red-600 dark:text-red-400">
              Las cuentas tienen monedas distintas ({origenAccount!.moneda} → {destinoAccount!.moneda}). No se permiten transferencias entre monedas diferentes.
            </p>
          </div>
        )}
      </div>

      {/* Amount */}
      <div className="space-y-1.5">
        <label className="text-xs font-semibold uppercase tracking-wide text-slate-500">
          Monto <span className="text-red-500">*</span>
        </label>
        <div className="relative">
          <span className="absolute left-3 top-1/2 -translate-y-1/2 text-sm font-medium text-slate-400">
            {origenAccount?.moneda ?? "USD"}
          </span>
          <input
            {...register("monto")}
            type="number"
            min="0.01"
            step="0.01"
            placeholder="0.00"
            className="w-full rounded-xl border border-slate-200 bg-white py-2.5 pl-12 pr-3 text-sm text-slate-900 focus:border-blue-500 focus:outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-white"
          />
        </div>
        {errors.monto && (
          <p className="text-xs text-red-500">{errors.monto.message}</p>
        )}
      </div>

      {/* Description */}
      <div className="space-y-1.5">
        <label className="text-xs font-semibold uppercase tracking-wide text-slate-500">
          Descripción <span className="text-red-500">*</span>
        </label>
        <input
          {...register("descripcion")}
          placeholder="Motivo de la transferencia"
          className="w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-900 focus:border-blue-500 focus:outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-white"
        />
        {errors.descripcion && (
          <p className="text-xs text-red-500">{errors.descripcion.message}</p>
        )}
      </div>

      <button
        type="submit"
        disabled={isPending || currencyMismatch}
        className="flex w-full items-center justify-center gap-2 rounded-xl bg-blue-600 py-3 text-sm font-semibold text-white transition-colors hover:bg-blue-700 disabled:opacity-60"
      >
        {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
        Confirmar transferencia
      </button>

      <p className="text-center text-[10px] text-slate-400">
        Límite diario: 50,000.00 · La transferencia es inmediata e irreversible
      </p>
    </form>
  );
}

function AccountPreview({ account, label }: { account: Account; label: string }) {
  return (
    <div className="flex items-center justify-between rounded-lg bg-slate-50 px-3 py-2 dark:bg-slate-800/60">
      <p className="text-xs text-slate-500">{label}</p>
      <p className="text-xs font-semibold text-slate-800 dark:text-white">
        {formatCurrency(account.saldo, account.moneda)}
      </p>
    </div>
  );
}
