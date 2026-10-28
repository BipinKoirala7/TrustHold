"use client";

import { Button } from "@/components/ui/button";
import { ProfileField } from "./profile-field";
import { useAuth } from "@/lib/hooks/useAuth";
import { redirect } from "next/navigation";

export function ProfileFormCard() {
  const { user } = useAuth();

  if (!user) return redirect("/login");
  return (
    <section className="rounded-xl border bg-card p-6 shadow-sm">
      <div className="flex items-center gap-4 border-b pb-6">
        <div className="flex size-14 items-center justify-center rounded-full bg-primary/10 text-lg font-bold text-primary">
          {user.firstName[0].toUpperCase() + user.lastName[0].toUpperCase()}
        </div>
        <div>
          <p className="font-semibold">
            {user.firstName + " " + user.lastName}
          </p>
          <p className="text-sm text-muted-foreground">{user.email}</p>
        </div>
      </div>

      <form onSubmit={(e) => e.preventDefault()} className="mt-6 grid gap-5">
        <div className="grid gap-5 sm:grid-cols-2">
          <ProfileField
            id="firstName"
            label="First name"
            defaultValue={user.firstName}
          />
          <ProfileField
            id="lastName"
            label="Last name"
            defaultValue={user.lastName}
          />
          <ProfileField
            id="username"
            label="Username"
            defaultValue={user.username}
          />
          {/* <ProfileField
            id="phone"
            label="Phone number"
            type="tel"
            defaultValue={profile.phone}
          /> */}
          <ProfileField
            id="email"
            label="Email address"
            type="email"
            defaultValue={user.email}
            disabled
          />
          <ProfileField
            id="role"
            label="Account role"
            defaultValue={user.role}
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
