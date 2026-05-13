"use client";

import {
  Users,
  CreditCard,
  ArrowLeftRight,
  Banknote,
  TrendingUp,
  Activity,
} from "lucide-react";
import dynamic from "next/dynamic";
import { KpiCard } from "@/features/dashboard/kpi-card";
import { ExchangeRateCard } from "@/features/dashboard/exchange-rate-card";
import { useDashboardSummary } from "@/hooks/use-dashboard";
import { formatCurrency } from "@/lib/utils";

const TransferVolumeChart = dynamic(
  () => import("@/features/dashboard/transfer-volume-chart").then((m) => m.TransferVolumeChart),
  { ssr: false }
);

const AccountTypeChart = dynamic(
  () => import("@/features/dashboard/account-type-chart").then((m) => m.AccountTypeChart),
  { ssr: false }
);

export default function DashboardPage() {
  const { data: summary, isLoading } = useDashboardSummary();

  return (
    <div className="space-y-6 p-6">
      {/* Header */}
      <div>
        <h1 className="text-xl font-bold text-slate-900 dark:text-white">Dashboard financiero</h1>
        <p className="mt-0.5 text-xs text-slate-500">Resumen operativo en tiempo real</p>
      </div>

      {/* KPI Grid */}
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <KpiCard
          label="Clientes activos"
          value={summary?.activeCustomers ?? "—"}
          sub={summary ? `de ${summary.totalCustomers} totales` : undefined}
          icon={Users}
          color="blue"
          isLoading={isLoading}
        />
        <KpiCard
          label="Cuentas activas"
          value={summary?.totalAccounts ?? "—"}
          icon={CreditCard}
          color="emerald"
          isLoading={isLoading}
        />
        <KpiCard
          label="Transferencias hoy"
          value={summary?.totalTransfersToday ?? "—"}
          sub={summary ? formatCurrency(summary.transferVolumeToday, "USD") : undefined}
          icon={ArrowLeftRight}
          color="violet"
          isLoading={isLoading}
        />
        <KpiCard
          label="Transferencias este mes"
          value={summary?.totalTransfersThisMonth ?? "—"}
          sub={summary ? formatCurrency(summary.transferVolumeThisMonth, "USD") : undefined}
          icon={TrendingUp}
          color="amber"
          isLoading={isLoading}
        />
      </div>

      {/* Balance KPIs */}
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-3">
        <KpiCard
          label="Saldo total USD"
          value={summary ? formatCurrency(summary.totalBalanceUSD ?? 0, "USD") : "—"}
          icon={Banknote}
          color="emerald"
          isLoading={isLoading}
        />
        <KpiCard
          label="Saldo total CRC"
          value={summary ? formatCurrency(summary.totalBalanceCRC ?? 0, "CRC") : "—"}
          icon={Banknote}
          color="amber"
          isLoading={isLoading}
        />
        <KpiCard
          label="Saldo total EUR"
          value={summary ? formatCurrency(summary.totalBalanceEUR ?? 0, "EUR") : "—"}
          icon={Activity}
          color="blue"
          isLoading={isLoading}
        />
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Transfer volume — takes 2/3 */}
        <div className="rounded-2xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900 lg:col-span-2">
          <p className="mb-1 text-sm font-semibold text-slate-700 dark:text-slate-200">
            Volumen de transferencias — últimos 30 días
          </p>
          <p className="mb-4 text-xs text-slate-500">Solo transferencias COMPLETADAS</p>
          <TransferVolumeChart />
        </div>

        {/* Right column — account distribution + exchange rates */}
        <div className="space-y-6">
          <div className="rounded-2xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900">
            <p className="mb-1 text-sm font-semibold text-slate-700 dark:text-slate-200">
              Distribución de cuentas
            </p>
            <p className="mb-4 text-xs text-slate-500">Por tipo (excluye CERRADAS)</p>
            <AccountTypeChart />
          </div>

          <ExchangeRateCard />
        </div>
      </div>
    </div>
  );
}
