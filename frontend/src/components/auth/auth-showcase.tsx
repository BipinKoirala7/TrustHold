import Image from "next/image";
import { ShieldCheck } from "lucide-react";
import { cn } from "@/lib/utils";

type AuthShowcaseProps = {
  quote: string;
  caption: string;
  className?: string;
};

export function AuthShowcase({ quote, caption, className }: AuthShowcaseProps) {
  return (
    <aside
      className={cn(
        "relative isolate flex flex-col justify-end overflow-hidden bg-primary p-12 text-primary-foreground xl:p-16",
        className,
      )}
    >
      {/* Photo is desaturated, then tinted green so any image fits the brand. */}
      <Image
        src="/images/hero.jpg"
        alt=""
        fill
        sizes="50vw"
        className="-z-20 object-cover grayscale"
      />
      <div
        aria-hidden
        className="absolute inset-0 -z-10 bg-linear-to-t from-primary/95 via-primary/75 to-primary/60"
      />

      <div className="max-w-xl">
        <ShieldCheck
          className="size-10 text-primary-foreground/80"
          aria-hidden
        />
        <blockquote className="mt-6 text-2xl font-semibold leading-snug tracking-tight text-balance xl:text-3xl">
          “{quote}”
        </blockquote>
        <p className="mt-5 text-sm text-primary-foreground/80">{caption}</p>
      </div>
    </aside>
  );
}
