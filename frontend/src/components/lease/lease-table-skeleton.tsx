import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Skeleton } from "@/components/ui/skeleton";

const headClass =
  "text-xs font-semibold tracking-wide text-muted-foreground uppercase";

export function LeasesTableSkeleton({ rows = 6 }: { rows?: number }) {
  return (
    <div
      className="overflow-hidden rounded-xl border bg-card shadow-sm"
      aria-busy="true"
      aria-label="Loading leases"
    >
      <Table>
        <TableHeader className="bg-muted/40">
          <TableRow className="hover:bg-transparent">
            <TableHead className={headClass}>Property</TableHead>
            <TableHead className={`${headClass} hidden md:table-cell`}>
              Tenant
            </TableHead>
            <TableHead className={headClass}>Deposit</TableHead>
            <TableHead className={headClass}>Status</TableHead>
            <TableHead className={`${headClass} hidden lg:table-cell`}>
              Start date
            </TableHead>
            <TableHead className="w-10" />
          </TableRow>
        </TableHeader>
        <TableBody>
          {Array.from({ length: rows }).map((_, i) => (
            <TableRow key={i} className="hover:bg-transparent">
              <TableCell className="py-3">
                <Skeleton className="h-4 w-36" />
                <Skeleton className="mt-1.5 h-3 w-20" />
              </TableCell>
              <TableCell className="hidden md:table-cell">
                <Skeleton className="h-4 w-28" />
              </TableCell>
              <TableCell>
                <Skeleton className="h-4 w-20" />
              </TableCell>
              <TableCell>
                <Skeleton className="h-5 w-20 rounded-full" />
              </TableCell>
              <TableCell className="hidden lg:table-cell">
                <Skeleton className="h-4 w-24" />
              </TableCell>
              <TableCell className="w-10">
                <Skeleton className="ml-auto size-4" />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
