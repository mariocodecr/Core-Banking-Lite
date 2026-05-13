import { cn } from "@/lib/utils";
import type { LucideIcon } from "lucide-react";

interface KpiCardProps {
  label: string;
  value: string | number;
  sub?: string;
  icon: LucideIcon;
  trend?: "up" | "down" | "neutral";
  color?: "blue" | "emerald" | "violet" | "amber";
  isLoading?: boolean;
}

const COLOR_MAP = {
  blue:    { bg: "bg-blue-50 dark:bg-blue-950/50",    icon: "bg-blue-600",    text: "text-blue-600" },
  emerald: { bg: "bg-emerald-50 dark:bg-emerald-950/50", icon: "bg-emerald-600", text: "text-emerald-600" },
  violet:  { bg: "bg-violet-50 dark:bg-violet-950/50", icon: "bg-violet-600",  text: "text-violet-600" },
  amber:   { bg: "bg-amber-50 dark:bg-amber-950/50",  icon: "bg-amber-500",   text: "text-amber-600" },
};

export function KpiCard({ label, value, sub, icon: Icon, color = "blue", isLoading }: KpiCardProps) {
  const c = COLOR_MAP[color];

  if (isLoading) {
    return (
      <div className="animate-pulse rounded-2xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900">
        <div className="flex items-start justify-between">
          <div className="space-y-2">
            <div className="h-3 w-24 rounded bg-slate-200 dark:bg-slate-700" />
            <div className="h-7 w-32 rounded bg-slate-200 dark:bg-slate-700" />
            <div className="h-2.5 w-20 rounded bg-slate-100 dark:bg-slate-800" />
          </div>
          <div className="h-10 w-10 rounded-xl bg-slate-200 dark:bg-slate-700" />
        </div>
      </div>
    );
  }

  return (
    <div className={cn("rounded-2xl border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900", c.bg)}>
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-medium uppercase tracking-wide text-slate-500 dark:text-slate-400">{label}</p>
          <p className="mt-1.5 text-2xl font-bold tracking-tight text-slate-900 dark:text-white">{value}</p>
          {sub && <p className="mt-0.5 text-xs text-slate-500 dark:text-slate-400">{sub}</p>}
        </div>
        <div className={cn("flex h-10 w-10 shrink-0 items-center justify-center rounded-xl", c.icon)}>
          <Icon className="h-5 w-5 text-white" />
        </div>
      </div>
    </div>
  );
}
