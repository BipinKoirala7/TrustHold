"use client";

import { useMutation } from "@tanstack/react-query";
import { registerUser } from "@/lib/api/auth";
import { RegisterUerDto } from "@/lib/schema/authSchema";
import { ApiError, handleError } from "@/lib/api/error";
import { toast } from "sonner";
import { useRouter } from "next/navigation";

export function useRegister() {
  const router = useRouter();
  return useMutation<void, ApiError, RegisterUerDto>({
    mutationFn: registerUser,
    onSuccess: () => {
      toast.success("Registration successful", {
        description:
          "Your account has been created. Please check your email for confirmation.",
      });
      router.push("/login");
    },
    onError: (error: ApiError) => handleError(error, "Registration failed"),
  });
}
