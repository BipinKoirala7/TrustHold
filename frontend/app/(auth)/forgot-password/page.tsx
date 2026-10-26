import { AuthShowcase } from "@/components/auth/auth-showcase";
import { AuthSplitLayout } from "@/components/auth/auth-split-layout";
import { ResetPasswordForm } from "@/components/auth/reset-password-form";

export default function ForgotPasswordPage() {
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
      <div className="flex flex-col gap-6">
        <div className="flex flex-col gap-1">
          <h1 className="text-3xl font-bold tracking-tight text-foreground sm:text-4xl">
            Reset your password
          </h1>
          <p className="mt-2 text-muted-foreground">
            Enter your email and we’ll send you a secure reset link.
          </p>
        </div>

        <ResetPasswordForm />
      </div>
    </AuthSplitLayout>
  );
}
