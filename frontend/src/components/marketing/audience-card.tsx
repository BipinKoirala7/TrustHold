import { CircleCheck, type LucideIcon } from "lucide-react";
import { IconTile } from "@/components/marketing/icon-tile";

type AudienceCardProps = {
  icon: LucideIcon;
  eyebrow: string;
  title: string;
  items: string[];
};

export function AudienceCard({
  icon: Icon,
  eyebrow,
  title,
  items,
}: AudienceCardProps) {
  return (
    <article className="rounded-3xl border border-border bg-card p-8 shadow-sm">
      <IconTile size="lg">
        <Icon />
      </IconTile>
      <p className="mt-6 text-xs font-semibold uppercase tracking-[0.18em] text-primary">
        {eyebrow}
      </p>
      <h3 className="mt-2 text-xl font-bold tracking-tight text-foreground sm:text-2xl">
        {title}
      </h3>
      <ul className="mt-6 space-y-3">
        {items.map((item) => (
          <li
            key={item}
            className="flex items-start gap-3 text-sm sm:text-base"
          >
            <CircleCheck
              className="mt-0.5 size-5 shrink-0 text-primary"
              aria-hidden
            />
            <span className="text-muted-foreground">{item}</span>
          </li>
        ))}
      </ul>
    </article>
  );
}
