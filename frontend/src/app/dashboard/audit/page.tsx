"use client";

import { useState, useMemo } from "react";
import {
  ShieldCheck,
  CheckCircle2,
  XCircle,
  ArrowRight,
  Search,
  TrendingUp,
  AlertTriangle,
  Activity,
} from "lucide-react";
import { useTransfers } from "@/hooks/use-transfers";
import { cn, formatCurrency, formatDateTime } from "@/lib/utils";
import type { Transfer, TransferStatus } from "@/types/transfer.types";

type StatusFilter = TransferStatus | "";

export default function AuditPage() {
  const [statusFilter, setStatusFilter] = useState<StatusFilter>("");
  const [search, setSearch] = useState("");

  const { data, isLoading, isError } = useTransfers({ size: 200 });

  const filtered = useMemo(() => {
    if (!data?.content) return [];
    return data.content.filter((t) => {
      const matchStatus = !statusFilter || t.estado === statusFilter;
      const q = search.toLowerCase();
      const matchSearch =
        !q ||
        t.referencia.toLowerCase().includes(q) ||
        t.numeroCuentaOrigen.toLowerCase().includes(q) ||
        t.numeroCuentaDestino.toLowerCase().includes(q) ||
        t.descripcion.toLowerCase().includes(q);
      return matchStatus && matchSearch;
    });
  }, [data, statusFilter, search]);

  const stats = useMemo(() => {
    const all = data?.content ?? [];
    const completed = all.filter((t) => t.estado === "COMPLETADA");
    const failed    = all.filter((t) => t.estado === "FALLIDA");
    const volume    = completed.reduce((s, t) => s + t.monto, 0);
    return { total: all.length, completed: completed.length, failed: failed.length, volume };
  }, [data]);

  return (
    <div className="flex h-full flex-col gap-6 overflow-y-auto p-6">
      {/* ── Page header ─────────────────────────────────────── */}
      <div className="flex items-center gap-3">
        <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-slate-900 dark:bg-white">
          <ShieldCheck className="h-4.5 w-4.5 text-white dark:text-slate-900" />
        </div>
        <div>
          <h1 className="text-xl font-bold text-slate-900 dark:text-white">Auditoría</h1>
          <p className="text-xs text-slate-500">Registro inmutable de todas las operaciones financieras</p>
        </div>
      </div>

      {/* ── Stats strip ─────────────────────────────────────── */}
      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <StatCard
          icon={<Activity className="h-4 w-4 text-slate-600 dark:text-slate-300" />}
          label="Total operaciones"
          value={String(stats.total)}
          bg="bg-slate-100 dark:bg-slate-800"
        />
        <StatCard
          icon={<CheckCircle2 className="h-4 w-4 text-emerald-600" />}
          label="Completadas"
          value={String(stats.completed)}
          bg="bg-emerald-50 dark:bg-emerald-900/20"
        />
        <StatCard
          icon={<AlertTriangle className="h-4 w-4 text-red-500" />}
          label="Fallidas"
          value={String(stats.failed)}
          bg="bg-red-50 dark:bg-red-900/20"
        />
        <StatCard
          icon={<TrendingUp className="h-4 w-4 text-blue-600" />}
          label="Volumen procesado"
          value={formatCurrency(stats.volume, "PEN")}
          bg="bg-blue-50 dark:bg-blue-900/20"
        />
      </div>

      {/* ── Filters ─────────────────────────────────────────── */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex gap-1.5">
          {(["", "COMPLETADA", "FALLIDA"] as const).map((s) => (
            <button
              key={s}
              onClick={() => setStatusFilter(s)}
              className={cn(
                "rounded-lg px-3 py-1.5 text-xs font-medium transition-colors",
                statusFilter === s
                  ? s === "COMPLETADA"
                    ? "bg-emerald-600 text-white"
                    : s === "FALLIDA"
                      ? "bg-red-500 text-white"
                      : "bg-slate-900 text-white dark:bg-white dark:text-slate-900"
                  : "bg-slate-100 text-slate-600 hover:bg-slate-200 dark:bg-slate-800 dark:text-slate-300",
              )}
            >
              {s || "Todas"}
            </button>
          ))}
        </div>

        <div className="relative w-full sm:w-72">
          <Search className="absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-slate-400" />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar referencia, cuenta, descripción..."
            className="w-full rounded-xl border border-slate-200 bg-white py-2 pl-8 pr-3 text-xs focus:border-slate-400 focus:outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-white"
          />
        </div>
      </div>

      {/* ── Results count ───────────────────────────────────── */}
      <p className="text-xs text-slate-500">
        {isLoading ? "Cargando..." : `${filtered.length} operaciones`}
      </p>

      {/* ── Table ───────────────────────────────────────────── */}
      {isError ? (
        <div className="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-600">
          Error al cargar el registro de auditoría.
        </div>
      ) : isLoading ? (
        <AuditSkeleton />
      ) : filtered.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-20 text-center">
          <ShieldCheck className="mx-auto mb-3 h-10 w-10 text-slate-200" />
          <p className="text-sm font-medium text-slate-500">Sin registros</p>
          <p className="mt-1 text-xs text-slate-400">Ajustá los filtros para ver operaciones</p>
        </div>
      ) : (
        <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900">
          {/* Table header */}
          <div className="grid grid-cols-[1fr_1fr_auto_auto_auto] gap-3 border-b border-slate-100 bg-slate-50 px-4 py-2.5 dark:border-slate-800 dark:bg-slate-800/50">
            {["Referencia", "Cuentas", "Fecha", "Monto", "Estado"].map((h) => (
              <span key={h} className="text-[10px] font-semibold uppercase tracking-wider text-slate-400">
                {h}
              </span>
            ))}
          </div>

          {/* Table rows */}
          <div className="divide-y divide-slate-100 dark:divide-slate-800">
            {filtered.map((t) => (
              <AuditRow key={t.id} transfer={t} />
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

function AuditRow({ transfer: t }: { transfer: Transfer }) {
  const isOk = t.estado === "COMPLETADA";

  return (
    <div className="grid grid-cols-[1fr_1fr_auto_auto_auto] items-center gap-3 px-4 py-3 transition-colors hover:bg-slate-50 dark:hover:bg-slate-800/40">
      {/* Referencia */}
      <div className="min-w-0">
        <p className="truncate font-mono text-xs font-semibold text-slate-800 dark:text-slate-200">
          {t.referencia}
        </p>
        {t.descripcion && (
          <p className="mt-0.5 truncate text-[10px] text-slate-400">{t.descripcion}</p>
        )}
        {!isOk && t.motivoFallo && (
          <p className="mt-0.5 truncate text-[10px] text-red-500">{t.motivoFallo}</p>
        )}
      </div>

      {/* Cuentas */}
      <div className="min-w-0">
        <div className="flex items-center gap-1">
          <span className="truncate font-mono text-[10px] text-slate-600 dark:text-slate-300">
            {t.numeroCuentaOrigen}
          </span>
          <ArrowRight className="h-2.5 w-2.5 shrink-0 text-slate-300" />
          <span className="truncate font-mono text-[10px] text-slate-600 dark:text-slate-300">
            {t.numeroCuentaDestino}
          </span>
        </div>
        <p className="mt-0.5 truncate text-[10px] text-slate-400">
          {t.nombreClienteOrigen} → {t.nombreClienteDestino}
        </p>
      </div>

      {/* Fecha */}
      <p className="whitespace-nowrap text-[10px] text-slate-500">
        {formatDateTime(t.fechaTransferencia)}
      </p>

      {/* Monto */}
      <p
        className={cn(
          "whitespace-nowrap text-sm font-bold",
          isOk ? "text-slate-800 dark:text-white" : "text-red-400 line-through",
        )}
      >
        {formatCurrency(t.monto, t.moneda)}
      </p>

      {/* Estado */}
      <span
        className={cn(
          "flex items-center gap-1 whitespace-nowrap rounded-full px-2 py-0.5 text-[10px] font-semibold",
          isOk
            ? "bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400"
            : "bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400",
        )}
      >
        {isOk ? <CheckCircle2 className="h-3 w-3" /> : <XCircle className="h-3 w-3" />}
        {isOk ? "Completada" : "Fallida"}
      </span>
    </div>
  );
}

function StatCard({
  icon,
  label,
  value,
  bg,
}: {
  icon: React.ReactNode;
  label: string;
  value: string;
  bg: string;
}) {
  return (
    <div className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900">
      <div className={cn("rounded-xl p-2.5", bg)}>{icon}</div>
      <div className="min-w-0">
        <p className="truncate text-[10px] text-slate-500">{label}</p>
        <p className="mt-0.5 truncate text-sm font-bold text-slate-900 dark:text-white">{value}</p>
      </div>
    </div>
  );
}

function AuditSkeleton() {
  return (
    <div className="space-y-2">
      {Array.from({ length: 6 }).map((_, i) => (
        <div key={i} className="h-14 animate-pulse rounded-xl bg-slate-100 dark:bg-slate-800" />
      ))}
    </div>
  );
}
