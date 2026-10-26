import { GreetingHeader } from "@/components/dashboard/greeting-header";
import { PendingActionsCard } from "@/components/dashboard/pending-actions-card";
import { RecentActivityCard } from "@/components/dashboard/recent-activity-card";
import { StatsGrid } from "@/components/dashboard/stats-grid";
import {
  currentLandlord,
  dashboardStats,
  pendingActions,
  recentActivity,
} from "@/lib/mock-data";

export default function LandlordDashboardPage() {
  return (
    <div className="mx-auto max-w-7xl space-y-8">
      <GreetingHeader
        firstName={currentLandlord.firstName}
        subtitle="Here's what's happening across your leases today."
        leasesHref="/landlord/leases"
      />
      <StatsGrid stats={dashboardStats} />
      <div className="grid gap-6 lg:grid-cols-[1.45fr_1fr]">
        <PendingActionsCard actions={pendingActions} />
        <RecentActivityCard entries={recentActivity} />
      </div>
    </div>
  );
}
