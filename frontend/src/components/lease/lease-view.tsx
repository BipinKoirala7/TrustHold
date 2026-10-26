"use client";

import { useMemo, useState } from "react";
import { leases } from "@/lib/lease-data";
import { LeasesTable } from "@/components/lease/lease-table";
import { LeasesToolbar } from "@/components/lease/lease-toolbar";

export function LeasesView() {
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("all");

  const filtered = useMemo(() => {
    const query = search.trim().toLowerCase();

    return leases.filter((lease) => {
      const matchesStatus = status === "all" || lease.status === status;
      const matchesSearch =
        query === "" ||
        lease.propertyName.toLowerCase().includes(query) ||
        lease.propertyDetail.toLowerCase().includes(query) ||
        lease.tenantName.toLowerCase().includes(query);

      return matchesStatus && matchesSearch;
    });
  }, [search, status]);

  return (
    <div className="space-y-4">
      <LeasesToolbar
        search={search}
        onSearchChange={setSearch}
        status={status}
        onStatusChange={setStatus}
      />
      <LeasesTable leases={filtered} />
    </div>
  );
}
