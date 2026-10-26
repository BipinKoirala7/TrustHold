import { pendingActions } from "@/lib/mock-data";
import { PendingActionItem } from "./pending-action-item";

export function PendingActionsCard() {
  return (
    <section className="rounded-2xl border bg-card shadow-sm">
      <header className="flex items-center justify-between border-b px-6 py-5">
        <div>
          <h2 className="font-semibold">Pending actions</h2>
          <p className="text-sm text-muted-foreground">
            Items that need your attention
          </p>
        </div>
        <span className="flex size-8 items-center justify-center rounded-full bg-amber-100 text-sm font-semibold text-amber-700">
          {pendingActions.length}
        </span>
      </header>
      <ul className="divide-y">
        {pendingActions.map((action) => (
          <PendingActionItem key={action.id} action={action} />
        ))}
      </ul>
    </section>
  );
}
