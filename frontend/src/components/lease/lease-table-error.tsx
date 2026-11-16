import { AlertCircle, RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";

export function LeasesTableError({
  onRetry,
  isRetrying = false,
}: {
  onRetry: () => void;
  isRetrying?: boolean;
}) {
  return (
    <div
      role="alert"
      className="flex flex-col items-center justify-center gap-3 rounded-xl border border-dashed bg-card px-6 py-14 text-center shadow-sm"
    >
      <div className="flex size-11 items-center justify-center rounded-full bg-destructive/10">
        <AlertCircle className="size-5 text-destructive" />
      </div>
      <div className="space-y-1">
        <h3 className="text-sm font-semibold">
          Couldn&apos;t load your leases
        </h3>
        <p className="max-w-sm text-sm text-muted-foreground">
          Something went wrong while fetching your leases. Check your connection
          and try again.
        </p>
      </div>
      <Button
        variant="outline"
        size="sm"
        onClick={onRetry}
        disabled={isRetrying}
      >
        <RefreshCw className={isRetrying ? "animate-spin" : ""} />
        {isRetrying ? "Retrying..." : "Try again"}
      </Button>
    </div>
  );
}
