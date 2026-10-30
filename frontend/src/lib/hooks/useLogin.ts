"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { getCurrentUser, homePathFor, login } from "../api/auth";
import { ApiError } from "../api/error";
import { toast } from "sonner";
import { LoginUserDto } from "../schema/authSchema";
import { queryKeys } from "../query-cache-key";

export function useLogin() {
  const queryClient = useQueryClient();
  const router = useRouter();

  return useMutation({
    mutationFn: async (values: LoginUserDto) => {
      await login(values);
      const user = await queryClient.query({
        queryKey: queryKeys.me,
        queryFn: getCurrentUser,
        staleTime: 0,
      });
      if (!user)
        throw new ApiError(
          "Signed in, but could not load your account.",
          500,
          "AUTH_INTERNAL_SERVER_ERROR",
        );
      return user;
    },
    onSuccess: (user) => router.replace(homePathFor(user.role)),
    onError: (error) => {
      if (error instanceof ApiError && error.status === 401) {
        toast.error("Sign in failed", {
          description: error.message || "Invalid credentials",
        });
      } else {
        toast.error("Sign in failed", { description: error.message });
      }
    },
  });
}
