import { useQuery } from "@tanstack/react-query";
import { getCurrentUser } from "../api/auth";

export function useAuth() {
  const { data, isLoading } = useQuery({
    queryKey: ["me"],
    queryFn: getCurrentUser,
    staleTime: 0,
    retry: false,
  });

  return { user: data, isLoading };
}
