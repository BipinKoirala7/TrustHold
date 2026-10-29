import { toast } from "sonner";

export class ApiError extends Error {
  constructor(
    message: string,
    public status: number,
    public code: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

export function handleErrorToast(error: any) {
  if (error instanceof ApiError && error.status === 409) {
    toast.error("Registration failed", {
      description: "An account with this email or username already exists.",
    });
  } else if (error instanceof ApiError && error.status === 422) {
    toast.error("Registration failed", {
      description: "Invalid input. Please check your data and try again.",
    });
  } else {
    toast.error("Registration failed", { description: error.message });
  }
}
