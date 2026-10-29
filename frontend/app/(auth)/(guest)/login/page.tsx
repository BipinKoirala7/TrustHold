import type { Metadata } from "next";
import Link from "next/link";
import { AuthShowcase } from "@/components/auth/auth-showcase";
import { AuthSplitLayout } from "@/components/auth/auth-split-layout";
import { GoogleButton } from "@/components/auth/google-button";
import { LoginForm } from "@/components/auth/login-form";
import { OrDivider } from "@/components/auth/or-divider";

export const metadata: Metadata = {
  title: "Log in — TrustHold",
  description: "Sign in to manage your leases and deposit activity.",
};

export default function LoginPage() {
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
      <h1 className="text-3xl font-bold tracking-tight text-foreground sm:text-4xl">
        Welcome back
      </h1>
      <p className="mt-2 text-muted-foreground">
        Sign in to manage your leases and deposit activity.
      </p>

      <div className="mt-8 flex flex-col gap-6">
        <GoogleButton />
        <OrDivider />
        <LoginForm />
      </div>

      <p className="mt-8 text-center text-sm text-muted-foreground">
        New to TrustHold?{" "}
        <Link
          href="/register"
          className="font-semibold text-primary underline-offset-4 hover:underline"
        >
          Create an account
        </Link>
      </p>
    </AuthSplitLayout>
  );
}
