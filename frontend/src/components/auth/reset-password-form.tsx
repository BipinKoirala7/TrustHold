"use client";

import {
  resetPasswordSchema,
  type ResetPasswordDto,
  type ResetPasswordValues,
} from "@/lib/schema/authSchema";
import { useSearchParams } from "next/navigation";
import { useForm } from "react-hook-form";
import { Field, FieldError, FieldLabel } from "../ui/field";
import { Input } from "../ui/input";
import { Button } from "../ui/button";
import { cn } from "cn";
import { buttonVariants } from "../ui/button";
import { toast } from "sonner";
import { resetPassword } from "@/lib/api/auth";
import { useMutation } from "@tanstack/react-query";
import { Spinner } from "../ui/spinner";
import { zodResolver } from "@hookform/resolvers/zod";
import Link from "next/link";
import { PasswordInput } from "./password-input";

const inputClass = "h-12 bg-card px-4 text-base md:text-base border-border";

export function ResetPasswordForm() {
  const searchParams = useSearchParams();
  const token = searchParams.get("token") ?? "";
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ResetPasswordValues, unknown, ResetPasswordDto>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: {
      token: searchParams.get("token") ?? "",
      password: "",
      confirmPassword: "",
    },
  });

  const { mutate, isPending } = useMutation({
    mutationFn: async (values: ResetPasswordDto) => {
      await resetPassword(values.token, values.password);
    },
    onSuccess: () => {
      toast.success("Password reset successfully. You can now log in.");
    },
    onError: (error) => {
      toast.error("Failed to reset password. Please try again.", {
        description: error instanceof Error ? error.message : "Unknown error",
      });
    },
  });

  if (!token) {
    return (
      <div className="flex flex-col gap-4">
        <p className="text-sm text-muted-foreground">
          This reset link is missing or invalid. Request a new one to continue.
        </p>
        <Link
          href="/forgot-password"
          className={cn(buttonVariants(), "h-12 w-full text-base")}
        >
          Request a new link
        </Link>
      </div>
    );
  }

  return (
    <form
      onSubmit={handleSubmit((values) => mutate(values))}
      noValidate
      className="flex flex-col gap-5"
    >
      <Field>
        <FieldLabel htmlFor="newPassword">New password</FieldLabel>
        <PasswordInput
          id="newPassword"
          autoComplete="new-password"
          placeholder="Enter your new password"
          aria-invalid={!!errors.password}
          className={inputClass}
          {...register("password")}
        />
        {errors.password && <FieldError>{errors.password.message}</FieldError>}
      </Field>

      <Field>
        <FieldLabel htmlFor="confirmPassword">Confirm password</FieldLabel>
        <PasswordInput
          id="confirmPassword"
          autoComplete="new-password"
          placeholder="Confirm your new password"
          aria-invalid={!!errors.confirmPassword}
          className={inputClass}
          {...register("confirmPassword")}
        />
        {errors.confirmPassword && (
          <FieldError>{errors.confirmPassword.message}</FieldError>
        )}
      </Field>
      <div className="flex flex-col gap-2">
        <Button type="submit" disabled={isPending} className="w-full text-base">
          {isPending ? (
            <>
              <Spinner />
              Resetting...
            </>
          ) : (
            "Reset password"
          )}
        </Button>

        <Link
          href="/login"
          className={cn(buttonVariants({ variant: "secondary" }))}
        >
          Back to login
        </Link>
      </div>
    </form>
  );
}
