"use client";

import Link from "next/link";
import { redirect, usePathname } from "next/navigation";
import {
  ChevronUp,
  FileText,
  Home,
  LogOut,
  PanelLeftOpen,
  ShieldCheck,
  User,
  UserRound,
} from "lucide-react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarRail,
  SidebarSeparator,
  SidebarTrigger,
  useSidebar,
} from "@/components/ui/sidebar";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/lib/hooks/useAuth";
import { Logo } from "../brand/logo";

const activeStyles =
  "data-active:bg-primary data-active:text-primary-foreground data-active:hover:bg-primary/90 data-active:hover:text-primary-foreground " +
  "data-[active=true]:bg-primary data-[active=true]:text-primary-foreground data-[active=true]:hover:bg-primary/90 data-[active=true]:hover:text-primary-foreground";

function InsetSeparator() {
  return (
    <div className="px-2">
      <SidebarSeparator className="mx-0 w-full" />
    </div>
  );
}

export function AppSidebar() {
  const { user } = useAuth();
  const pathname = usePathname();
  const { state, isMobile, toggleSidebar } = useSidebar();

  if (!user) return redirect("/login");

  const basePath = `/${user.role.toLowerCase()}`;
  const nav = [
    { label: "Dashboard", href: basePath, icon: Home },
    { label: "Leases", href: `/leases`, icon: FileText },
    { label: "Profile", href: `/profile`, icon: User },
  ];

  // On mobile the sidebar is a sheet, so it should always show the full header
  const collapsed = state === "collapsed" && !isMobile;

  // The dashboard link only matches exactly, the rest match their sub-pages too
  const isActive = (href: string) =>
    href === basePath ? pathname === href : pathname.startsWith(href);

  return (
    <Sidebar collapsible="icon">
      <SidebarHeader className="h-14 justify-center">
        {collapsed ? (
          <Button
            type="button"
            onClick={toggleSidebar}
            aria-label="Expand sidebar"
            title="Expand sidebar"
            className="group/logo relative flex size-8 items-center justify-center rounded-lg bg-primary text-primary-foreground transition-colors hover:bg-primary/90"
          >
            <ShieldCheck className="size-4 transition-opacity group-hover/logo:opacity-0" />
            <PanelLeftOpen className="absolute size-4 opacity-0 transition-opacity group-hover/logo:opacity-100" />
          </Button>
        ) : (
          <div className="flex items-center justify-between gap-2">
            <Logo />
            <SidebarTrigger aria-label="Collapse sidebar" />
          </div>
        )}
      </SidebarHeader>

      <InsetSeparator />

      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupContent>
            <SidebarMenu>
              {nav.map((item) => (
                <SidebarMenuItem key={item.href}>
                  <SidebarMenuButton
                    tooltip={item.label}
                    isActive={isActive(item.href)}
                    className={activeStyles}
                    render={<Link href={item.href} />}
                  >
                    <item.icon />
                    <span>{item.label}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>

      <InsetSeparator />

      <SidebarFooter>
        <SidebarMenu>
          <SidebarMenuItem>
            <DropdownMenu>
              <DropdownMenuTrigger
                className="cursor-pointer"
                render={<SidebarMenuButton size="lg" />}
              >
                <div className="flex aspect-square size-8 items-center justify-center rounded-full bg-primary/10 text-xs font-bold text-primary">
                  {user.firstName[0] + user.lastName[0]}
                </div>
                <div className="grid flex-1 text-left leading-tight">
                  <span className="truncate text-sm font-semibold">
                    {user.firstName + " " + user.lastName}
                  </span>
                  <span className="truncate text-xs text-muted-foreground">
                    {user.role}
                  </span>
                </div>
                <ChevronUp className="size-4 text-muted-foreground group-data-[collapsible=icon]:hidden" />
              </DropdownMenuTrigger>
              <DropdownMenuContent
                side={isMobile ? "top" : "right"}
                align="end"
                sideOffset={8}
                className="min-w-44"
              >
                <DropdownMenuItem className="cursor-pointer hover:bg-primary/10">
                  <LogOut />
                  Log out
                </DropdownMenuItem>
                <DropdownMenuItem
                  className="cursor-pointer hover:bg-primary/10"
                  render={<Link href={`/profile`} />}
                >
                  <UserRound />
                  Profile
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarFooter>

      <SidebarRail />
    </Sidebar>
  );
}
