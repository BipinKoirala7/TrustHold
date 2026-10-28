"use client";

import { useRouter } from "next/navigation";
import { Controller, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
  Field,
  FieldError,
  FieldLabel,
  FieldDescription,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Spinner } from "@/components/ui/spinner";
import { GoogleButton } from "@/components/auth/google-button";
import { OrDivider } from "@/components/auth/or-divider";
import { PasswordInput } from "@/components/auth/password-input";
import { RoleSelector } from "@/components/auth/role-selector";
import {
  registerSchema,
  RegisterUerDto,
  type RegisterValues,
  type RegistrationRole,
} from "@/lib/schema/authSchema";
import { useRegister } from "@/lib/hooks/useRegister";

const inputClass = "h-12 bg-card px-4 text-base md:text-base border-border";

function googleSignupUrl(role: RegistrationRole) {
  const base = process.env.NEXT_PUBLIC_API_URL ?? "";
  return `${base}/oauth2/authorization/google?role=${role}`;
}

export function RegisterForm() {
  const { mutate, isPending } = useRegister();

  const {
    register,
    handleSubmit,
    control,
    watch,
    formState: { errors },
  } = useForm<RegisterValues, unknown, RegisterUerDto>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      registrationRole: "LANDLORD",
      firstName: "",
      lastName: "",
      username: "",
      phone: "",
      email: "",
      password: "",
      confirmPassword: "",
    },
  });

  const role = watch("registrationRole");

  return (
    <form
      noValidate
      onSubmit={handleSubmit((values) => mutate(values))}
      className="flex flex-col gap-5"
    >
      <Controller
        control={control}
        name="registrationRole"
        render={({ field }) => (
          <RoleSelector
            name={field.name}
            value={field.value}
            onChange={field.onChange}
            disabled={isPending}
          />
        )}
      />

      <GoogleButton label="Sign up with Google" href={googleSignupUrl(role)} />

      <OrDivider />

      <div className="flex flex-col gap-3">
        <Field>
          <FieldLabel htmlFor="username">Username</FieldLabel>
          <Input
            id="username"
            autoComplete="name"
            placeholder="Your full name"
            aria-invalid={!!errors.username}
            className={inputClass}
            {...register("username")}
          />
          {errors.username && (
            <FieldError>{errors.username.message}</FieldError>
          )}
        </Field>
        <div className="flex items-center justify-between gap-4">
          <Field>
            <FieldLabel htmlFor="firstName">First name</FieldLabel>
            <Input
              id="firstName"
              autoComplete="name"
              placeholder="Your full name"
              aria-invalid={!!errors.firstName}
              className={inputClass}
              {...register("firstName")}
            />
            {errors.firstName && (
              <FieldError>{errors.firstName.message}</FieldError>
            )}
          </Field>

          <Field>
            <FieldLabel htmlFor="lastName">Last name</FieldLabel>
            <Input
              id="lastName"
              autoComplete="name"
              placeholder="Your full name"
              aria-invalid={!!errors.lastName}
              className={inputClass}
              {...register("lastName")}
            />
            {errors.lastName && (
              <FieldError>{errors.lastName.message}</FieldError>
            )}
          </Field>
        </div>

        <Field>
          <FieldLabel htmlFor="phone">Phone number</FieldLabel>
          <Input
            id="phone"
            type="tel"
            autoComplete="tel"
            placeholder="+1 (555) 123-4567"
            aria-invalid={!!errors.phone}
            className={inputClass}
            {...register("phone")}
          />
          {errors.phone && <FieldError>{errors.phone.message}</FieldError>}
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
          disabled={isPending}
          className="h-12 w-full rounded-xl text-base"
        >
          {isPending ? (
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
