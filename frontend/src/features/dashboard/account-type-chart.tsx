"use client";

import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip, Legend } from "recharts";
import { useAccountTypeStats } from "@/hooks/use-dashboard";
import { formatCurrency } from "@/lib/utils";

const COLORS: Record<string, string> = {
  AHORROS:   "#2563eb",
  CTS:       "#10b981",
  CORRIENTE: "#64748b",
};

const LABEL: Record<string, string> = {
  AHORROS:   "Ahorros",
  CTS:       "CTS",
  CORRIENTE: "Corriente",
};

export function AccountTypeChart() {
  const { data, isLoading } = useAccountTypeStats();

  if (isLoading) {
    return (
      <div className="flex h-56 items-center justify-center">
        <div className="h-40 w-40 animate-pulse rounded-full bg-slate-100 dark:bg-slate-800" />
      </div>
    );
  }

  if (!data?.length) {
    return (
      <div className="flex h-56 items-center justify-center rounded-xl bg-slate-50 dark:bg-slate-800/50">
        <p className="text-sm text-slate-400">Sin cuentas registradas</p>
      </div>
    );
  }

  const chartData = data.map((d) => ({
    name:         LABEL[d.tipo] ?? d.tipo,
    tipo:         d.tipo,
    value:        d.count,
    totalBalance: d.totalBalance,
  }));

  return (
    <ResponsiveContainer width="100%" height={220}>
      <PieChart>
        <Pie
          data={chartData}
          cx="50%"
          cy="50%"
          innerRadius={55}
          outerRadius={85}
          paddingAngle={3}
          dataKey="value"
        >
          {chartData.map((entry) => (
            <Cell key={entry.tipo} fill={COLORS[entry.tipo] ?? "#94a3b8"} />
          ))}
        </Pie>
        <Tooltip
          contentStyle={{ borderRadius: 12, fontSize: 12, border: "1px solid #e2e8f0" }}
          formatter={(value, _name, item) => {
            const p = (item as { payload: { totalBalance: number; name: string } }).payload;
            return [`${Number(value)} cuentas — ${formatCurrency(p.totalBalance, "PEN")}`, p.name] as [string, string];
          }}
        />
        <Legend
          iconType="circle"
          iconSize={8}
          formatter={(value) => <span style={{ fontSize: 12, color: "#64748b" }}>{value}</span>}
        />
      </PieChart>
    </ResponsiveContainer>
  );
}
