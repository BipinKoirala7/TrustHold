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

export function handleError(error: ApiError, errorTitle: string) {
  toast.error(errorTitle, { description: error.message });
}
