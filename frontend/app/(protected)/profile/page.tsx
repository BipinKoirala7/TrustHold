import { AccountInfoCard } from "@/components/profile/account-info-card";
import { ChangePasswordCard } from "@/components/profile/change-password-card";
import { ProfileFormCard } from "@/components/profile/profile-form-card";

export default function ProfilePage() {
  return (
    <div className="mx-auto max-w-6xl space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">
          Profile &amp; account
        </h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Manage your personal details and account security.
        </p>
      </div>

      <div className="grid items-start gap-6 lg:grid-cols-[1fr_20rem]">
        <div className="space-y-6">
          <ProfileFormCard />
          <ChangePasswordCard />
        </div>
        <AccountInfoCard />
      </div>
    </div>
  );
}
