"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  LayoutDashboard,
  Users,
  CreditCard,
  ArrowLeftRight,
  PiggyBank,
  ClipboardList,
  ShieldCheck,
  LogOut,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { useLogout } from "@/hooks/use-auth";
import { useCurrentUser } from "@/hooks/use-current-user";
import { ROUTES } from "@/constants";
import type { Role } from "@/types/auth.types";

const ROLE_LABEL: Record<Role, string> = {
  ADMIN:   "Administrador",
  ADVISOR: "Asesor",
  AUDITOR: "Auditor",
  CLIENT:  "Cliente",
};

const ROLE_COLOR: Record<Role, string> = {
  ADMIN:   "bg-red-100 text-red-700 dark:bg-red-900/40 dark:text-red-400",
  ADVISOR: "bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-400",
  AUDITOR: "bg-amber-100 text-amber-700 dark:bg-amber-900/40 dark:text-amber-400",
  CLIENT:  "bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400",
};

type NavItem = {
  href: string;
  label: string;
  icon: React.ElementType;
  roles: Role[];
};

const NAV_ITEMS: NavItem[] = [
  { href: ROUTES.DASHBOARD,  label: "Dashboard",      icon: LayoutDashboard, roles: ["ADMIN", "ADVISOR", "AUDITOR"] },
  { href: ROUTES.CUSTOMERS,  label: "Clientes",       icon: Users,           roles: ["ADMIN", "ADVISOR", "AUDITOR"] },
  { href: ROUTES.ACCOUNTS,   label: "Cuentas",        icon: CreditCard,      roles: ["ADMIN", "ADVISOR", "AUDITOR", "CLIENT"] },
  { href: ROUTES.TRANSFERS,  label: "Transferencias", icon: ArrowLeftRight,  roles: ["ADMIN", "ADVISOR", "AUDITOR", "CLIENT"] },
  { href: ROUTES.SAVINGS,    label: "Ahorros",         icon: PiggyBank,       roles: ["ADMIN", "ADVISOR", "AUDITOR"] },
  { href: ROUTES.AUDIT,      label: "Auditoría",      icon: ClipboardList,   roles: ["ADMIN", "ADVISOR", "AUDITOR"] },
];

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const { mutate: logout } = useLogout();
  const user = useCurrentUser();

  const visibleNav = user
    ? NAV_ITEMS.filter((item) => item.roles.includes(user.role))
    : NAV_ITEMS;

  return (
    <div className="flex h-screen bg-slate-50 dark:bg-slate-950">
      {/* Sidebar */}
      <aside className="flex w-64 flex-col border-r border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900">
        {/* Logo */}
        <div className="flex items-center gap-2.5 border-b border-slate-200 px-5 py-4 dark:border-slate-800">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-blue-600">
            <ShieldCheck className="h-4 w-4 text-white" />
          </div>
          <span className="text-sm font-semibold text-slate-900 dark:text-white">
            Core Banking
          </span>
        </div>

        {/* User info */}
        {user && (
          <div className="border-b border-slate-200 px-5 py-3 dark:border-slate-800">
            <p className="truncate text-xs font-semibold text-slate-900 dark:text-white">
              {user.fullName}
            </p>
            <p className="truncate text-[10px] text-slate-500">{user.email}</p>
            <span className={cn("mt-1.5 inline-block rounded-full px-2 py-0.5 text-[10px] font-semibold", ROLE_COLOR[user.role])}>
              {ROLE_LABEL[user.role]}
            </span>
          </div>
        )}

        {/* Nav */}
        <nav className="flex-1 space-y-0.5 overflow-y-auto px-3 py-4">
          {visibleNav.map(({ href, label, icon: Icon }) => {
            const active = pathname === href || (href !== ROUTES.DASHBOARD && pathname.startsWith(href));
            return (
              <Link
                key={href}
                href={href}
                className={cn(
                  "flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors",
                  active
                    ? "bg-blue-50 text-blue-700 dark:bg-blue-950 dark:text-blue-400"
                    : "text-slate-600 hover:bg-slate-100 hover:text-slate-900 dark:text-slate-400 dark:hover:bg-slate-800 dark:hover:text-white",
                )}
              >
                <Icon className={cn("h-4 w-4 shrink-0", active ? "text-blue-600" : "")} />
                {label}
              </Link>
            );
          })}
        </nav>

        {/* Logout */}
        <div className="border-t border-slate-200 p-3 dark:border-slate-800">
          <button
            onClick={() => logout()}
            className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-slate-500 transition-colors hover:bg-red-50 hover:text-red-600 dark:text-slate-400 dark:hover:bg-red-950 dark:hover:text-red-400"
          >
            <LogOut className="h-4 w-4 shrink-0" />
            Cerrar sesión
          </button>
        </div>
      </aside>

      {/* Main */}
      <main className="flex-1 overflow-y-auto">
        {children}
      </main>
    </div>
  );
}
