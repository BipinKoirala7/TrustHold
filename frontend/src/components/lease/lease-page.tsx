"use client";

import { useAuth } from "@/hooks/useAuth";
import { LeasesView } from "./lease-view";
import { Spinner } from "../ui/spinner";
import { redirect } from "next/navigation";
import { buttonVariants } from "../ui/button";
import { Plus } from "lucide-react";
import Link from "next/link";

function LeasePage() {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="w-full h-screen flex items-center justify-center">
        <Spinner />
      </div>
    );
  }

  if (!user) {
    redirect("/login");
  }

  return (
    <div className="mx-auto max-w-7xl space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Leases</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            View your invitations, deposits, and lease progress.
          </p>
        </div>
        {user.role === "LANDLORD" && (
          <Link href="/leases/new" className={buttonVariants({ size: "sm" })}>
            <Plus className="mr-2 h-4 w-4" />
            New Lease
          </Link>
        )}
      </div>
      <LeasesView />
    </div>
  );
}

export default LeasePage;
