import { z } from "zod";

export const loginSchema = z.object({
  email: z.email({ message: "Please enter a valid email address." }),
  password: z
    .string({ message: "Password is required." })
    .min(8, "Password must be at least 8 characters long"),
});

export type LoginValues = z.infer<typeof loginSchema>;
