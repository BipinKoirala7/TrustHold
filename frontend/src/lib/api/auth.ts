import type { LoginUserDto, Role } from "@/lib/schema/authSchema";
import type { RegisterUerDto } from "@/lib/schema/authSchema";
import api from "./axios";
import { ApiError } from "./error";
import { PublicUserDto } from "../schema/userSchema";

const REGISTER_PATH = "auth/register";

export async function registerUser(values: RegisterUerDto): Promise<void> {
  await api.post(REGISTER_PATH, values);
}

const LOGIN_PATH = "auth/login";

export async function login(values: LoginUserDto): Promise<void> {
  await api.post(LOGIN_PATH, values);
}

const ME_PATH = "user/me";

export async function getCurrentUser(): Promise<PublicUserDto | null> {
  try {
    const res = await api.get(ME_PATH);
    return res.data.data;
  } catch (e) {
    if (e instanceof ApiError && e.status === 401) return null;
    throw e;
  }
}

export async function forgotPassword(email: string): Promise<void> {
  await api.post("auth/forgot-password", { email });
}

export async function resetPassword(
  token: string,
  password: string,
): Promise<void> {
  await api.post("auth/reset-password", {
    rawToken: token,
    newPassword: password,
  });
}

// Helper functions
export const homePathFor = (role: Role) => `/${role.toLowerCase()}`;
