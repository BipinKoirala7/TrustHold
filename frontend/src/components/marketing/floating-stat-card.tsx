import { CircleCheck } from "lucide-react";
import { IconTile } from "@/components/marketing/icon-tile";
import { cn } from "@/lib/utils";

type FloatingStatCardProps = {
  title: string;
  caption: string;
  className?: string;
};

export function FloatingStatCard({
  title,
  caption,
  className,
}: FloatingStatCardProps) {
  return (
    <div
      className={cn(
        "flex items-center gap-3 rounded-2xl bg-card px-5 py-4 shadow-lg ring-1 ring-border",
        className,
      )}
    >
      <IconTile shape="circle">
        <CircleCheck />
      </IconTile>
      <div>
        <p className="text-sm font-semibold text-foreground">{title}</p>
        <p className="text-xs text-muted-foreground">{caption}</p>
      </div>
    </div>
  );
}
