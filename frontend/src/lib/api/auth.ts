import type { LoginUserDto, Role } from "@/lib/schema/authSchema";
import type { RegisterUerDto } from "@/lib/schema/authSchema";
import api from "./axios";
import { toast } from "sonner";

const REGISTER_PATH = "auth/register";

export async function registerUser(values: RegisterUerDto): Promise<void> {
  const response = await api.post(REGISTER_PATH, values);

  if (response.status === 409) {
    toast.error("Email already exists", {
      description: "Please use a different email address.",
    });
    return;
  }

  if (response.status === 400) {
    toast.error("Invalid registration data", {
      description:
        response.data?.message ?? "Please check your input and try again.",
    });
    return;
  }
}

const LOGIN_PATH = "auth/login";

export async function login(values: LoginUserDto): Promise<void> {
  const response = await api.post(LOGIN_PATH, values);

  if (response.status === 200) {
    toast.success("Logged in successfully", {
      description: "You are now logged in.",
    });
  } else {
    toast.error("Login failed", {
      description: "Please check your credentials and try again.",
    });
  }
}

const ME_PATH = "user/me";

export async function getCurrentUser(): Promise<User | null> {
  try {
    const res = await api.get(ME_PATH);
    return res.data.data;
  } catch (e) {
    if (e instanceof ApiError && e.status === 401) return null; // logged out, not an error
    throw e;
  }
}

// Helper functions
export const homePathFor = (role: Role) => `/${role.toLowerCase()}`;
