import { GreetingHeader } from "@/components/dashboard/greeting-header";
import { LandlordRecentActivity } from "@/components/dashboard/recent-activity/landlord-recent-activity";
import { LandlordStatsGrid } from "@/components/dashboard/landlord-stats-grid";
import { PendingActionsCard } from "@/components/dashboard/pending-actions-card";
import { RecentActivityCard } from "@/components/dashboard/recent-activity/recent-activity-card";
import {
  currentLandlord,
  pendingActions,
  recentActivity,
} from "@/lib/mock-data";

export default function LandlordDashboardPage() {
  return (
    <div className="mx-auto max-w-7xl space-y-8">
      <GreetingHeader
        firstName={currentLandlord.firstName}
        subtitle="Here's what's happening across your leases today."
      />
      <LandlordStatsGrid />
      <div className="grid gap-6 lg:grid-cols-[1.45fr_1fr]">
        <PendingActionsCard actions={pendingActions} />
        <LandlordRecentActivity />
      </div>
    </div>
  );
}
