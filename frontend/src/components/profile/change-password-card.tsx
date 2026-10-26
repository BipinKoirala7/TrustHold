"use client";

import { Button } from "@/components/ui/button";
import { ProfileField } from "./profile-field";

export function ChangePasswordCard() {
  return (
    <section className="rounded-xl border bg-card p-6 shadow-sm">
      <h2 className="font-semibold">Change password</h2>
      <p className="mt-1 text-sm text-muted-foreground">
        Choose a unique password you don&apos;t use elsewhere.
      </p>

      <form onSubmit={(e) => e.preventDefault()} className="mt-6 grid gap-5">
        <ProfileField
          id="currentPassword"
          label="Current password"
          type="password"
          autoComplete="current-password"
        />
        <div className="grid gap-5 sm:grid-cols-2">
          <ProfileField
            id="newPassword"
            label="New password"
            type="password"
            autoComplete="new-password"
          />
          <ProfileField
            id="confirmPassword"
            label="Confirm new password"
            type="password"
            autoComplete="new-password"
          />
        </div>
        <div>
          <Button type="submit" variant="outline">
            Update password
          </Button>
        </div>
      </form>
    </section>
  );
}
