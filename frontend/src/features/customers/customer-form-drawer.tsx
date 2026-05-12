"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Loader2, X } from "lucide-react";
import { cn } from "@/lib/utils";
import { useCreateCustomer, useUpdateCustomer } from "@/hooks/use-customers";
import type { Customer } from "@/types/customer.types";

const DOCUMENT_TYPES = ["DNI", "CE", "RUC", "PASAPORTE"] as const;

const schema = z.object({
  tipoDocumento: z.enum(DOCUMENT_TYPES, { required_error: "Requerido" }),
  numeroDocumento: z
    .string()
    .min(8, "Mínimo 8 dígitos")
    .max(12, "Máximo 12 dígitos")
    .regex(/^[0-9]+$/, "Solo números"),
  nombres: z.string().min(2, "Mínimo 2 caracteres").max(100),
  apellidos: z.string().min(2, "Mínimo 2 caracteres").max(100),
  email: z.string().email("Email inválido").or(z.literal("")).optional(),
  telefono: z
    .string()
    .regex(/^[+]?[0-9]{9,15}$/, "Formato inválido")
    .or(z.literal(""))
    .optional(),
  fechaNacimiento: z.string().optional(),
});

type FormData = z.infer<typeof schema>;

interface Props {
  open: boolean;
  onClose: () => void;
  customer?: Customer | null;
}

export function CustomerFormDrawer({ open, onClose, customer }: Props) {
  const isEdit = !!customer;

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) });

  const { mutate: create, isPending: isCreating } = useCreateCustomer(onClose);
  const { mutate: update, isPending: isUpdating } = useUpdateCustomer(onClose);
  const isPending = isCreating || isUpdating;

  useEffect(() => {
    if (open && customer) {
      reset({
        tipoDocumento: customer.tipoDocumento,
        numeroDocumento: customer.numeroDocumento,
        nombres: customer.nombres,
        apellidos: customer.apellidos,
        email: customer.email ?? "",
        telefono: customer.telefono ?? "",
        fechaNacimiento: customer.fechaNacimiento ?? "",
      });
    } else if (!open) {
      reset({});
    }
  }, [open, customer, reset]);

  const onSubmit = (data: FormData) => {
    const payload = {
      ...data,
      email: data.email || undefined,
      telefono: data.telefono || undefined,
      fechaNacimiento: data.fechaNacimiento || undefined,
    };
    if (isEdit) {
      update({ id: customer!.id, data: payload });
    } else {
      create(payload);
    }
  };

  return (
    <>
      {/* Backdrop */}
      <div
        className={cn(
          "fixed inset-0 z-40 bg-black/40 backdrop-blur-sm transition-opacity",
          open ? "opacity-100" : "pointer-events-none opacity-0",
        )}
        onClick={onClose}
      />

      {/* Drawer */}
      <aside
        className={cn(
          "fixed inset-y-0 right-0 z-50 flex w-full max-w-md flex-col bg-white shadow-2xl transition-transform duration-300 dark:bg-slate-900",
          open ? "translate-x-0" : "translate-x-full",
        )}
      >
        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4 dark:border-slate-800">
          <div>
            <h2 className="text-base font-semibold text-slate-900 dark:text-white">
              {isEdit ? "Editar cliente" : "Nuevo cliente"}
            </h2>
            <p className="text-xs text-slate-500">
              {isEdit ? `Editando: ${customer?.nombreCompleto}` : "Completá los datos del cliente"}
            </p>
          </div>
          <button
            onClick={onClose}
            className="rounded-lg p-1.5 text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-600 dark:hover:bg-slate-800"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-1 flex-col overflow-y-auto">
          <div className="space-y-5 px-6 py-6">
            {/* Documento */}
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="text-xs font-medium text-slate-600 dark:text-slate-400">
                  Tipo de documento <span className="text-red-500">*</span>
                </label>
                <select
                  {...register("tipoDocumento")}
                  className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2.5 text-sm text-slate-900 focus:border-blue-500 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 dark:border-slate-700 dark:bg-slate-800 dark:text-white"
                >
                  <option value="">Seleccionar</option>
                  {DOCUMENT_TYPES.map((t) => (
                    <option key={t} value={t}>{t}</option>
                  ))}
                </select>
                {errors.tipoDocumento && (
                  <p className="text-xs text-red-500">{errors.tipoDocumento.message}</p>
                )}
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-medium text-slate-600 dark:text-slate-400">
                  Número <span className="text-red-500">*</span>
                </label>
                <input
                  {...register("numeroDocumento")}
                  placeholder="12345678"
                  className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2.5 text-sm text-slate-900 focus:border-blue-500 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 dark:border-slate-700 dark:bg-slate-800 dark:text-white"
                />
                {errors.numeroDocumento && (
                  <p className="text-xs text-red-500">{errors.numeroDocumento.message}</p>
                )}
              </div>
            </div>

            {/* Nombres */}
            <div className="grid grid-cols-2 gap-4">
              <Field label="Nombres" required error={errors.nombres?.message}>
                <input
                  {...register("nombres")}
                  placeholder="Juan Carlos"
                  className={inputClass}
                />
              </Field>
              <Field label="Apellidos" required error={errors.apellidos?.message}>
                <input
                  {...register("apellidos")}
                  placeholder="Pérez García"
                  className={inputClass}
                />
              </Field>
            </div>

            {/* Contacto */}
            <Field label="Email" error={errors.email?.message}>
              <input
                {...register("email")}
                type="email"
                placeholder="juan@example.com"
                className={inputClass}
              />
            </Field>

            <Field label="Teléfono" error={errors.telefono?.message}>
              <input
                {...register("telefono")}
                placeholder="999888777"
                className={inputClass}
              />
            </Field>

            <Field label="Fecha de nacimiento" error={errors.fechaNacimiento?.message}>
              <input
                {...register("fechaNacimiento")}
                type="date"
                className={inputClass}
              />
            </Field>
          </div>

          {/* Footer */}
          <div className="mt-auto flex gap-3 border-t border-slate-200 px-6 py-4 dark:border-slate-800">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 rounded-lg border border-slate-200 bg-white py-2.5 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-50 dark:border-slate-700 dark:bg-transparent dark:text-slate-300"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={isPending}
              className="flex flex-1 items-center justify-center gap-2 rounded-lg bg-blue-600 py-2.5 text-sm font-medium text-white transition-colors hover:bg-blue-700 disabled:opacity-60"
            >
              {isPending && <Loader2 className="h-3.5 w-3.5 animate-spin" />}
              {isEdit ? "Guardar cambios" : "Crear cliente"}
            </button>
          </div>
        </form>
      </aside>
    </>
  );
}

// ─── Small helpers ────────────────────────────────────────────────────────────

const inputClass =
  "w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2.5 text-sm text-slate-900 focus:border-blue-500 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 dark:border-slate-700 dark:bg-slate-800 dark:text-white";

function Field({
  label,
  required,
  error,
  children,
}: {
  label: string;
  required?: boolean;
  error?: string;
  children: React.ReactNode;
}) {
  return (
    <div className="space-y-1.5">
      <label className="text-xs font-medium text-slate-600 dark:text-slate-400">
        {label} {required && <span className="text-red-500">*</span>}
      </label>
      {children}
      {error && <p className="text-xs text-red-500">{error}</p>}
    </div>
  );
}
