import type { LoginValues } from "@/lib/schema/auth";

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
