import { FileText } from "lucide-react";
import type { ReactNode } from "react";

export function LeasesTableEmpty({ action }: { action?: ReactNode }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 rounded-xl border border-dashed bg-card px-6 py-14 text-center shadow-sm">
      <div className="flex size-11 items-center justify-center rounded-full bg-muted">
        <FileText className="size-5 text-muted-foreground" />
      </div>
      <div className="space-y-1">
        <h3 className="text-sm font-semibold">No leases yet</h3>
        <p className="max-w-sm text-sm text-muted-foreground">
          Leases you create for your properties will show up here, along with
          their deposit and move-out status.
        </p>
      </div>
      {action}
    </div>
  );
}
