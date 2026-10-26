import { recentActivity } from "@/lib/mock-data";
import { ActivityItem } from "./activity-item";

export function RecentActivityCard() {
  return (
    <section className="rounded-2xl border bg-card shadow-sm">
      <header className="border-b px-6 py-5">
        <h2 className="font-semibold">Recent activity</h2>
        <p className="text-sm text-muted-foreground">
          Latest updates across your leases
        </p>
      </header>
      <ul className="space-y-6 px-6 py-6">
        {recentActivity.map((entry) => (
          <ActivityItem key={entry.id} entry={entry} />
        ))}
      </ul>
    </section>
  );
}
