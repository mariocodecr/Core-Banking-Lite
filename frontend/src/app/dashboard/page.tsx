import type { Metadata } from "next";

export const metadata: Metadata = { title: "Dashboard" };

export default function DashboardPage() {
  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold text-slate-900 dark:text-white">Dashboard financiero</h1>
      <p className="mt-2 text-sm text-slate-500">
        Los KPIs y gráficos se implementan en la Fase 6.
      </p>
    </div>
  );
}
