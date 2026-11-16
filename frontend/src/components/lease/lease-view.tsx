"use client";

import { useState } from "react";
import { LeasesTable } from "@/components/lease/lease-table";
import { LeasesToolbar } from "@/components/lease/lease-toolbar";
import { getLeases, LeaseSummaryDto } from "@/lib/api/lease";
import { useQuery } from "@tanstack/react-query";

export function LeasesView() {
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("all");

  const { isPending, data, isError } = useQuery<LeaseSummaryDto[]>({
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
      {isPending && <p>Loading...</p>}
      {isError && <p>Error occurred while fetching leases.</p>}
      {data && <LeasesTable leases={data} />}
    </div>
  );
}
