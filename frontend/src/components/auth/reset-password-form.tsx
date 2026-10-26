import Link from "next/link";
import { Button, buttonVariants } from "../ui/button";
import { Input } from "../ui/input";
import { cn } from "cn";

const inputClass = "h-12 bg-card px-4 text-base md:text-base border-border";

export function ResetPasswordForm() {
  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-col gap-2">
        <label
          htmlFor="password"
          className="text-sm font-medium text-foreground"
        >
          Email
        </label>
        <Input
          id="email"
          placeholder="your.email@example.com"
          className={inputClass}
        />
      </div>
      <div className="flex flex-col gap-2">
        <Button>Send Reset Link</Button>
        <Link
          href="/login"
          className={cn(buttonVariants({ variant: "secondary" }), "w-full")}
        >
          Back to Login
        </Link>
      </div>
    </div>
  );
}
