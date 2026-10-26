import { dashboardStats } from "@/lib/mock-data";
import { StatCard } from "@/components/dashboard/landlord/stats-card";

export function StatsGrid() {
  return (
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      {dashboardStats.map((stat) => (
        <StatCard key={stat.id} stat={stat} />
      ))}
    </div>
  );
}
