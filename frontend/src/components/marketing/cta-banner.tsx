import Link from "next/link";
import { ArrowRight, ShieldCheck } from "lucide-react";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export function CtaBanner() {
  return (
    <div className="rounded-3xl bg-primary px-6 py-14 text-center text-primary-foreground sm:px-12 sm:py-16">
      <ShieldCheck className="mx-auto size-10 text-emerald-200" aria-hidden />
      <h2 className="mt-5 text-3xl font-bold tracking-tight text-balance sm:text-4xl">
        A deposit process both sides can trust
      </h2>
      <p className="mx-auto mt-3 max-w-xl text-base text-primary-foreground/80 sm:text-lg">
        Start a private lease record and keep every important step in one place.
      </p>
      <Link
        href="/register"
        className={cn(
          buttonVariants({ size: "lg" }),
          "mt-8 h-12 rounded-xl bg-amber-500 px-6 text-base font-semibold text-stone-900 hover:bg-amber-400",
        )}
      >
        Get started free
        <ArrowRight className="size-4" aria-hidden />
      </Link>
    </div>
  );
}
