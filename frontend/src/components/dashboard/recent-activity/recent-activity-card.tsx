import { ActivityItem } from "../activity-item";
import { LeaseActivity } from "@/lib/api/landlord";

export function RecentActivityCard({
  activities,
}: {
  activities: LeaseActivity[];
}) {
  return (
    <section className="rounded-2xl border bg-card shadow-sm">
      <header className="border-b px-6 py-5">
        <h2 className="font-semibold">Recent activity</h2>
        <p className="text-sm text-muted-foreground">
          Latest updates across your leases
        </p>
      </header>
      <ul className="space-y-6 px-6 py-6">
        {activities.map((activity) => (
          <ActivityItem key={activity.id} entry={activity} />
        ))}
      </ul>
    </section>
  );
}
