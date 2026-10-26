import { GreetingHeader } from "@/components/dashboard/greeting-header";
import { PendingActionsCard } from "@/components/dashboard/pending-actions-card";
import { RecentActivityCard } from "@/components/dashboard/recent-activity-card";
import { StatsGrid } from "@/components/dashboard/stats-grid";
import {
  currentTenant,
  tenantActivity,
  tenantPendingActions,
  tenantStats,
} from "@/lib/tenant-data";

export default function TenantDashboardPage() {
  return (
    <div className="mx-auto max-w-7xl space-y-8">
      <GreetingHeader
        prefix="Welcome back"
        firstName={currentTenant.firstName}
        subtitle="Your deposits, evidence, and next steps are all in one place."
        leasesHref="/tenant/leases"
      />
      <StatsGrid stats={tenantStats} />
      <div className="grid gap-6 lg:grid-cols-[1.45fr_1fr]">
        <PendingActionsCard actions={tenantPendingActions} />
        <RecentActivityCard entries={tenantActivity} />
      </div>
    </div>
  );
}
