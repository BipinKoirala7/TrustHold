import type { LoginValues } from "@/lib/schema/authSchema";
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

export async function login(values: LoginValues): Promise<void> {
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
