import type { Metadata } from "next";
import { Suspense } from "react";
import { AuthSplitLayout } from "@/components/auth/auth-split-layout";
import { ResetPasswordForm } from "@/components/auth/reset-password-form";
import { AuthShowcase } from "@/components/auth/auth-showcase";

export const metadata: Metadata = {
  title: "Reset password",
  referrer: "no-referrer", // keeps the token out of Referer headers
  robots: { index: false },
};

export default function ResetPasswordPage() {
  return (
    <AuthSplitLayout
      panel={
        <AuthShowcase
          quote="Your deposit stays safe until you both agree."
          caption="Built for calmer rental relationships"
        />
      }
    >
      <div className="mb-8">
        <h1 className="text-4xl font-bold tracking-tight text-foreground">
          Set a new password
        </h1>
        <p className="mt-2 text-base text-muted-foreground">
          Choose a strong password you haven&apos;t used before.
        </p>
      </div>

      <Suspense fallback={null}>
        <ResetPasswordForm />
      </Suspense>
    </AuthSplitLayout>
  );
}
