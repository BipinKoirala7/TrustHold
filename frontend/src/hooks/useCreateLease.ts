import { ApiError, handleError } from "@/lib/api/error";
import { createLease } from "@/lib/api/lease";
import { queryKeys } from "@/lib/query-cache-key";
import { CreateLeaseDto } from "@/lib/schema/leaseSchema";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { useRouter } from "next/navigation";

export function useCreateLease() {
  const router = useRouter();
  const queryClient = useQueryClient();
  return useMutation<void, ApiError, CreateLeaseDto>({
    mutationFn: createLease,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.leases });
      toast.success("Lease created successfully");
      router.push("/lease")
    },
    onError: (error) => handleError(error, "Error creating lease"),
  });
}
