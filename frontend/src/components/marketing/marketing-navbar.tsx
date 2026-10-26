import Link from "next/link";
import { Logo } from "@/components/brand/logo";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export function MarketingNavbar() {
  return (
    <header className="sticky top-0 z-40 bg-background/80 backdrop-blur supports-backdrop-filter:bg-background/70">
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-6">
        <Logo />
        <nav aria-label="Account" className="flex items-center gap-2">
          <Link
            href="/login"
            className={cn(
              buttonVariants({ variant: "ghost" }),
              "rounded-xl px-4",
            )}
          >
            Log in
          </Link>
          <Link
            href="/register"
            className={cn(buttonVariants(), "rounded-xl px-5")}
          >
            Get started
          </Link>
        </nav>
      </div>
    </header>
  );
}
