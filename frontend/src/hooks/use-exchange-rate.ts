import { useQuery } from "@tanstack/react-query";
import { getExchangeRate } from "@/services/exchange-rate.service";

export function useExchangeRate(from: string | undefined, to: string | undefined) {
  return useQuery({
    queryKey: ["exchange-rate", from, to],
    queryFn: () => getExchangeRate(from!, to!),
    enabled: !!from && !!to && from !== to,
    staleTime: 4 * 60 * 60 * 1000, // 4 hours — matches backend cache TTL
  });
}
