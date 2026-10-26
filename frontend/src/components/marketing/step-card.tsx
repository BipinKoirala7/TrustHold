import type { LucideIcon } from "lucide-react";
import { IconTile } from "@/components/marketing/icon-tile";

type StepCardProps = {
  number: string;
  icon: LucideIcon;
  title: string;
  description: string;
};

export function StepCard({
  number,
  icon: Icon,
  title,
  description,
}: StepCardProps) {
  return (
    <li className="relative rounded-2xl border border-border bg-card p-6 shadow-sm">
      <span className="absolute right-6 top-6 text-sm font-semibold tabular-nums text-muted-foreground/60">
        {number}
      </span>
      <IconTile>
        <Icon />
      </IconTile>
      <h3 className="mt-5 text-base font-semibold text-foreground">{title}</h3>
      <p className="mt-2 text-sm leading-relaxed text-muted-foreground">
        {description}
      </p>
    </li>
  );
}
