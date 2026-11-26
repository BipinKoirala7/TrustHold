"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { ChevronRight } from "lucide-react";
import { TableCell, TableRow } from "@/components/ui/table";
import { LeaseStatusBadge } from "./lease-status-badge";
import { formatDeposit, formatLeaseDate } from "@/lib/api/lease";
import { LeaseSummaryDto } from "@/lib/schema/leaseSchema";

function getTimeline(startDate: string, endDate: string | null) {
  if (!endDate) {
    return formatLeaseDate(startDate) + " - " + "Present";
  }
  return `${formatLeaseDate(startDate)} - ${formatLeaseDate(endDate)}`;
}

export function LeaseRow({ lease }: { lease: LeaseSummaryDto }) {
  const router = useRouter();
  const href = `/landlord/leases/${lease.id}`;

  return (
    <TableRow onClick={() => router.push(href)} className="cursor-pointer">
      <TableCell className="py-3">
        <Link
          href={href}
          onClick={(e) => e.stopPropagation()}
          className="font-semibold hover:underline"
        >
          {lease.propertySummary.name}
        </Link>
        <p className="text-xs text-muted-foreground">
          {lease.propertySummary.city}
        </p>
      </TableCell>
      <TableCell className="hidden md:table-cell">
        {lease.tenantName ?? (
          <span className="text-muted-foreground">Awaiting Tenant</span>
        )}
      </TableCell>
      <TableCell className="font-semibold">
        {formatDeposit(lease.depositAmount)}
      </TableCell>
      <TableCell>
        <LeaseStatusBadge status={lease.status} />
      </TableCell>
      <TableCell className="hidden text-muted-foreground lg:table-cell">
        {getTimeline(lease.startDate, lease.endDate)}
      </TableCell>
      <TableCell className="w-10 text-right">
        <ChevronRight className="ml-auto size-4 text-muted-foreground" />
      </TableCell>
    </TableRow>
  );
}
