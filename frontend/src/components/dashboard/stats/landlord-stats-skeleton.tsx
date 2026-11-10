import { Skeleton } from "@/components/ui/skeleton";

export function LandlordStatsSkeleton() {
  return (
    <div
      className="grid gap-2 sm:grid-cols-2 xl:grid-cols-4"
      aria-busy="true"
      aria-label="Loading dashboard stats"
    >
      {Array.from({ length: 4 }).map((_, i) => (
        <div key={i} className="rounded-2xl border bg-card p-6 shadow-sm">
          <div className="flex items-start justify-between">
            <Skeleton className="h-4 w-24" />
            <Skeleton className="size-10 rounded-xl" />
          </div>
          <Skeleton className="mt-6 h-9 w-20" />
          <Skeleton className="mt-2 h-4 w-32" />
        </div>
      ))}
    </div>
  );
}
