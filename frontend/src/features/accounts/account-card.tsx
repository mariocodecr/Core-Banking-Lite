"use client";

import { Snowflake, Lock, TrendingUp } from "lucide-react";
import { cn, formatCurrency } from "@/lib/utils";
import type { Account, AccountStatus, AccountType } from "@/types/account.types";

const TYPE_CONFIG: Record<AccountType, { label: string; gradient: string; textMuted: string }> = {
  AHORROS:   { label: "Cuenta de Ahorros",  gradient: "from-blue-600 to-blue-800",    textMuted: "text-blue-200" },
  EMPRESARIAL: { label: "Cuenta Empresarial", gradient: "from-emerald-600 to-emerald-800", textMuted: "text-emerald-200" },
  CORRIENTE: { label: "Cuenta Corriente",    gradient: "from-slate-600 to-slate-800",  textMuted: "text-slate-300" },
};

const STATUS_ICON: Record<AccountStatus, React.ReactNode> = {
  ACTIVA:    null,
  CONGELADA: <Snowflake className="h-3.5 w-3.5" />,
  CERRADA:   <Lock className="h-3.5 w-3.5" />,
};

interface Props {
  account: Account;
  onClick?: () => void;
  selected?: boolean;
}

export function AccountCard({ account, onClick, selected }: Props) {
  const config = TYPE_CONFIG[account.tipo];
  const isClickable = !!onClick;

  return (
    <button
      onClick={onClick}
      disabled={!isClickable}
      className={cn(
        "group relative w-full overflow-hidden rounded-2xl bg-gradient-to-br p-5 text-left transition-all",
        config.gradient,
        isClickable && "cursor-pointer hover:scale-[1.02] hover:shadow-xl",
        selected && "ring-2 ring-white ring-offset-2",
        !isClickable && "cursor-default",
      )}
    >
      {/* Background pattern */}
      <div className="absolute inset-0 opacity-10">
        <div className="absolute -right-8 -top-8 h-32 w-32 rounded-full bg-white" />
        <div className="absolute -bottom-4 -left-4 h-20 w-20 rounded-full bg-white" />
      </div>

      <div className="relative space-y-4">
        {/* Header */}
        <div className="flex items-start justify-between">
          <div>
            <p className={cn("text-xs font-medium uppercase tracking-widest", config.textMuted)}>
              {config.label}
            </p>
            <p className="mt-0.5 font-mono text-sm text-white/90">{account.numeroCuenta}</p>
          </div>

          <StatusBadge status={account.estado} />
        </div>

        {/* Balance */}
        <div>
          <p className={cn("text-xs", config.textMuted)}>Saldo disponible</p>
          <p className="mt-0.5 text-2xl font-bold tracking-tight text-white">
            {formatCurrency(account.saldo, account.moneda)}
          </p>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-between pt-1">
          <p className="text-xs text-white/60">
            Titular: <span className="text-white/90">{account.nombreCliente}</span>
          </p>
          {isClickable && (
            <TrendingUp className={cn("h-4 w-4 transition-opacity", config.textMuted, "opacity-0 group-hover:opacity-100")} />
          )}
        </div>
      </div>
    </button>
  );
}

function StatusBadge({ status }: { status: AccountStatus }) {
  const configs: Record<AccountStatus, { label: string; className: string }> = {
    ACTIVA:    { label: "Activa",    className: "bg-white/20 text-white" },
    CONGELADA: { label: "Congelada", className: "bg-blue-200/30 text-blue-100" },
    CERRADA:   { label: "Cerrada",   className: "bg-black/30 text-white/60" },
  };
  const { label, className } = configs[status];
  const icon = STATUS_ICON[status];

  return (
    <span className={cn("flex items-center gap-1 rounded-full px-2 py-0.5 text-[10px] font-semibold", className)}>
      {icon}
      {label}
    </span>
  );
}
