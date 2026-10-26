"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
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
import { getRoleFromPath, roleUsers } from "@/lib/role-config";

// Solid green for the active item (the default is a subtle accent tint)
const activeStyles =
  "data-active:bg-primary data-active:text-primary-foreground data-active:hover:bg-primary/90 data-active:hover:text-primary-foreground " +
  "data-[active=true]:bg-primary data-[active=true]:text-primary-foreground data-[active=true]:hover:bg-primary/90 data-[active=true]:hover:text-primary-foreground";

// Same width as the menu buttons: inset by the sidebar's p-2, no extra margin
function InsetSeparator() {
  return (
    <div className="px-2">
      <SidebarSeparator className="mx-0 w-full" />
    </div>
  );
}

export function AppSidebar() {
  const pathname = usePathname();
  const { state, isMobile, toggleSidebar } = useSidebar();

  const role = getRoleFromPath(pathname);
  const user = roleUsers[role];

  // "/landlord" or "/tenant": every link below is built from this
  const basePath = `/${role}`;
  const nav = [
    { label: "Dashboard", href: basePath, icon: Home },
    { label: "Leases", href: `${basePath}/leases`, icon: FileText },
    { label: "Profile", href: `${basePath}/profile`, icon: User },
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
            <Link href={basePath} className="flex items-center gap-2">
              <div className="flex items-center justify-center rounded-lg bg-primary p-2 text-primary-foreground">
                <ShieldCheck className="size-4" />
              </div>
              <span className="text-lg font-bold tracking-tight">
                Trust<span className="text-primary">Hold</span>
              </span>
            </Link>
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
                  {user.initials}
                </div>
                <div className="grid flex-1 text-left leading-tight">
                  <span className="truncate text-sm font-semibold">
                    {user.name}
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
                <DropdownMenuItem className="cursor-pointer">
                  <LogOut />
                  Log out
                </DropdownMenuItem>
                <DropdownMenuItem
                  className="cursor-pointer"
                  render={<Link href={`${basePath}/profile`} />}
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
