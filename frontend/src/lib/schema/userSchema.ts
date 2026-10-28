export interface PublicUserDto {
  id: string;
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  role: Role;
  authProvider: AuthProvider;
  lastLogin: string | null; // ISO 8601 Instant
  createdAt: string; // ISO 8601 Instant
  updatedAt: string; // ISO 8601 Instant
}

export type Role = "TENANT" | "LANDLORD" | "ADMIN";
export type AuthProvider = "LOCAL" | "GOOGLE";
