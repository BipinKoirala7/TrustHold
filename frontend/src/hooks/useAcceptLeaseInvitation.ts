import { ApiError, handleError } from "@/lib/api/error";
import { acceptLeaseInvitation } from "@/lib/api/lease";
import { useMutation } from "@tanstack/react-query";
import { toast } from "sonner";

export function useAcceptLeaseInvitation() {
  return useMutation<void, ApiError, string>({
    mutationFn: acceptLeaseInvitation,
    onSuccess: () => {
      toast.success("Lease invitation accepted successfully");
    },
    onError: (error) => handleError(error, "Failed to accept lease invitation"),
  });
}
