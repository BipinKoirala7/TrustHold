import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { LeaseRow } from "./lease-row";
import { LeaseSummaryDto } from "@/lib/schema/leaseSchema";

const headClass =
  "text-xs font-semibold tracking-wide text-muted-foreground uppercase";

export function LeasesTable({ leases }: { leases: LeaseSummaryDto[] }) {
  return (
    <div className="overflow-hidden rounded-xl border bg-card shadow-sm">
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
          {leases.map((lease) => (
            <LeaseRow key={lease.id} lease={lease} />
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
