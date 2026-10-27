"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { getCurrentUser, homePathFor, login } from "../api/auth";
import { ApiError } from "../api/error";
import { toast } from "sonner";
import { LoginUserDto } from "../schema/authSchema";

export function useLogin() {
  const queryClient = useQueryClient();
  const router = useRouter();

  return useMutation({
    mutationFn: async (values: LoginUserDto) => {
      await login(values);
      const user = await queryClient.query({
        queryKey: ["me"],
        queryFn: getCurrentUser,
        staleTime: 0,
      });
      if (!user) throw new ApiError(401, "Could not load your account.");
      return user;
    },
    onSuccess: (user) => router.replace(homePathFor(user.role)),
    onError: (error) => {
      if (error instanceof ApiError && error.status === 401) {
        toast.error("Sign in failed", {
          description: "Incorrect email or password.",
        });
      } else {
        toast.error("Sign in failed", { description: error.message });
      }
    },
  });
}
