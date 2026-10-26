import { z } from "zod";

export const registrationRoles = ["LANDLORD", "TENANT"] as const;
export type RegistrationRole = (typeof registrationRoles)[number];

export const registerSchema = z
  .object({
    role: z.enum(registrationRoles),
    fullName: z.string().trim().min(2, "Please enter your full name"),
    email: z.email({ message: "Please enter a valid email address." }),
    password: z.string().min(8, "Password must be at least 8 characters long"),
    confirmPassword: z.string().min(1, "Please confirm your password"),
  })
  .refine((values) => values.password === values.confirmPassword, {
    message: "Passwords don't match",
    path: ["confirmPassword"],
  });

export type RegisterValues = z.infer<typeof registerSchema>;

export const loginSchema = z.object({
  email: z.email({ message: "Please enter a valid email address." }),
  password: z
    .string({ message: "Password is required." })
    .min(8, "Password must be at least 8 characters long"),
});

export type LoginValues = z.infer<typeof loginSchema>;
