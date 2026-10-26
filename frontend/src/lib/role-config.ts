import { currentLandlord, type CurrentUser } from "@/lib/mock-data";
import { currentTenant } from "@/lib/tenant-data";

export type Role = "landlord" | "tenant";
export type NavIcon = "dashboard" | "leases" | "profile";

export interface NavItem {
  label: string;
  href: string;
  icon: NavIcon;
}

interface RoleConfig {
  basePath: string;
  user: CurrentUser;
  nav: NavItem[];
}

const buildNav = (base: string): NavItem[] => [
  { label: "Dashboard", href: base, icon: "dashboard" },
  { label: "Leases", href: `${base}/leases`, icon: "leases" },
  { label: "Profile", href: `${base}/profile`, icon: "profile" },
];

export const roleConfig: Record<Role, RoleConfig> = {
  landlord: {
    basePath: "/landlord",
    user: currentLandlord,
    nav: buildNav("/landlord"),
  },
  tenant: {
    basePath: "/tenant",
    user: currentTenant,
    nav: buildNav("/tenant"),
  },
};

// UI-only phase: the role comes from the URL.
// Later, replace this with the role from the logged-in session.
export const getRoleFromPath = (pathname: string): Role =>
  pathname.startsWith("/tenant") ? "tenant" : "landlord";
