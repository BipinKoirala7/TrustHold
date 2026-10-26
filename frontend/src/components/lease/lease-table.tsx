import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import type { Lease } from "@/lib/lease-data";
import { LeaseRow } from "./lease-row";

const headClass =
  "text-xs font-semibold tracking-wide text-muted-foreground uppercase";

export function LeasesTable({ leases }: { leases: Lease[] }) {
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
          {leases.length === 0 ? (
            <TableRow className="hover:bg-transparent">
              <TableCell
                colSpan={6}
                className="h-32 text-center text-muted-foreground"
              >
                No leases match your search.
              </TableCell>
            </TableRow>
          ) : (
            leases.map((lease) => <LeaseRow key={lease.id} lease={lease} />)
          )}
        </TableBody>
      </Table>
    </div>
  );
}
