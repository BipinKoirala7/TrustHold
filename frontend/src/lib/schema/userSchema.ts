import z from "zod";

export interface PublicUserDto {
  id: string;
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  phone: string;
  role: Role;
  authProvider: AuthProvider;
  lastLogin: string | null; // ISO 8601 Instant
  createdAt: string; // ISO 8601 Instant
  updatedAt: string; // ISO 8601 Instant
}

export type Role = "TENANT" | "LANDLORD" | "ADMIN";
export type AuthProvider = "LOCAL" | "GOOGLE";

const updateUserSchema = z.object({
  firstName: z.string().trim().min(2, "Please enter your first name"),
  lastName: z.string().trim().min(2, "Please enter your last name"),
  username: z
    .string()
    .trim()
    .min(2, "Please enter a username")
    .regex(
      /^[a-z0-9_]+$/,
      "Username can only contain lowercase letters, numbers and underscores",
    ),
  phone: z
    .string()
    .trim()
    .min(1, "Please enter your phone number")
    .regex(/^\+[1-9]\d{1,14}$/, "Please enter a valid phone number"),
});

export type UpdateUserDto = z.infer<typeof updateUserSchema>;
