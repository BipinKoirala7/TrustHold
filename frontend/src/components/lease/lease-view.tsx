"use client";

import { useState } from "react";
import { LeasesTable } from "@/components/lease/lease-table";
import { LeasesTableSkeleton } from "@/components/lease/lease-table-skeleton";
import { LeasesTableError } from "@/components/lease/lease-table-error";
import { LeasesTableEmpty } from "@/components/lease/lease-table-empty";
import { LeasesToolbar } from "@/components/lease/lease-toolbar";
import { getLeases, LeaseSummaryDto } from "@/lib/api/lease";
import { useQuery } from "@tanstack/react-query";

export function LeasesView() {
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("all");

  const { isPending, isError, data, refetch, isFetching } = useQuery<
    LeaseSummaryDto[]
  >({
    queryKey: ["leases"],
    queryFn: getLeases,
  });

  return (
    <div className="space-y-4">
      <LeasesToolbar
        search={search}
        onSearchChange={setSearch}
        status={status}
        onStatusChange={setStatus}
      />
      {isPending ? (
        <LeasesTableSkeleton />
      ) : isError ? (
        <LeasesTableError onRetry={() => refetch()} isRetrying={isFetching} />
      ) : data.length === 0 ? (
        <LeasesTableEmpty />
      ) : (
        <LeasesTable leases={data} />
      )}
    </div>
  );
}
