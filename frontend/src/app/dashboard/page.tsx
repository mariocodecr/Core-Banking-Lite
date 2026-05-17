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
import { useInvestmentSummary, useCombinedPortfolio } from "@/hooks/use-investments";
import { useMyAccounts } from "@/hooks/use-accounts";
import { useMyTransfers } from "@/hooks/use-transfers";
import { usePermissions } from "@/hooks/use-current-user";
import { formatCurrency } from "@/lib/utils";

const TransferVolumeChart = dynamic(
  () => import("@/features/dashboard/transfer-volume-chart").then((m) => m.TransferVolumeChart),
  { ssr: false }
);

const AccountTypeChart = dynamic(
  () => import("@/features/dashboard/account-type-chart").then((m) => m.AccountTypeChart),
  { ssr: false }
);

function ClientDashboard() {
  const { data: accounts, isLoading: loadingAccounts } = useMyAccounts();
  const { data: portfolio } = useCombinedPortfolio();
  const { data: transfers } = useMyTransfers({ size: 5 });

  const activeAccounts = accounts?.filter((a) => a.estado === "ACTIVA") ?? [];
  const totalUSD = activeAccounts.filter((a) => a.moneda === "USD").reduce((s, a) => s + a.saldo, 0);
  const totalCRC = activeAccounts.filter((a) => a.moneda === "CRC").reduce((s, a) => s + a.saldo, 0);
  const totalPositions = portfolio?.positions?.length ?? 0;
  const totalInvested  = portfolio?.totalInvested     ?? 0;
  const currentValue   = portfolio?.totalCurrentValue ?? 0;
  const totalPnl       = portfolio?.totalPnl          ?? 0;
  const totalPnlPct    = portfolio?.totalPnlPercent   ?? 0;

  return (
    <div className="space-y-6 p-6">
      <div>
        <h1 className="text-xl font-bold text-slate-900 dark:text-white">Mi resumen financiero</h1>
        <p className="mt-0.5 text-xs text-slate-500">Tus cuentas e inversiones personales</p>
      </div>

      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <KpiCard label="Cuentas activas"    value={activeAccounts.length}           icon={CreditCard} color="blue"    isLoading={loadingAccounts} />
        <KpiCard label="Saldo USD"          value={formatCurrency(totalUSD, "USD")} icon={Banknote}   color="emerald" isLoading={loadingAccounts} />
        <KpiCard label="Saldo CRC"          value={formatCurrency(totalCRC, "CRC")} icon={Banknote}   color="amber"   isLoading={loadingAccounts} />
        <KpiCard label="Posiciones abiertas" value={totalPositions}                 icon={TrendingUp} color="violet" />
      </div>

      {totalPositions > 0 && (
        <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
          <KpiCard
            label="Total invertido"
            value={formatCurrency(totalInvested, "USD")}
            icon={TrendingUp}
            color="violet"
          />
          <KpiCard
            label="Valor actual"
            value={formatCurrency(currentValue, "USD")}
            icon={Activity}
            color="blue"
          />
          <KpiCard
            label="Ganancias / Pérdidas"
            value={`${totalPnl >= 0 ? "+" : ""}${formatCurrency(totalPnl, "USD")}`}
            sub={`${totalPnlPct >= 0 ? "+" : ""}${totalPnlPct.toFixed(2)}%`}
            icon={totalPnl >= 0 ? TrendingUp : Activity}
            color={totalPnl >= 0 ? "emerald" : "amber"}
          />
          <KpiCard
            label="Posiciones"
            value={totalPositions}
            sub={`${totalPositions} instrumento${totalPositions !== 1 ? "s" : ""}`}
            icon={CreditCard}
            color="blue"
          />
        </div>
      )}

      <ExchangeRateCard />

      {transfers && transfers.content.length > 0 && (
        <div className="rounded-2xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900">
          <p className="mb-3 text-sm font-semibold text-slate-700 dark:text-slate-200">
            Últimas transferencias
          </p>
          <div className="space-y-2">
            {transfers.content.map((t) => (
              <div key={t.id} className="flex items-center justify-between rounded-xl bg-slate-50 px-4 py-2.5 dark:bg-slate-800/50">
                <div className="min-w-0">
                  <p className="truncate font-mono text-xs font-medium text-slate-700 dark:text-slate-200">
                    {t.numeroCuentaOrigen} → {t.numeroCuentaDestino}
                  </p>
                  <p className="truncate text-[10px] text-slate-400">{t.descripcion}</p>
                </div>
                <p className="ml-3 shrink-0 text-sm font-bold text-slate-800 dark:text-white">
                  {formatCurrency(t.monto, t.moneda)}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

export default function DashboardPage() {
  const { role } = usePermissions();
  const isClient = role === "CLIENT";
  const roleReady = !!role;

  const { data: summary, isLoading } = useDashboardSummary(roleReady && !isClient);
  const { data: investSummary } = useInvestmentSummary(roleReady && !isClient);

  if (isClient) {
    return <ClientDashboard />;
  }

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

      {/* Investment KPIs */}
      {investSummary && (investSummary.totalInvested > 0 || investSummary.activePortfolios > 0) && (
        <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
          <KpiCard
            label="Total invertido"
            value={formatCurrency(investSummary.totalInvested, "USD")}
            icon={TrendingUp}
            color="violet"
          />
          <KpiCard
            label="Valor actual"
            value={formatCurrency(investSummary.totalCurrentValue, "USD")}
            icon={TrendingUp}
            color="blue"
          />
          <KpiCard
            label="Ganancias / Pérdidas"
            value={`${investSummary.totalPnl >= 0 ? "+" : ""}${formatCurrency(investSummary.totalPnl, "USD")}`}
            sub={`${investSummary.totalPnlPercent >= 0 ? "+" : ""}${investSummary.totalPnlPercent.toFixed(2)}%`}
            icon={investSummary.totalPnl >= 0 ? TrendingUp : Activity}
            color={investSummary.totalPnl >= 0 ? "emerald" : "amber"}
          />
          <KpiCard
            label="Posiciones abiertas"
            value={investSummary.totalPositions}
            sub={`${investSummary.activePortfolios} portfolio${investSummary.activePortfolios !== 1 ? "s" : ""}`}
            icon={Activity}
            color="blue"
          />
        </div>
      )}

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
