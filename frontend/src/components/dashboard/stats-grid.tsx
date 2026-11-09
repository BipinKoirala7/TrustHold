import type { DashboardStat } from "@/lib/mock-data";
import { StatCard } from "@/components/dashboard/stats-card";

export function StatsGrid({ stats }: { stats: DashboardStat[] }) {
  const columns =
    stats.length === 3
      ? "sm:grid-cols-2 lg:grid-cols-3"
      : "sm:grid-cols-2 xl:grid-cols-4";

  return (
    <div className={`grid gap-2 ${columns}`}>
      {stats.map((stat) => (
        <StatCard key={stat.id} stat={stat} />
      ))}
    </div>
  );
}
