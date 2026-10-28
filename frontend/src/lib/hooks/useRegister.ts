"use client";

import { useMutation } from "@tanstack/react-query";
import { registerUser } from "../api/auth";
import { RegisterUerDto } from "../schema/authSchema";
import { ApiError } from "../api/error";
import { toast } from "sonner";
import { useRouter } from "next/navigation";

export function useRegister() {
  const router = useRouter();
  return useMutation({
    mutationFn: async (values: RegisterUerDto) => {
      await registerUser(values);
    },
    onSuccess: () => {
      toast.success("Registration successful", {
        description:
          "Your account has been created. Please check your email for confirmation.",
      });
      router.push("/login");
    },
    onError: (error) => {
      if (error instanceof ApiError && error.status === 409) {
        toast.error("Registration failed", {
          description: "An account with this email or username already exists.",
        });
      } else if (error instanceof ApiError && error.status === 422) {
        toast.error("Registration failed", {
          description: "Invalid input. Please check your data and try again.",
        });
      } else {
        toast.error("Registration failed", { description: error.message });
      }
    },
  });
}
