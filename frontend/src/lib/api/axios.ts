import axios from "axios";
import { ApiError } from "./error";

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
    if (axios.isAxiosError(error)) {
      if (!error.response) {
        return Promise.reject(
          new ApiError(0, "Can't reach the server. Check your connection."),
        );
      }
      const message =
        error.response.data?.message ??
        "Something went wrong. Please try again.";
      return Promise.reject(new ApiError(error.response.status, message));
    }
    return Promise.reject(error);
  },
);

export default api;
