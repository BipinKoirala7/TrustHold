import { Logo } from "@/components/brand/logo";
import { Year } from "./footer-date";

export function MarketingFooter() {
  return (
    <footer className="border-t border-border bg-background">
      <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-4 px-6 py-8 sm:flex-row">
        <Logo />
        <p className="text-sm text-muted-foreground">
          © {<Year />} TrustHold. Clear deposits, calmer tenancies.
        </p>
      </div>
    </footer>
  );
}
