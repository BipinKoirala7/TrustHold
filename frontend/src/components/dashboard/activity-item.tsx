import type { ActivityEntry } from "@/lib/mock-data";

export function ActivityItem({ entry }: { entry: ActivityEntry }) {
  return (
    <li className="flex gap-3">
      <span className="mt-2 size-2 shrink-0 rounded-full bg-primary" />
      <div>
        <p className="font-semibold">{entry.title}</p>
        <p className="text-sm text-muted-foreground">{entry.description}</p>
        <p className="mt-1 text-xs text-muted-foreground/80">{entry.time}</p>
      </div>
    </li>
  );
}
