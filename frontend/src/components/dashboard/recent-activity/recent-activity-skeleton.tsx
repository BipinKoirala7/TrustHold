import { Skeleton } from "@/components/ui/skeleton";

export function RecentActivitySkeleton() {
  return (
    <section
      className="rounded-2xl border bg-card shadow-sm"
      aria-busy="true"
      aria-label="Loading recent activity"
    >
      <header className="border-b px-6 py-5">
        <h2 className="font-semibold">Recent activity</h2>
        <p className="text-sm text-muted-foreground">
          Latest updates across your leases
        </p>
      </header>
      <ul className="space-y-6 px-6 py-6">
        {Array.from({ length: 4 }).map((_, i) => (
          <li key={i} className="flex gap-3">
            <Skeleton className="mt-2 size-2 shrink-0 rounded-full" />
            <div className="w-full space-y-2">
              <Skeleton className="h-4 w-1/3" />
              <Skeleton className="h-3 w-2/3" />
              <Skeleton className="h-3 w-1/4" />
            </div>
          </li>
        ))}
      </ul>
    </section>
  );
}
