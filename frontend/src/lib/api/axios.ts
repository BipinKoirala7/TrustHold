import axios, { InternalAxiosRequestConfig } from "axios";
import { ApiError } from "./error";
import { ApiErrorResponse } from "./api";

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});

type RetryableConfig = InternalAxiosRequestConfig & { _retry?: boolean };

// Endpoints that must never trigger a refresh attempt
const NO_REFRESH_URLS = [
  "/auth/refresh",
  "/auth/login",
  "/auth/register",
  "/admin",
];

let refreshPromise: Promise<void> | null = null;

function refreshSession(): Promise<void> {
  if (!refreshPromise) {
    // Use bare axios so this call doesn't go through our interceptor
    refreshPromise = axios
      .post(
        `${process.env.NEXT_PUBLIC_API_URL}/auth/token/refresh`,
        {},
        { withCredentials: true },
      )
      .then(() => undefined)
      .finally(() => {
        refreshPromise = null;
      });
  }
  return refreshPromise;
}

function toApiError(error: unknown): ApiError {
  if (error instanceof ApiError) return error;

  if (!axios.isAxiosError<ApiErrorResponse>(error)) {
    return new ApiError(
      "Something went wrong. Please try again later.",
      0,
      "INTERNAL_SERVER_ERROR",
    );
  }

  if (!error.response) {
    return new ApiError(
      "Network error. Please check your connection and try again.",
      0,
      "NETWORK_ERROR",
    );
  }

  const { status, data } = error.response;
  return new ApiError(
    data?.error?.message ?? "Something went wrong. Please try again.",
    status,
    data?.error?.code,
  );
}

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const apiError = toApiError(error);
    const original = error?.config as RetryableConfig | undefined;

    const shouldRefresh =
      apiError.status === 401 &&
      original &&
      !original._retry &&
      !NO_REFRESH_URLS.some((url) => original.url?.includes(url));

    if (shouldRefresh) {
      original._retry = true;
      try {
        await refreshSession();
      } catch (refreshError) {
        // Refresh failed -> session is really gone
        throw toApiError(refreshError);
      }
      return api(original); // retry; its own failure goes through this interceptor again
    }

    throw apiError;
  },
);

export default api;
