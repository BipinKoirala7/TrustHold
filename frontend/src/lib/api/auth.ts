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

const OAUTH_ROLE_PATH = "auth/oauth/register";
export async function OAuthRoleRegistration(role: Role) {
  await api.post(OAUTH_ROLE_PATH, { role });
}

const FORGOT_PASSWORD_PATH = "auth/forgot-password";
export async function forgotPassword(email: string): Promise<void> {
  await api.post(FORGOT_PASSWORD_PATH, { email });
}

const RESET_PASSWORD_PATH = "auth/reset-password";
export async function resetPassword(
  token: string,
  password: string,
): Promise<void> {
  await api.post(RESET_PASSWORD_PATH, {
    rawToken: token,
    newPassword: password,
  });
}

const LOGOUT_PATH = "auth/logout";
export async function logoutUser(): Promise<void> {
  await api.post(LOGOUT_PATH);
}

// Helper functions
export const homePathFor = (role: Role) => `/${role.toLowerCase()}`;
