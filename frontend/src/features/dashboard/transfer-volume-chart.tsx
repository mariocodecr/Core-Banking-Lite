"use client";

import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { useDailyTransferStats } from "@/hooks/use-dashboard";
import { formatCurrency } from "@/lib/utils";

export function TransferVolumeChart() {
  const { data, isLoading } = useDailyTransferStats(30);

  if (isLoading) {
    return (
      <div className="h-56 animate-pulse rounded-xl bg-slate-100 dark:bg-slate-800" />
    );
  }

  if (!data?.length) {
    return (
      <div className="flex h-56 items-center justify-center rounded-xl bg-slate-50 dark:bg-slate-800/50">
        <p className="text-sm text-slate-400">Sin datos de transferencias</p>
      </div>
    );
  }

  const formatted = data.map((d) => ({
    date:   new Date(d.date).toLocaleDateString("es-PE", { day: "2-digit", month: "short" }),
    volume: d.volume,
    count:  d.count,
  }));

  return (
    <ResponsiveContainer width="100%" height={220}>
      <AreaChart data={formatted} margin={{ top: 4, right: 4, left: 0, bottom: 0 }}>
        <defs>
          <linearGradient id="volumeGradient" x1="0" y1="0" x2="0" y2="1">
            <stop offset="5%"  stopColor="#2563eb" stopOpacity={0.2} />
            <stop offset="95%" stopColor="#2563eb" stopOpacity={0} />
          </linearGradient>
        </defs>
        <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
        <XAxis
          dataKey="date"
          tick={{ fontSize: 11, fill: "#94a3b8" }}
          tickLine={false}
          axisLine={false}
          interval="preserveStartEnd"
        />
        <YAxis
          tick={{ fontSize: 11, fill: "#94a3b8" }}
          tickLine={false}
          axisLine={false}
          tickFormatter={(v) => `S/ ${(v / 1000).toFixed(0)}k`}
          width={56}
        />
        <Tooltip
          contentStyle={{ borderRadius: 12, fontSize: 12, border: "1px solid #e2e8f0" }}
          formatter={(value) => [formatCurrency(Number(value), "PEN"), "Volumen"] as [string, string]}
        />
        <Area
          type="monotone"
          dataKey="volume"
          stroke="#2563eb"
          strokeWidth={2}
          fill="url(#volumeGradient)"
          dot={false}
          activeDot={{ r: 4 }}
        />
      </AreaChart>
    </ResponsiveContainer>
  );
}
