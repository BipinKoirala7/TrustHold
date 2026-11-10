import type { LucideIcon } from "lucide-react";
import { Button } from "@/components/ui/button";

interface RecentActivityStateProps {
  icon: LucideIcon;
  title: string;
  description: string;
  onRetry?: () => void;
  isRetrying?: boolean;
}

export function RecentActivityState({
  icon: Icon,
  title,
  description,
  onRetry,
  isRetrying,
}: RecentActivityStateProps) {
  return (
    <section className="rounded-2xl border bg-card shadow-sm">
      <header className="border-b px-6 py-5">
        <h2 className="font-semibold">Recent activity</h2>
        <p className="text-sm text-muted-foreground">
          Latest updates across your leases
        </p>
      </header>
      <div
        role="status"
        className="flex flex-col items-center justify-center gap-3 px-6 py-12 text-center"
      >
        <div className="flex size-12 items-center justify-center rounded-full bg-muted">
          <Icon className="size-6 text-muted-foreground" />
        </div>
        <div className="space-y-1">
          <p className="font-semibold">{title}</p>
          <p className="max-w-xs text-sm text-muted-foreground">
            {description}
          </p>
        </div>
        {onRetry && (
          <Button
            variant="outline"
            size="sm"
            onClick={onRetry}
            disabled={isRetrying}
          >
            {isRetrying ? "Retrying..." : "Try again"}
          </Button>
        )}
      </div>
    </section>
  );
}
