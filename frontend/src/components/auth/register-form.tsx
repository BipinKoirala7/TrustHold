"use client";

import { useRouter } from "next/navigation";
import { Controller, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Field, FieldError, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Spinner } from "@/components/ui/spinner";
import { GoogleButton } from "@/components/auth/google-button";
import { OrDivider } from "@/components/auth/or-divider";
import { PasswordInput } from "@/components/auth/password-input";
import { RoleSelector } from "@/components/auth/role-selector";
import { registerUser } from "@/lib/api/auth";
import {
  registerSchema,
  type RegisterValues,
  type RegistrationRole,
} from "@/lib/schema/auth";

const inputClass = "h-12 bg-card px-4 text-base md:text-base border-border";

// The role is passed along so the backend knows which kind of account a Google
// sign-up should create. Spring Security does not forward custom query params
// on its own: the backend needs a custom OAuth2AuthorizationRequestResolver
// (or similar) to keep "role" until the callback.
function googleSignupUrl(role: RegistrationRole) {
  const base = process.env.NEXT_PUBLIC_API_URL ?? "";
  return `${base}/oauth2/authorization/google?role=${role}`;
}

export function RegisterForm() {
  const router = useRouter();

  const {
    register,
    handleSubmit,
    control,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<RegisterValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      role: "LANDLORD",
      fullName: "",
      email: "",
      password: "",
      confirmPassword: "",
    },
  });

  const role = watch("role");

  async function onSubmit(values: RegisterValues) {
    try {
      await registerUser(values);
      toast.success("Account created", {
        description: "Log in with your new account to continue.",
      });
      router.push("/login");
    } catch (error) {
      if (error instanceof TypeError) {
        toast.error("Can't reach the server", {
          description: "Check your connection and try again.",
        });
        return;
      }

      toast.error("Couldn't create your account", {
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
      <Controller
        control={control}
        name="role"
        render={({ field }) => (
          <RoleSelector
            name={field.name}
            value={field.value}
            onChange={field.onChange}
            disabled={isSubmitting}
          />
        )}
      />

      <GoogleButton label="Sign up with Google" href={googleSignupUrl(role)} />

      <OrDivider />

      <div className="flex flex-col gap-3">
        <Field>
          <FieldLabel htmlFor="fullName">Full name</FieldLabel>
          <Input
            id="fullName"
            autoComplete="name"
            placeholder="Your full name"
            aria-invalid={!!errors.fullName}
            className={inputClass}
            {...register("fullName")}
          />
          {errors.fullName && (
            <FieldError>{errors.fullName.message}</FieldError>
          )}
        </Field>

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

        <div className="grid gap-5 sm:grid-cols-2">
          <Field>
            <FieldLabel htmlFor="password">Password</FieldLabel>
            <PasswordInput
              id="password"
              autoComplete="new-password"
              aria-invalid={!!errors.password}
              className={inputClass}
              {...register("password")}
            />
            {errors.password && (
              <FieldError>{errors.password.message}</FieldError>
            )}
          </Field>

          <Field>
            <FieldLabel htmlFor="confirmPassword">Confirm password</FieldLabel>
            <PasswordInput
              id="confirmPassword"
              autoComplete="new-password"
              aria-invalid={!!errors.confirmPassword}
              className={inputClass}
              {...register("confirmPassword")}
            />
            {errors.confirmPassword && (
              <FieldError>{errors.confirmPassword.message}</FieldError>
            )}
          </Field>
        </div>

        <Button
          type="submit"
          size="lg"
          disabled={isSubmitting}
          className="h-12 w-full rounded-xl text-base"
        >
          {isSubmitting ? (
            <>
              <Spinner />
              Creating account…
            </>
          ) : (
            "Create account"
          )}
        </Button>
      </div>
    </form>
  );
}
