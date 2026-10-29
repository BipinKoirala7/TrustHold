"use client";

import { Button } from "@/components/ui/button";
import { useAuth } from "@/lib/hooks/useAuth";
import { redirect } from "next/navigation";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { updateUser } from "@/lib/api/user";
import { Spinner } from "../ui/spinner";
import { useForm } from "react-hook-form";
import { UpdateUserDto } from "@/lib/schema/userSchema";
import { Field, FieldError, FieldLabel } from "../ui/field";
import { Input } from "../ui/input";
import { queryKeys } from "@/lib/query-cache-key";
import { toast } from "sonner";

const inputClass = "h-12 bg-card px-4 text-base md:text-base border-border";

export function ProfileFormCard() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<UpdateUserDto>({
    defaultValues: {
      firstName: user?.firstName,
      lastName: user?.lastName,
      phone: user?.phone,
      username: user?.username,
    },
  });
  const { mutate, isPending } = useMutation({
    mutationFn: async (values: UpdateUserDto) => {
      await updateUser(values);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.me }),
    onError: (error) =>
      toast.error("Failed to update profile", { description: error.message }),
  });

  if (!user) return redirect("/login");
  return (
    <section className="flex flex-col gap-5 rounded-xl border bg-card p-6 shadow-sm">
      <div className="flex items-center gap-4 border-b pb-6">
        <div className="flex size-14 items-center justify-center rounded-full bg-primary/10 text-lg font-bold text-primary">
          {user.firstName[0].toUpperCase() + user.lastName[0].toUpperCase()}
        </div>
        <div>
          <p className="font-semibold">
            {user.firstName + " " + user.lastName}
          </p>
          <p className="text-sm text-muted-foreground">{user.email}</p>
        </div>
      </div>

      <form noValidate className="grid gap-5">
        <div className="grid gap-5 sm:grid-cols-2">
          <Field className="gap-2">
            <FieldLabel htmlFor="email">First name</FieldLabel>
            <Input
              id="firstName"
              type="text"
              autoComplete="firstName"
              aria-invalid={!!errors.firstName}
              className={inputClass}
              {...register("firstName")}
            />
            {errors.firstName && (
              <FieldError>{errors.firstName.message}</FieldError>
            )}
          </Field>
          <Field className="gap-2">
            <FieldLabel htmlFor="email">Last name</FieldLabel>
            <Input
              id="lastName"
              type="text"
              autoComplete="lastName"
              aria-invalid={!!errors.lastName}
              className={inputClass}
              {...register("lastName")}
            />
            {errors.lastName && (
              <FieldError>{errors.lastName.message}</FieldError>
            )}
          </Field>
          <Field className="gap-2">
            <FieldLabel htmlFor="email">Phone</FieldLabel>
            <Input
              id="phone"
              type="tel"
              autoComplete="tel"
              aria-invalid={!!errors.phone}
              className={inputClass}
              {...register("phone")}
            />
            {errors.phone && <FieldError>{errors.phone.message}</FieldError>}
          </Field>
          <Field className="gap-2">
            <FieldLabel htmlFor="email">Username</FieldLabel>
            <Input
              id="username"
              type="text"
              autoComplete="username"
              aria-invalid={!!errors.username}
              className={inputClass}
              {...register("username")}
            />
            {errors.username && (
              <FieldError>{errors.username.message}</FieldError>
            )}
          </Field>
        </div>
      </form>
      <div className="grid gap-5 sm:grid-cols-2">
        <Field className="gap-2">
          <FieldLabel htmlFor="email">Email</FieldLabel>
          <Input
            id="email"
            type="email"
            autoComplete="email"
            placeholder=""
            className={inputClass}
            value={user.email}
            disabled
          />
        </Field>
        <Field className="gap-2">
          <FieldLabel htmlFor="email">Account Role</FieldLabel>
          <Input
            id="role"
            type="text"
            autoComplete="role"
            className={inputClass}
            value={user.role}
            disabled
          />
        </Field>
      </div>
      <Button
        type="submit"
        className={"w-fit"}
        disabled={isPending}
        onClick={() => handleSubmit((values) => mutate(values))()}
      >
        {isPending ? (
          <>
            <Spinner />
            Saving...
          </>
        ) : (
          <>Save changes</>
        )}
      </Button>
    </section>
  );
}
