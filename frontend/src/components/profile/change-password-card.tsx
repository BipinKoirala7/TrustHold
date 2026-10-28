import { cn } from "cn";
import Link from "next/link";
import { buttonVariants } from "../ui/button";

export function ChangePasswordCard() {
  return (
    <section className="flex flex-col gap-4 rounded-xl border bg-card p-6 shadow-sm">
      <div className="flex flex-col">
        <h2 className="font-semibold">Change password</h2>
        <p className="mt-1 text-sm text-muted-foreground">
          Choose a unique password you don&apos;t use elsewhere.
        </p>
      </div>
      <Link
        className={cn(buttonVariants({ className: "w-fit" }))}
        href={"/forgot-password"}
      >
        Change Password
      </Link>
    </section>
  );
}
