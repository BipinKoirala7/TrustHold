"use client";

import { Button } from "@/components/ui/button";
import { getInitials, roleLabels, type UserProfile } from "@/lib/profile-data";
import { ProfileField } from "./profile-field";

export function ProfileFormCard({ profile }: { profile: UserProfile }) {
  return (
    <section className="rounded-xl border bg-card p-6 shadow-sm">
      <div className="flex items-center gap-4 border-b pb-6">
        <div className="flex size-14 items-center justify-center rounded-full bg-primary/10 text-lg font-bold text-primary">
          {getInitials(profile.fullName)}
        </div>
        <div>
          <p className="font-semibold">{profile.fullName}</p>
          <p className="text-sm text-muted-foreground">{profile.email}</p>
        </div>
      </div>

      <form onSubmit={(e) => e.preventDefault()} className="mt-6 grid gap-5">
        <div className="grid gap-5 sm:grid-cols-2">
          <ProfileField
            id="fullName"
            label="Full name"
            defaultValue={profile.fullName}
          />
          <ProfileField
            id="phone"
            label="Phone number"
            type="tel"
            defaultValue={profile.phone}
          />
          <ProfileField
            id="email"
            label="Email address"
            type="email"
            defaultValue={profile.email}
            disabled
          />
          <ProfileField
            id="role"
            label="Account role"
            defaultValue={roleLabels[profile.role]}
            disabled
          />
        </div>
        <div>
          <Button type="submit">Save changes</Button>
        </div>
      </form>
    </section>
  );
}
