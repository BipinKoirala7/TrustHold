import { ApiError, handleError } from "@/lib/api/error";
import { deleteProperty } from "@/lib/api/property";
import { queryKeys } from "@/lib/query-cache-key";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

export function useDeleteProperty(afterSuccess: () => void) {
  const queryClient = useQueryClient();
  return useMutation<void, ApiError, string>({
    mutationFn: deleteProperty,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: queryKeys.properties });
      toast.success("Property deleted");
      afterSuccess();
    },
    onError: (error) => handleError(error, "Could not delete property"),
  });
}
