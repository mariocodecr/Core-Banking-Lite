"use client";

import { cn, formatCurrency, formatShares } from "@/lib/utils";
import { useOrderHistory } from "@/hooks/use-investments";
import { Clock } from "lucide-react";
import axios from "axios";
import type { OrderStatus } from "@/types/investment.types";

interface Props {
  accountId: string;
}

export function OrderHistory({ accountId }: Props) {
  const { data, isLoading, isError, error } = useOrderHistory(accountId);

  const isNotFound = isError && axios.isAxiosError(error) && error.response?.status === 404;

  if (isLoading) {
    return (
      <div className="space-y-2">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="h-10 animate-pulse rounded-lg bg-slate-200 dark:bg-slate-700" />
        ))}
      </div>
    );
  }

  if (isNotFound || !data?.content.length) {
    return (
      <p className="py-6 text-center text-xs text-slate-400">No hay órdenes ejecutadas aún</p>
    );
  }

  if (isError) {
    return <p className="text-xs text-red-500">Error al cargar el historial</p>;
  }

  return (
    <div className="overflow-x-auto rounded-2xl border border-slate-200 dark:border-slate-700">
      <table className="w-full text-xs">
        <thead>
          <tr className="border-b border-slate-200 bg-slate-50 dark:border-slate-700 dark:bg-slate-800/50">
            {["Fecha", "Estado", "Tipo", "Símbolo", "Acciones", "Precio", "Total"].map((h) => (
              <th key={h} className="px-4 py-2.5 text-left font-semibold text-slate-500">{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.content.map((order) => (
            <tr key={order.id} className="border-b border-slate-100 last:border-0 dark:border-slate-800">
              <td className="px-4 py-3 text-slate-500">
                {new Date(order.fechaOrden).toLocaleDateString("es-CR", {
                  day: "2-digit", month: "short", year: "numeric",
                })}
              </td>
              <td className="px-4 py-3">
                <StatusBadge status={order.estado} />
              </td>
              <td className="px-4 py-3">
                <span className={cn(
                  "rounded-full px-2 py-0.5 text-[10px] font-semibold",
                  order.tipo === "BUY"
                    ? "bg-emerald-100 text-emerald-700 dark:bg-emerald-900/40 dark:text-emerald-400"
                    : "bg-red-100 text-red-600 dark:bg-red-900/40 dark:text-red-400",
                )}>
                  {order.tipo === "BUY" ? "Compra" : "Venta"}
                </span>
              </td>
              <td className="px-4 py-3 font-semibold text-slate-900 dark:text-white">{order.symbol}</td>
              <td className="px-4 py-3 tabular-nums text-slate-700 dark:text-slate-300">
                {formatShares(order.shares)}
              </td>
              <td className="px-4 py-3 tabular-nums text-slate-700 dark:text-slate-300">
                {formatCurrency(order.pricePerShare, "USD")}
              </td>
              <td className="px-4 py-3 font-semibold tabular-nums text-slate-900 dark:text-white">
                {formatCurrency(order.totalAmount, "USD")}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function StatusBadge({ status }: { status: OrderStatus }) {
  if (status === "PENDING") {
    return (
      <span className="inline-flex items-center gap-1 rounded-full bg-amber-100 px-2 py-0.5 text-[10px] font-semibold text-amber-700 dark:bg-amber-900/40 dark:text-amber-400">
        <Clock className="h-2.5 w-2.5" />
        Pendiente
      </span>
    );
  }
  if (status === "EXECUTED") {
    return (
      <span className="rounded-full bg-emerald-100 px-2 py-0.5 text-[10px] font-semibold text-emerald-700 dark:bg-emerald-900/40 dark:text-emerald-400">
        Ejecutada
      </span>
    );
  }
  return (
    <span className="rounded-full bg-red-100 px-2 py-0.5 text-[10px] font-semibold text-red-600 dark:bg-red-900/40 dark:text-red-400">
      Fallida
    </span>
  );
}
