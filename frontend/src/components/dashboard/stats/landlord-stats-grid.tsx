"use client";

import {
  getLandlordDashboardSummaryStats,
  LandlordDashboardSummaryDto,
} from "@/lib/api/landlord";
import { useQuery } from "@tanstack/react-query";
import { cn } from "@/lib/utils";
import {
  AlertCircle,
  Building2,
  CheckCircle2,
  Clock,
  Wallet,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { LandlordStatsSkeleton } from "./landlord-stats-skeleton";

const tones: Record<string, string> = {
  primary: "bg-primary/10 text-primary",
  amber: "bg-amber-100 text-amber-600",
};

export function LandlordStatsGrid() {
  const { data, isPending, isError, refetch, isFetching } =
    useQuery<LandlordDashboardSummaryDto>({
      queryKey: ["landlord-dashboard-summary-stats"],
      queryFn: getLandlordDashboardSummaryStats,
    });

  if (isPending) {
    return <LandlordStatsSkeleton />;
  }

  if (isError || data === undefined) {
    return (
      <div
        role="alert"
        className="flex items-center justify-between gap-3 rounded-2xl border border-destructive/20 bg-destructive/5 px-4 py-3"
      >
        <div className="flex items-center gap-2 text-sm text-destructive">
          <AlertCircle className="size-4 shrink-0" />
          <span>
            Couldn&apos;t load your dashboard stats. Please try again.
          </span>
        </div>
        <Button
          variant="outline"
          size="sm"
          onClick={() => refetch()}
          disabled={isFetching}
        >
          {isFetching ? "Retrying..." : "Retry"}
        </Button>
      </div>
    );
  }

  return (
    <div className={`grid gap-2 sm:grid-cols-2 xl:grid-cols-4`}>
      {/* Active Leases */}
      <div className="rounded-2xl border bg-card p-6 shadow-sm">
        <div className="flex items-start justify-between">
          <p className="text-sm text-muted-foreground">Active Leases</p>
          <div
            className={cn(
              "flex size-10 items-center justify-center rounded-xl",
              tones.primary,
            )}
          >
            <Building2 className="size-5" />
          </div>
        </div>
        <p className="mt-6 text-3xl font-bold tracking-tight">
          {data.activeLeases}
        </p>
        <p className="mt-2 text-sm text-muted-foreground">
          {data.moveOutThisMonth} move-outs this month
        </p>
      </div>

      {/* Held in Escrow */}
      <div className="rounded-2xl border bg-card p-6 shadow-sm">
        <div className="flex items-start justify-between">
          <p className="text-sm text-muted-foreground">Held in escrow</p>
          <div
            className={cn(
              "flex size-10 items-center justify-center rounded-xl",
              tones.primary,
            )}
          >
            <Wallet className="size-5" />
          </div>
        </div>
        <p className="mt-6 text-3xl font-bold tracking-tight">
          Rs. {data.totalAmountHeldInEscrow}
        </p>
        <p className="mt-2 text-sm text-muted-foreground">
          Across {data.escrowDepositCount} deposits
        </p>
      </div>

      {/* Awaiting Payment */}
      <div className="rounded-2xl border bg-card p-6 shadow-sm">
        <div className="flex items-start justify-between">
          <p className="text-sm text-muted-foreground">Awaiting payment</p>
          <div
            className={cn(
              "flex size-10 items-center justify-center rounded-xl",
              tones.primary,
            )}
          >
            <Clock className="size-5" />
          </div>
        </div>
        <p className="mt-6 text-3xl font-bold tracking-tight">
          {data.numbOfAwaitingMyAgreement}
        </p>
        <p className="mt-2 text-sm text-muted-foreground">
          Rs. {data.totalPendingAmount} pending
        </p>
      </div>

      {/* Awaiting My Agreement */}
      <div className="rounded-2xl border bg-card p-6 shadow-sm">
        <div className="flex items-start justify-between">
          <p className="text-sm text-muted-foreground">Awaiting my agreement</p>
          <div
            className={cn(
              "flex size-10 items-center justify-center rounded-xl",
              tones.amber,
            )}
          >
            <CheckCircle2 className="size-5" />
          </div>
        </div>
        <p className="mt-6 text-3xl font-bold tracking-tight">
          {data.numbOfAwaitingMyAgreement}
        </p>
        <p className="mt-2 text-sm text-muted-foreground">Review settlement</p>
      </div>
    </div>
  );
}
