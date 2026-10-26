"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { FileText, Home, LogOut, User } from "lucide-react";
import { Logo } from "@/components/brand/logo";
import { currentLandlord, landlordNav } from "@/lib/mock-data";
import { cn } from "@/lib/utils";

const icons = {
  dashboard: Home,
  leases: FileText,
  profile: User,
};

export function AppSidebar() {
  const pathname = usePathname();

  const isActive = (href: string) =>
    href === "/landlord" ? pathname === href : pathname.startsWith(href);

  return (
    <aside className="sticky top-0 hidden h-screen w-80 shrink-0 flex-col border-r bg-card p-7 md:flex">
      <div className="px-2">
        <Logo />
      </div>

      <nav className="mt-10 flex flex-col gap-2">
        {landlordNav.map((item) => {
          const Icon = icons[item.icon];
          const active = isActive(item.href);
          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "flex items-center gap-3 rounded-xl px-4 py-3 font-semibold transition-colors",
                active
                  ? "bg-primary text-primary-foreground"
                  : "text-foreground/80 hover:bg-muted",
              )}
            >
              <Icon className="size-5" />
              {item.label}
            </Link>
          );
        })}
      </nav>

      <div className="mt-auto flex items-center gap-3 border-t pt-5">
        <div className="flex size-10 items-center justify-center rounded-full bg-primary/10 text-sm font-bold text-primary">
          {currentLandlord.initials}
        </div>
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-semibold">
            {currentLandlord.name}
          </p>
          <p className="text-xs text-muted-foreground">
            {currentLandlord.role}
          </p>
        </div>
        <button
          type="button"
          aria-label="Log out"
          className="rounded-lg p-2 text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
        >
          <LogOut className="size-4" />
        </button>
      </div>
    </aside>
  );
}
