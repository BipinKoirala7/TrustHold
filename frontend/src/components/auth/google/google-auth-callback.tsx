import Link from "next/link";
import { AlertCircle, ShieldCheck } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/spinner";

export type CallbackStatus = "loading" | "error";

export function GoogleCallbackStatus({ status }: { status: CallbackStatus }) {
  const isError = status === "error";

  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4">
      <div className="w-full max-w-sm rounded-2xl border border-border bg-card p-8 text-center shadow-sm">
        <div className="relative mx-auto mb-6 flex size-16 items-center justify-center">
          {!isError && (
            <span className="absolute inset-0 animate-ping rounded-2xl bg-primary/20" />
          )}
          <div
            className={
              isError
                ? "relative flex size-16 items-center justify-center rounded-2xl bg-destructive/10 text-destructive"
                : "relative flex size-16 items-center justify-center rounded-2xl bg-primary text-primary-foreground"
            }
          >
            {isError ? (
              <AlertCircle className="size-8" />
            ) : (
              <ShieldCheck className="size-8" />
            )}
          </div>
        </div>

        {isError ? (
          <>
            <h1 className="text-xl font-semibold">Google sign-in failed</h1>
            <p className="mt-2 text-sm text-muted-foreground">
              We couldn&apos;t complete your sign-in. Please try again.
            </p>
            <Button size="lg" className="mt-6 h-12 w-full rounded-xl">
              <Link href="/login">Back to login</Link>
            </Button>
          </>
        ) : (
          <>
            <h1 className="text-xl font-semibold">Signing you in</h1>
            <p className="mt-2 text-sm text-muted-foreground">
              Verifying your Google account…
            </p>
            <div className="mt-6 flex justify-center text-primary">
              <Spinner />
            </div>
          </>
        )}

        <p className="mt-8 text-xs font-medium tracking-wide text-muted-foreground">
          Trust<span className="text-primary">Hold</span>
        </p>
      </div>
    </div>
  );
}
