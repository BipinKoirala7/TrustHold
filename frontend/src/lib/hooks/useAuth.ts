import { useQuery } from "@tanstack/react-query";
import { getCurrentUser } from "../api/auth";
import { queryKeys } from "../query-cache-key";

export function useAuth() {
  const { data, isLoading } = useQuery({
    queryKey: queryKeys.me,
    queryFn: getCurrentUser,
    retry: false,
  });

  return { user: data, isLoading };
}
