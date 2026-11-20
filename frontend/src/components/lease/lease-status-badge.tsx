import { Badge } from "@/components/ui/badge";
import { LeaseStatus, leaseStatusMeta } from "@/lib/api/lease";
import { cn } from "@/lib/utils";

export function LeaseStatusBadge({ status }: { status: LeaseStatus }) {
  const meta = leaseStatusMeta[status];

  return (
    <Badge variant="outline" className={cn("font-medium", meta.className)}>
      {meta.label}
    </Badge>
  );
}
