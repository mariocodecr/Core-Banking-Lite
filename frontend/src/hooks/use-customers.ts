import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { customerService } from "@/services/customer.service";
import type { CustomerFilterParams, CustomerRequest } from "@/types/customer.types";

const QUERY_KEY = "customers";

export function useCustomers(params: CustomerFilterParams) {
  return useQuery({
    queryKey: [QUERY_KEY, params],
    queryFn: () => customerService.findAll(params),
  });
}

export function useCustomer(id: string) {
  return useQuery({
    queryKey: [QUERY_KEY, id],
    queryFn: () => customerService.findById(id),
    enabled: !!id,
  });
}

export function useCreateCustomer(onSuccess?: () => void) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CustomerRequest) => customerService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
      toast.success("Cliente creado exitosamente");
      onSuccess?.();
    },
    onError: () => {
      toast.error("Error al crear el cliente. Verificá los datos ingresados.");
    },
  });
}

export function useUpdateCustomer(onSuccess?: () => void) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: CustomerRequest }) =>
      customerService.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
      toast.success("Cliente actualizado correctamente");
      onSuccess?.();
    },
    onError: () => {
      toast.error("Error al actualizar el cliente.");
    },
  });
}

export function useDeleteCustomer() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => customerService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEY] });
      toast.success("Cliente eliminado");
    },
    onError: () => {
      toast.error("Error al eliminar el cliente.");
    },
  });
}
