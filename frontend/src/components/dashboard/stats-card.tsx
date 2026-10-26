import { Building2, CheckCircle2, Clock, Wallet } from "lucide-react";
import type { DashboardStat, StatIcon, StatTone } from "@/lib/mock-data";
import { cn } from "@/lib/utils";

const icons: Record<StatIcon, typeof Building2> = {
  leases: Building2,
  escrow: Wallet,
  payment: Clock,
  agreement: CheckCircle2,
};

const tones: Record<StatTone, string> = {
  primary: "bg-primary/10 text-primary",
  amber: "bg-amber-100 text-amber-600",
};

export function StatCard({ stat }: { stat: DashboardStat }) {
  const Icon = icons[stat.icon];

  return (
    <div className="rounded-2xl border bg-card p-6 shadow-sm">
      <div className="flex items-start justify-between">
        <p className="text-sm text-muted-foreground">{stat.label}</p>
        <div
          className={cn(
            "flex size-10 items-center justify-center rounded-xl",
            tones[stat.tone],
          )}
        >
          <Icon className="size-5" />
        </div>
      </div>
      <p className="mt-6 text-3xl font-bold tracking-tight">{stat.value}</p>
      <p className="mt-2 text-sm text-muted-foreground">{stat.hint}</p>
    </div>
  );
}
