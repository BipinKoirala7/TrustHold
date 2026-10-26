import type { LoginValues } from "@/lib/schema/auth";
import type { RegisterValues } from "@/lib/schema/auth";

// Adjust to match the Spring Boot auth controller. With the Next.js rewrite
// proxy from earlier, "/api/*" is forwarded to the backend.
const LOGIN_PATH = "/api/auth/login";

export async function login(values: LoginValues): Promise<void> {
  const response = await fetch(LOGIN_PATH, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include", // backend sets the JWT cookie
    body: JSON.stringify(values),
  });

  if (response.status === 401 || response.status === 403) {
    throw new Error("Invalid email or password.");
  }

  if (!response.ok) {
    throw new Error("We couldn't sign you in right now. Please try again.");
  }
}

// Adjust the path and field names to match the Spring Boot registration DTO.
const REGISTER_PATH = "/api/auth/register";

export async function registerUser(values: RegisterValues): Promise<void> {
  // confirmPassword is a frontend-only check; the backend never needs it.
  const { confirmPassword: _confirmPassword, ...payload } = values;

  const response = await fetch(REGISTER_PATH, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify(payload),
  });

  if (response.status === 409) {
    throw new Error("An account with this email already exists.");
  }

  if (response.status === 400) {
    throw new Error("Please check your details and try again.");
  }

  if (!response.ok) {
    throw new Error(
      "We couldn't create your account right now. Please try again.",
    );
  }
}
