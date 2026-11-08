"use client";

import { logoutUser } from "@/lib/api/auth";
import { queryKeys } from "@/lib/query-cache-key";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { toast } from "sonner";

export function useLogout() {
  const queryClient = useQueryClient();
  const router = useRouter();

  return useMutation({
    mutationFn: logoutUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.me });
      toast.success("Logged out successfully");
      router.replace("/login");
    },
    onError: (error) => {
      toast.error("Logout Failed", {
        description:
          error instanceof Error ? error.message : "Please try again!",
      });
    },
  });
}
