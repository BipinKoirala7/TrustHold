import { AppSidebar } from "@/components/dashboard/sidebar";
import { Logo } from "@/components/brand/logo";

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="flex min-h-screen bg-background">
      <AppSidebar />
      <div className="flex min-w-0 flex-1 flex-col">
        <header className="border-b bg-card px-5 py-4 md:hidden">
          <Logo />
        </header>
        <main className="flex-1 px-5 py-8 md:px-12 md:py-12">{children}</main>
      </div>
    </div>
  );
}
