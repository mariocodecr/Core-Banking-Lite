"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { authService } from "@/services/auth.service";
import { clearTokens, setTokens } from "@/lib/auth-storage";
import type { LoginRequest } from "@/types/auth.types";
import { ROUTES } from "@/constants";

export function useLogin() {
  const router = useRouter();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: LoginRequest) => authService.login(data),
    onSuccess: (response) => {
      setTokens(response.accessToken, response.refreshToken);
      queryClient.clear();
      toast.success(`Bienvenido, ${response.user.fullName}`);
      router.push(ROUTES.DASHBOARD);
    },
    onError: () => {
      toast.error("Credenciales incorrectas. Verificá tu email y contraseña.");
    },
  });
}

export function useLogout() {
  const router = useRouter();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => authService.logout(),
    onSettled: () => {
      clearTokens();
      queryClient.clear();
      router.push(ROUTES.LOGIN);
    },
  });
}
