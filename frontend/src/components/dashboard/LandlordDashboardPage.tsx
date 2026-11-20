"use client";

import { LandlordRecentActivity } from "./recent-activity/landlord-recent-activity";
import { LandlordStatsGrid } from "./stats/landlord-stats-grid";
import { GreetingHeader } from "./greeting-header";
import { useAuth } from "@/hooks/useAuth";

function LandlordDashboardPage() {
  const { user } = useAuth();

  if (!user) {
    return <div>Loading...</div>;
  }

  return (
    <div className="mx-auto max-w-7xl space-y-8">
      <GreetingHeader
        firstName={user.firstName + user.lastName}
        subtitle="Here's what's happening across your leases today."
      />
      <LandlordStatsGrid />
      <div className="grid gap-6 lg:grid-cols-[1.45fr_1fr]">
        {/* <PendingActionsCard actions={pendingActions} /> */}
        <LandlordRecentActivity />
      </div>
    </div>
  );
}

export default LandlordDashboardPage;
