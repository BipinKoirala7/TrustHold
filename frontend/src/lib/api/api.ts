export type ApiResponse<T> = {
  data: T;
  timestamp: string; // ISO 8601 format
};

export type ApiErrorResponse = {
  error: {
    message: string;
    code: string;
  };
  timestamp: string; // ISO 8601 format
};
