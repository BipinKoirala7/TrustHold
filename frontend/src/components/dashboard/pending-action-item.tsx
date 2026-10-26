import Link from "next/link";
import { ChevronRight, FileText, Upload, Wallet } from "lucide-react";
import type { PendingAction, PendingActionKind } from "@/lib/mock-data";

const icons: Record<PendingActionKind, typeof Wallet> = {
  payment: Wallet,
  settlement: FileText,
  photos: Upload,
};

export function PendingActionItem({ action }: { action: PendingAction }) {
  const Icon = icons[action.kind];

  return (
    <li>
      <Link
        href={action.href}
        className="flex items-center gap-4 px-6 py-4 transition-colors hover:bg-muted/50"
      >
        <div className="flex size-10 shrink-0 items-center justify-center rounded-xl bg-amber-100 text-amber-600">
          <Icon className="size-5" />
        </div>
        <div className="min-w-0 flex-1">
          <p className="truncate font-semibold">{action.title}</p>
          <p className="truncate text-sm text-muted-foreground">
            {action.property}
          </p>
        </div>
        <span className="flex items-center gap-1 text-sm font-semibold text-primary">
          {action.ctaLabel}
          <ChevronRight className="size-4 text-muted-foreground" />
        </span>
      </Link>
    </li>
  );
}
