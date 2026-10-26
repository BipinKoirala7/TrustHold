import { GreetingHeader } from "@/components/dashboard/landlord/greeting-header";
import { StatsGrid } from "@/components/dashboard/landlord/stats-grid";
import { PendingActionsCard } from "@/components/dashboard/landlord/pending-actions-card";
import { RecentActivityCard } from "@/components/dashboard/landlord/recent-activity-card";
import { currentLandlord } from "@/lib/mock-data";

export default function LandlordDashboardPage() {
  return (
    <div className="mx-auto max-w-7xl space-y-8">
      <GreetingHeader firstName={currentLandlord.firstName} />
      <StatsGrid />
      <div className="grid gap-6 lg:grid-cols-[1.45fr_1fr]">
        <PendingActionsCard />
        <RecentActivityCard />
      </div>
    </div>
  );
}
