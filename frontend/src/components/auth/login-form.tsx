"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Spinner } from "@/components/ui/spinner";
import { PasswordInput } from "@/components/auth/password-input";
import { login } from "@/lib/api/auth";
import { loginSchema, type LoginValues } from "@/lib/schema/auth";

const inputClass =
  "h-12 rounded-xl bg-card px-4 text-base md:text-base border-border";

export function LoginForm() {
  const router = useRouter();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<LoginValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
  });

  async function onSubmit(values: LoginValues) {
    try {
      await login(values);
      // TODO: redirect by role once the dashboards exist.
      router.push("/dashboard");
      router.refresh();
    } catch (error) {
      // fetch() rejects with a TypeError when the server can't be reached.
      if (error instanceof TypeError) {
        toast.error("Can't reach the server", {
          description: "Check your connection and try again.",
        });
        return;
      }

      toast.error("Sign in failed", {
        description:
          error instanceof Error
            ? error.message
            : "Something went wrong. Please try again.",
      });
    }
  }

  return (
    <form
      noValidate
      onSubmit={handleSubmit(onSubmit)}
      className="flex flex-col gap-5"
    >
      {/* No data-invalid on Field: that is what turns the label red.
          The red border comes from aria-invalid on the input itself. */}
      <Field>
        <FieldLabel htmlFor="email">Email address</FieldLabel>
        <Input
          id="email"
          type="email"
          autoComplete="email"
          placeholder="you@example.com"
          aria-invalid={!!errors.email}
          className={inputClass}
          {...register("email")}
        />
        {errors.email && <FieldError>{errors.email.message}</FieldError>}
      </Field>

      <Field>
        <div className="flex items-center justify-between">
          <FieldLabel htmlFor="password">Password</FieldLabel>
          <Link
            href="/forgot-password"
            className="text-sm font-semibold text-primary underline-offset-4 hover:underline"
          >
            Forgot password?
          </Link>
        </div>
        <PasswordInput
          id="password"
          autoComplete="current-password"
          aria-invalid={!!errors.password}
          className={inputClass}
          {...register("password")}
        />
        {errors.password && <FieldError>{errors.password.message}</FieldError>}
      </Field>

      <Button
        type="submit"
        size="lg"
        disabled={isSubmitting}
        className="h-12 w-full rounded-xl text-base"
      >
        {isSubmitting ? (
          <>
            <Spinner />
            Signing in…
          </>
        ) : (
          "Sign in"
        )}
      </Button>
    </form>
  );
}
