import axios from "axios";
import { ApiError } from "./error";
import { ApiErrorResponse } from "./api";

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.response.use(
  (res) => res,
  (error) => {
    if (axios.isAxiosError<ApiErrorResponse>(error)) {
      if (!error.response) {
        return Promise.reject(
          new ApiError(error.message, 0, "INTERNAL_SERVER_ERROR"),
        );
      }
      const { status, data } = error.response;
      return Promise.reject(
        new ApiError(
          data.error.message ?? "Something went wrong. Please try again.",
          status,
          data.error.code,
        ),
      );
    }
    return Promise.reject(error);
  },
);

export default api;
