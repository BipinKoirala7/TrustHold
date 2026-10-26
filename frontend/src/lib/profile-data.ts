export type AccountRole = "LANDLORD" | "TENANT" | "ADMIN";

export interface UserProfile {
  fullName: string;
  email: string;
  phone: string;
  role: AccountRole;
  memberSince: string; // ISO date
}

export const roleLabels: Record<AccountRole, string> = {
  LANDLORD: "Landlord",
  TENANT: "Tenant",
  ADMIN: "Admin",
};

// Swap `role` to "TENANT" to preview the tenant version of the page
export const currentProfile: UserProfile = {
  fullName: "Aarav Shrestha",
  email: "aarav@trusthold.demo",
  phone: "+977 9841 234 567",
  role: "LANDLORD",
  memberSince: "2025-02-10",
};

export const getInitials = (name: string) =>
  name
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("");

// Fixed locale + UTC so server and browser render the same text
export const formatMemberSince = (iso: string) =>
  new Intl.DateTimeFormat("en-GB", {
    month: "long",
    year: "numeric",
    timeZone: "UTC",
  }).format(new Date(iso));
