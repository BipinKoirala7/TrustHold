import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";
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
const NO_REFRESH_URLS = ["/auth/refresh", "/auth/login", "/auth/register"];

let refreshPromise: Promise<void> | null = null;

function refreshSession(): Promise<void> {
  if (!refreshPromise) {
    // Use bare axios so this call doesn't go through our interceptor
    refreshPromise = axios
      .post(
        `${process.env.NEXT_PUBLIC_API_URL}/auth/refresh`,
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

function toApiError(error: AxiosError<ApiErrorResponse>): ApiError {
  if (!error.response) {
    return new ApiError(error.message, 0, "INTERNAL_SERVER_ERROR");
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
    if (!axios.isAxiosError<ApiErrorResponse>(error)) {
      return Promise.reject(error);
    }

    const original = error.config as RetryableConfig | undefined;
    const status = error.response?.status;

    const shouldRefresh =
      status === 401 &&
      original &&
      !original._retry &&
      !NO_REFRESH_URLS.some((url) => original.url?.includes(url));

    if (shouldRefresh) {
      original._retry = true;
      try {
        await refreshSession();
        return api(original); // retry the original request
      } catch (refreshError) {
        // Refresh failed -> session is really gone
        if (axios.isAxiosError<ApiErrorResponse>(refreshError)) {
          return Promise.reject(toApiError(refreshError));
        }
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(toApiError(error));
  },
);

export default api;
