import { Logo } from "@/components/brand/logo";

type AuthSplitLayoutProps = {
  children: React.ReactNode;
  /** Right-hand panel, shown from the lg breakpoint up. */
  panel: React.ReactNode;
  footer?: React.ReactNode;
};

export function AuthSplitLayout({
  children,
  panel,
  footer,
}: AuthSplitLayoutProps) {
  return (
    <div className="grid min-h-svh lg:grid-cols-2">
      <div className="flex flex-col bg-background px-6 py-8 sm:px-10 lg:px-12">
        <Logo />

        <div className="flex flex-1 items-center justify-center py-12">
          <div className="w-full max-w-md">{children}</div>
        </div>

        {footer && <p className="text-sm text-muted-foreground">{footer}</p>}
      </div>

      {panel}
    </div>
  );
}
