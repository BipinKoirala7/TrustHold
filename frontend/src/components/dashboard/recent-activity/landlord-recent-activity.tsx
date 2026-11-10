"use client";

import { useQuery } from "@tanstack/react-query";
import { AlertCircle, Inbox } from "lucide-react";
import { getLandlordRecentActivity } from "@/lib/api/landlord";
import { LeaseActivity } from "@/lib/api/landlord";
import { RecentActivityCard } from "./recent-activity-card";
import { RecentActivitySkeleton } from "./recent-activity-skeleton";
import { RecentActivityState } from "@/components/dashboard/recent-activity/recent-activity-state";
import { queryKeys } from "@/lib/query-cache-key";

export function LandlordRecentActivity() {
  const { data, isLoading, isError, refetch, isFetching } = useQuery<
    LeaseActivity[]
  >({
    queryKey: [queryKeys.landlordRecentActivity],
    queryFn: getLandlordRecentActivity,
  });

  if (isLoading) {
    return <RecentActivitySkeleton />;
  }

  if (isError) {
    return (
      <RecentActivityState
        icon={AlertCircle}
        title="Couldn't load recent activity"
        description="Something went wrong while fetching your latest lease updates. Please try again."
        onRetry={() => refetch()}
        isRetrying={isFetching}
      />
    );
  }

  if (!data || data.length === 0) {
    return (
      <RecentActivityState
        icon={Inbox}
        title="No recent activity"
        description="Updates like uploaded photos, move-outs and settlements across your leases will show up here."
      />
    );
  }

  return <RecentActivityCard activities={data} />;
}
