import { ApiError, handleError } from "@/lib/api/error";
import { createProperty, CreatePropertyDto } from "@/lib/api/property";
import { queryKeys } from "@/lib/query-cache-key";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

export function useCreateProperty(afterSuccess: () => void) {
  const queryClient = useQueryClient();
  return useMutation<void, ApiError, CreatePropertyDto>({
    mutationFn: createProperty,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: queryKeys.properties });
      toast.success("Property created");
      afterSuccess();
    },
    onError: (error: ApiError) =>
      handleError(error, "Could not create property"),
  });
}
