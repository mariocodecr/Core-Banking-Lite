"use client";

import { Pencil, Trash2 } from "lucide-react";
import { cn, formatDate } from "@/lib/utils";
import type { Customer } from "@/types/customer.types";
import { useDeleteCustomer } from "@/hooks/use-customers";

const DOC_LABEL: Record<string, string> = {
  DNI: "DNI",
  CE: "C.E.",
  RUC: "RUC",
  PASAPORTE: "PAS",
};

interface Props {
  customers: Customer[];
  onEdit: (customer: Customer) => void;
}

export function CustomerTable({ customers, onEdit }: Props) {
  const { mutate: deleteCustomer } = useDeleteCustomer();

  const handleDelete = (customer: Customer) => {
    if (confirm(`¿Eliminár a ${customer.nombreCompleto}? Esta acción no se puede deshacer.`)) {
      deleteCustomer(customer.id);
    }
  };

  if (customers.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center rounded-xl border border-dashed border-slate-200 py-16 text-center dark:border-slate-700">
        <p className="text-sm font-medium text-slate-500">No se encontraron clientes</p>
        <p className="mt-1 text-xs text-slate-400">Cambiá los filtros o creá un nuevo cliente</p>
      </div>
    );
  }

  return (
    <div className="overflow-hidden rounded-xl border border-slate-200 dark:border-slate-800">
      <table className="w-full text-sm">
        <thead>
          <tr className="border-b border-slate-200 bg-slate-50 dark:border-slate-800 dark:bg-slate-900/60">
            {["Documento", "Cliente", "Contacto", "Estado", "Registrado", ""].map((h) => (
              <th
                key={h}
                className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400"
              >
                {h}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
          {customers.map((c) => (
            <tr
              key={c.id}
              className="group bg-white transition-colors hover:bg-slate-50 dark:bg-slate-900 dark:hover:bg-slate-800/50"
            >
              {/* Documento */}
              <td className="px-4 py-3.5">
                <div className="flex items-center gap-2">
                  <span className="rounded-md bg-slate-100 px-1.5 py-0.5 text-[10px] font-bold text-slate-600 dark:bg-slate-700 dark:text-slate-300">
                    {DOC_LABEL[c.tipoDocumento] ?? c.tipoDocumento}
                  </span>
                  <span className="font-mono text-xs text-slate-700 dark:text-slate-300">
                    {c.numeroDocumento}
                  </span>
                </div>
              </td>

              {/* Nombre */}
              <td className="px-4 py-3.5">
                <p className="font-medium text-slate-900 dark:text-white">{c.nombreCompleto}</p>
              </td>

              {/* Contacto */}
              <td className="px-4 py-3.5">
                <div className="space-y-0.5">
                  {c.email && (
                    <p className="text-xs text-slate-600 dark:text-slate-300">{c.email}</p>
                  )}
                  {c.telefono && (
                    <p className="text-xs text-slate-400">{c.telefono}</p>
                  )}
                </div>
              </td>

              {/* Estado */}
              <td className="px-4 py-3.5">
                <StatusBadge status={c.estado} />
              </td>

              {/* Fecha */}
              <td className="px-4 py-3.5 text-xs text-slate-400">
                {c.createdAt ? formatDate(c.createdAt) : "—"}
              </td>

              {/* Acciones */}
              <td className="px-4 py-3.5">
                <div className="flex items-center justify-end gap-1 opacity-0 transition-opacity group-hover:opacity-100">
                  <ActionButton
                    label="Editar"
                    onClick={() => onEdit(c)}
                    icon={<Pencil className="h-3.5 w-3.5" />}
                  />
                  <ActionButton
                    label="Eliminar"
                    onClick={() => handleDelete(c)}
                    icon={<Trash2 className="h-3.5 w-3.5" />}
                    danger
                  />
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function StatusBadge({ status }: { status: string }) {
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-medium",
        status === "ACTIVO"
          ? "bg-emerald-50 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-400"
          : "bg-slate-100 text-slate-500 dark:bg-slate-800 dark:text-slate-400",
      )}
    >
      <span
        className={cn(
          "h-1.5 w-1.5 rounded-full",
          status === "ACTIVO" ? "bg-emerald-500" : "bg-slate-400",
        )}
      />
      {status}
    </span>
  );
}

function ActionButton({
  label,
  onClick,
  icon,
  danger = false,
}: {
  label: string;
  onClick: () => void;
  icon: React.ReactNode;
  danger?: boolean;
}) {
  return (
    <button
      title={label}
      onClick={onClick}
      className={cn(
        "rounded-lg p-1.5 transition-colors",
        danger
          ? "text-slate-400 hover:bg-red-50 hover:text-red-600 dark:hover:bg-red-950"
          : "text-slate-400 hover:bg-blue-50 hover:text-blue-600 dark:hover:bg-blue-950",
      )}
    >
      {icon}
    </button>
  );
}
