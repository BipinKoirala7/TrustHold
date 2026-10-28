"use client";

import { UserRound } from "lucide-react";
import { useAuth } from "@/lib/hooks/useAuth";
import { redirect } from "next/navigation";

export function AccountInfoCard() {
  const { user } = useAuth();

  if (!user) return redirect("/login");
  return (
    <aside className="rounded-xl border bg-card p-6 shadow-sm">
      <div className="flex items-center gap-3">
        <div className="flex size-10 items-center justify-center rounded-xl bg-primary/10 text-primary">
          <UserRound className="size-5" />
        </div>
        <p className="text-xl">@{user.username}</p>
      </div>
      <h2 className="mt-4 font-semibold">Your account</h2>
      <p className="mt-2 text-sm leading-relaxed text-muted-foreground">
        Your contact details are visible only to parties on your leases and
        TrustHold administrators.
      </p>

      <div className="mt-5 border-t pt-5">
        <p className="text-xs font-semibold tracking-wide text-muted-foreground uppercase">
          Member since
        </p>
        <p className="mt-1 font-semibold">
          {new Intl.DateTimeFormat("en-GB", {
            month: "long",
            year: "numeric",
            timeZone: "UTC",
          }).format(new Date(user.createdAt))}
        </p>
      </div>
    </aside>
  );
}
