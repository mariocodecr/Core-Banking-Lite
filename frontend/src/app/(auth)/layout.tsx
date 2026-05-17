import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Acceso",
};

export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return <>{children}</>;
}
