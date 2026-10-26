import Image from "next/image";
import Link from "next/link";
import { ArrowRight, ShieldCheck } from "lucide-react";

import heroImage from "~/public/hero.jpg";
import { buttonVariants } from "@/components/ui/button";
import { FloatingStatCard } from "@/components/marketing/floating-stat-card";
import { cn } from "@/lib/utils";

export function Hero() {
  return (
    <section className="relative overflow-hidden bg-linear-to-br from-background via-background to-highlight-soft/50 border-b border-input">
      <div className="mx-auto grid max-w-6xl items-center gap-14 px-6 pb-20 pt-10 md:pt-16 lg:grid-cols-2 lg:gap-16 lg:pb-28">
        <div>
          <span className="inline-flex items-center gap-1.5 rounded-full bg-highlight/50 px-3 py-1 text-sm font-medium text-highlight-soft-foreground">
            <ShieldCheck className="size-3.5" aria-hidden />
            Deposit escrow for Nepal rentals
          </span>

          <h1 className="mt-6 text-4xl font-bold leading-[1.1] tracking-tight text-balance text-foreground sm:text-5xl lg:text-6xl">
            The deposit stays safe until you{" "}
            <span className="text-primary">both agree</span>.
          </h1>

          <p className="mt-6 max-w-xl text-lg leading-relaxed text-muted-foreground">
            TrustHold holds the security deposit in escrow. Landlord and tenant
            document the home with photos, agree on the split together, and the
            money is released only then.
          </p>

          <div className="mt-8 flex flex-wrap items-center gap-3">
            <Link
              href="/register"
              className={cn(
                buttonVariants({ size: "lg" }),
                "h-12 rounded-xl px-6 text-base",
              )}
            >
              Get started
              <ArrowRight className="size-4" aria-hidden />
            </Link>
            <Link
              href="/login"
              className={cn(
                buttonVariants({ variant: "outline", size: "lg" }),
                "h-12 rounded-xl bg-card px-6 text-base",
              )}
            >
              Log in
            </Link>
          </div>

          <p className="mt-6 text-sm text-muted-foreground">
            No listings, no browsing — just a private record between you two.
          </p>
        </div>

        <div className="relative pb-6">
          <div className="relative aspect-4/3 overflow-hidden rounded-3xl bg-linear-to-br from-primary/20 to-highlight-soft shadow-xl ring-1 ring-foreground/5">
            <Image
              src={heroImage}
              alt="A sunlit living room with a wooden table holding a key, a shield and a folded lease agreement, with Kathmandu rooftops through the window"
              fill
              priority
              sizes="(min-width: 1024px) 560px, 100vw"
              className="object-cover"
            />
          </div>
          <FloatingStatCard
            title="Rs. 80,000 held in escrow"
            caption="Released when both agree"
            className="absolute bottom-0 left-4 sm:left-6"
          />
        </div>
      </div>
    </section>
  );
}
