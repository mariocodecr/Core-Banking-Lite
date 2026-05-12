"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Loader2, Lock, Mail, ShieldCheck } from "lucide-react";
import { useLogin } from "@/hooks/use-auth";

const loginSchema = z.object({
  email: z.string().min(1, "El email es requerido").email("Formato de email inválido"),
  password: z.string().min(1, "La contraseña es requerida"),
});

type LoginFormData = z.infer<typeof loginSchema>;

export default function LoginPage() {
  const { mutate: login, isPending } = useLogin();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = (data: LoginFormData) => login(data);

  return (
    <div className="flex min-h-screen">
      {/* ── Left panel: brand ─────────────────────────────────── */}
      <div className="relative hidden w-1/2 flex-col justify-between overflow-hidden bg-slate-950 p-12 lg:flex">
        {/* Subtle grid */}
        <div
          className="absolute inset-0 opacity-[0.04]"
          style={{
            backgroundImage:
              "linear-gradient(rgba(255,255,255,1) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,1) 1px, transparent 1px)",
            backgroundSize: "48px 48px",
          }}
        />
        {/* Glow */}
        <div className="absolute left-1/4 top-1/3 h-80 w-80 -translate-x-1/2 -translate-y-1/2 rounded-full bg-blue-600/20 blur-[100px]" />

        {/* Logo */}
        <div className="relative flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-600">
            <ShieldCheck className="h-5 w-5 text-white" />
          </div>
          <span className="text-lg font-semibold tracking-tight text-white">Core Banking Lite</span>
        </div>

        {/* Headline */}
        <div className="relative space-y-6">
          <p className="text-xs font-medium uppercase tracking-[0.2em] text-blue-400">
            Plataforma bancaria enterprise
          </p>
          <h1 className="text-5xl font-bold leading-[1.08] tracking-tight text-white">
            Finanzas modernas,
            <br />
            <span className="text-blue-400">arquitectura</span>
            <br />
            enterprise.
          </h1>
          <p className="max-w-sm text-sm leading-relaxed text-slate-400">
            Gestión integral de cuentas, transferencias y auditoría financiera con los más altos
            estándares de seguridad bancaria.
          </p>
        </div>

        {/* Stats */}
        <div className="relative grid grid-cols-3 gap-6 border-t border-white/10 pt-8">
          {[
            { value: "256-bit", label: "Encriptación" },
            { value: "99.9%", label: "Uptime SLA" },
            { value: "ISO 27001", label: "Certificación" },
          ].map(({ value, label }) => (
            <div key={label}>
              <p className="text-xl font-bold text-white">{value}</p>
              <p className="mt-1 text-xs text-slate-500">{label}</p>
            </div>
          ))}
        </div>
      </div>

      {/* ── Right panel: form ─────────────────────────────────── */}
      <div className="flex w-full flex-col items-center justify-center bg-white px-6 lg:w-1/2 lg:px-16 dark:bg-slate-900">
        {/* Mobile logo */}
        <div className="mb-10 flex items-center gap-2 lg:hidden">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-blue-600">
            <ShieldCheck className="h-4 w-4 text-white" />
          </div>
          <span className="font-semibold text-slate-900 dark:text-white">Core Banking Lite</span>
        </div>

        <div className="w-full max-w-sm">
          <div className="mb-8">
            <h2 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-white">
              Iniciar sesión
            </h2>
            <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
              Accedé con tus credenciales institucionales
            </p>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-5">
            {/* Email */}
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                Email institucional
              </label>
              <div className="relative">
                <Mail className="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input
                  {...register("email")}
                  type="email"
                  autoComplete="email"
                  placeholder="usuario@corebanking.com"
                  className="w-full rounded-xl border border-slate-200 bg-slate-50 py-3 pl-10 pr-4 text-sm text-slate-900 placeholder-slate-400 transition-colors focus:border-blue-500 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 disabled:opacity-50 dark:border-slate-700 dark:bg-slate-800 dark:text-white dark:focus:border-blue-400 dark:focus:bg-slate-800"
                  disabled={isPending}
                />
              </div>
              {errors.email && (
                <p className="text-xs text-red-500">{errors.email.message}</p>
              )}
            </div>

            {/* Password */}
            <div className="space-y-1.5">
              <label className="text-sm font-medium text-slate-700 dark:text-slate-300">
                Contraseña
              </label>
              <div className="relative">
                <Lock className="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input
                  {...register("password")}
                  type="password"
                  autoComplete="current-password"
                  placeholder="••••••••"
                  className="w-full rounded-xl border border-slate-200 bg-slate-50 py-3 pl-10 pr-4 text-sm text-slate-900 placeholder-slate-400 transition-colors focus:border-blue-500 focus:bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 disabled:opacity-50 dark:border-slate-700 dark:bg-slate-800 dark:text-white dark:focus:border-blue-400 dark:focus:bg-slate-800"
                  disabled={isPending}
                />
              </div>
              {errors.password && (
                <p className="text-xs text-red-500">{errors.password.message}</p>
              )}
            </div>

            {/* Submit */}
            <button
              type="submit"
              disabled={isPending}
              className="mt-2 flex w-full items-center justify-center gap-2 rounded-xl bg-blue-600 py-3 text-sm font-semibold text-white transition-all hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isPending ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  Verificando...
                </>
              ) : (
                "Ingresar"
              )}
            </button>
          </form>

          {/* Dev hint */}
          <div className="mt-8 rounded-xl border border-slate-100 bg-slate-50 p-4 dark:border-slate-700 dark:bg-slate-800/50">
            <p className="mb-2 text-xs font-medium uppercase tracking-wider text-slate-400">
              Credenciales de desarrollo
            </p>
            <div className="space-y-1">
              {[
                { email: "admin@corebanking.com", pass: "Admin1234!", role: "ADMIN" },
                { email: "advisor@corebanking.com", pass: "Advisor1234!", role: "ADVISOR" },
              ].map(({ email, role }) => (
                <div key={email} className="flex items-center justify-between">
                  <code className="text-xs text-slate-600 dark:text-slate-300">{email}</code>
                  <span className="rounded-full bg-blue-100 px-2 py-0.5 text-[10px] font-medium text-blue-700 dark:bg-blue-900/40 dark:text-blue-300">
                    {role}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>

        <p className="mt-12 text-xs text-slate-400">
          © {new Date().getFullYear()} Core Banking Lite. Todos los derechos reservados.
        </p>
      </div>
    </div>
  );
}
