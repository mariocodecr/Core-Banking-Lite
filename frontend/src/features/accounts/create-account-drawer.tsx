"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Loader2, X, Search, CheckCircle2, UserX } from "lucide-react";
import { useCreateAccount } from "@/hooks/use-accounts";
import { useCustomers } from "@/hooks/use-customers";
import type { AccountType } from "@/types/account.types";
import type { Customer } from "@/types/customer.types";

const ACCOUNT_TYPES: { value: AccountType; label: string }[] = [
  { value: "AHORROS",   label: "Cuenta de Ahorros" },
  { value: "CTS",       label: "CTS" },
  { value: "CORRIENTE", label: "Cuenta Corriente" },
];

const schema = z.object({
  customerId:   z.string().uuid("Buscá un cliente por documento primero"),
  tipo:         z.enum(["AHORROS", "CTS", "CORRIENTE"] as const),
  moneda:       z.string(),
  saldoInicial: z.coerce.number().min(0, "No puede ser negativo").optional(),
});

type FormData = z.infer<typeof schema>;

interface Props {
  open: boolean;
  onClose: () => void;
  preselectedCustomerId?: string;
}

export function CreateAccountDrawer({ open, onClose, preselectedCustomerId }: Props) {
  const { mutate: create, isPending } = useCreateAccount(onClose);
  const [docSearch, setDocSearch]   = useState("");
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      customerId: preselectedCustomerId ?? "",
      moneda: "USD",
      saldoInicial: 0,
    },
  });

  // Live search — only fires when ≥3 chars typed
  const { data: searchResult, isFetching } = useCustomers({
    numeroDocumento: docSearch.length >= 3 ? docSearch : undefined,
    size: 1,
  });

  const foundCustomer = docSearch.length >= 3 ? (searchResult?.content[0] ?? null) : null;

  const handleDocChange = (value: string) => {
    setDocSearch(value);
    setSelectedCustomer(null);
    setValue("customerId", "");
  };

  const handleSelectCustomer = (customer: Customer) => {
    setSelectedCustomer(customer);
    setValue("customerId", customer.id, { shouldValidate: true });
  };

  const onSubmit = (data: FormData) => create(data);

  const handleClose = () => {
    reset();
    setDocSearch("");
    setSelectedCustomer(null);
    onClose();
  };

  return (
    <>
      <div
        className={`fixed inset-0 z-40 bg-black/40 backdrop-blur-sm transition-opacity ${open ? "opacity-100" : "pointer-events-none opacity-0"}`}
        onClick={handleClose}
      />

      <aside
        className={`fixed inset-y-0 right-0 z-50 flex w-full max-w-md flex-col bg-white shadow-2xl transition-transform duration-300 dark:bg-slate-900 ${open ? "translate-x-0" : "translate-x-full"}`}
      >
        <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4 dark:border-slate-800">
          <div>
            <h2 className="text-base font-semibold text-slate-900 dark:text-white">Nueva cuenta bancaria</h2>
            <p className="text-xs text-slate-500">Completá los datos para abrir la cuenta</p>
          </div>
          <button onClick={handleClose} className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600">
            <X className="h-4 w-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-1 flex-col overflow-y-auto">
          <div className="space-y-5 px-6 py-6">

            {/* Customer search by document */}
            {!preselectedCustomerId && (
              <div className="space-y-2">
                <label className="text-xs font-medium text-slate-600">
                  Documento del cliente <span className="text-red-500">*</span>
                </label>

                <div className="relative">
                  <Search className="absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-slate-400" />
                  <input
                    value={docSearch}
                    onChange={(e) => handleDocChange(e.target.value)}
                    placeholder="DNI, Pasaporte, RUC, CE..."
                    className="w-full rounded-lg border border-slate-200 bg-slate-50 py-2.5 pl-9 pr-9 text-sm text-slate-900 focus:border-blue-500 focus:outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-white"
                  />
                  {isFetching && (
                    <Loader2 className="absolute right-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 animate-spin text-slate-400" />
                  )}
                </div>

                {/* Found — not yet selected */}
                {foundCustomer && !selectedCustomer && (
                  <button
                    type="button"
                    onClick={() => handleSelectCustomer(foundCustomer)}
                    className="flex w-full items-center gap-3 rounded-xl border border-slate-200 bg-white px-4 py-3 text-left transition-colors hover:border-blue-400 hover:bg-blue-50 dark:border-slate-700 dark:bg-slate-800"
                  >
                    <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-slate-100 text-xs font-bold text-slate-600 dark:bg-slate-700 dark:text-slate-300">
                      {foundCustomer.nombres[0]}{foundCustomer.apellidos[0]}
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-semibold text-slate-900 dark:text-white">
                        {foundCustomer.nombreCompleto}
                      </p>
                      <p className="text-xs text-slate-500">
                        {foundCustomer.tipoDocumento} · {foundCustomer.numeroDocumento}
                      </p>
                    </div>
                    <span className="text-xs font-medium text-blue-600">Seleccionar</span>
                  </button>
                )}

                {/* Selected customer confirmation */}
                {selectedCustomer && (
                  <div className="flex items-center gap-3 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 dark:border-emerald-800 dark:bg-emerald-900/20">
                    <CheckCircle2 className="h-4 w-4 shrink-0 text-emerald-600" />
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-semibold text-emerald-800 dark:text-emerald-300">
                        {selectedCustomer.nombreCompleto}
                      </p>
                      <p className="text-xs text-emerald-600 dark:text-emerald-400">
                        {selectedCustomer.tipoDocumento} · {selectedCustomer.numeroDocumento}
                      </p>
                    </div>
                    <button
                      type="button"
                      onClick={() => handleDocChange("")}
                      className="text-xs text-emerald-600 hover:text-emerald-800"
                    >
                      Cambiar
                    </button>
                  </div>
                )}

                {/* Not found */}
                {docSearch.length >= 3 && !isFetching && !foundCustomer && !selectedCustomer && (
                  <div className="flex items-center gap-2 rounded-lg border border-slate-200 bg-slate-50 px-3 py-2.5 dark:border-slate-700 dark:bg-slate-800">
                    <UserX className="h-4 w-4 shrink-0 text-slate-400" />
                    <p className="text-xs text-slate-500">
                      No se encontró ningún cliente con ese documento
                    </p>
                  </div>
                )}

                {errors.customerId && (
                  <p className="text-xs text-red-500">{errors.customerId.message}</p>
                )}
              </div>
            )}

            {/* Account type */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slate-600">
                Tipo de cuenta <span className="text-red-500">*</span>
              </label>
              <div className="grid grid-cols-3 gap-2">
                {ACCOUNT_TYPES.map(({ value, label }) => (
                  <label key={value} className="cursor-pointer">
                    <input {...register("tipo")} type="radio" value={value} className="peer sr-only" />
                    <div className="rounded-xl border-2 border-slate-200 p-3 text-center text-xs font-medium text-slate-600 transition-all peer-checked:border-blue-500 peer-checked:bg-blue-50 peer-checked:text-blue-700">
                      {label}
                    </div>
                  </label>
                ))}
              </div>
              {errors.tipo && <p className="text-xs text-red-500">{errors.tipo.message}</p>}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="text-xs font-medium text-slate-600">Moneda</label>
                <select
                  {...register("moneda")}
                  className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2.5 text-sm focus:border-blue-500 focus:outline-none"
                >
                  <option value="USD">USD — Dólar</option>
                  <option value="CRC">CRC — Colón</option>
                  <option value="EUR">EUR — Euro</option>
                </select>
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-medium text-slate-600">Depósito inicial</label>
                <input
                  {...register("saldoInicial")}
                  type="number"
                  min="0"
                  step="0.01"
                  placeholder="0.00"
                  className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2.5 text-sm focus:border-blue-500 focus:outline-none"
                />
                {errors.saldoInicial && <p className="text-xs text-red-500">{errors.saldoInicial.message}</p>}
              </div>
            </div>
          </div>

          <div className="mt-auto flex gap-3 border-t border-slate-200 px-6 py-4 dark:border-slate-800">
            <button
              type="button"
              onClick={handleClose}
              className="flex-1 rounded-lg border border-slate-200 bg-white py-2.5 text-sm font-medium text-slate-700 hover:bg-slate-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={isPending}
              className="flex flex-1 items-center justify-center gap-2 rounded-lg bg-blue-600 py-2.5 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-60"
            >
              {isPending && <Loader2 className="h-3.5 w-3.5 animate-spin" />}
              Abrir cuenta
            </button>
          </div>
        </form>
      </aside>
    </>
  );
}
