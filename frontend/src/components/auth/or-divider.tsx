import { cn } from "@/lib/utils";

type OrDividerProps = {
  label?: string;
  className?: string;
};

export function OrDivider({ label = "or", className }: OrDividerProps) {
  return (
    <div
      className={cn(
        "flex items-center gap-4 text-sm text-muted-foreground",
        className,
      )}
    >
      <span aria-hidden className="h-px flex-1 bg-border" />
      <span>{label}</span>
      <span aria-hidden className="h-px flex-1 bg-border" />
    </div>
  );
}
