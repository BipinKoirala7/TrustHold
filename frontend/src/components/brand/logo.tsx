import Link from "next/link";
import { ShieldCheck } from "lucide-react";
import { cn } from "@/lib/utils";

type LogoProps = {
  className?: string;
  href?: string | null;
};

export function Logo({ className, href = "/" }: LogoProps) {
  const content = (
    <span className={cn("inline-flex items-center gap-2.5", className)}>
      <span className="flex size-10 items-center justify-center rounded-xl bg-primary text-primary-foreground">
        <ShieldCheck className="size-5" aria-hidden />
      </span>
      <span className="text-xl font-semibold tracking-tight text-foreground">
        Trust<span className="text-primary">Hold</span>
      </span>
    </span>
  );

  if (href === null) return content;

  return (
    <Link href={href} aria-label="TrustHold home">
      {content}
    </Link>
  );
}
