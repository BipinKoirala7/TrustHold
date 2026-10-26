import type { Metadata } from "next";
import Link from "next/link";
import { AuthShowcase } from "@/components/auth/auth-showcase";
import { AuthSplitLayout } from "@/components/auth/auth-split-layout";
import { RegisterForm } from "@/components/auth/register-form";

export const metadata: Metadata = {
  title: "Create account — TrustHold",
  description: "Start with a private, secure lease record.",
};

export default function RegisterPage() {
  return (
    <AuthSplitLayout
      panel={
        <AuthShowcase
          className="hidden lg:flex"
          quote="A clear record, secure funds, and agreement from both sides — exactly how deposits should work."
          caption="Built for calmer rental relationships"
        />
      }
      footer="By continuing, you agree to TrustHold’s Terms and Privacy Policy."
    >
      <div className="flex flex-col gap-4">
        <div className="flex flex-col gap-1">
          <h1 className="text-3xl font-bold tracking-tight text-foreground sm:text-3xl">
            Create your account
          </h1>
          <p className="text-muted-foreground">
            Start with a private, secure lease record.
          </p>
        </div>

        <RegisterForm />

        <p className="text-center text-sm text-muted-foreground">
          Already have an account?{" "}
          <Link
            href="/login"
            className="font-semibold text-primary underline-offset-4 hover:underline"
          >
            Log in
          </Link>
        </p>
      </div>
    </AuthSplitLayout>
  );
}
