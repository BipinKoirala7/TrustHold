export type StatIcon = "leases" | "escrow" | "payment" | "agreement";
export type StatTone = "primary" | "amber";

export interface DashboardStat {
  id: string;
  label: string;
  value: string;
  hint: string;
  icon: StatIcon;
  tone: StatTone;
}

export type PendingActionKind = "payment" | "settlement" | "photos";

export interface PendingAction {
  id: string;
  kind: PendingActionKind;
  title: string;
  property: string;
  ctaLabel: string;
  href: string;
}

export interface ActivityEntry {
  id: string;
  title: string;
  description: string;
  time: string;
}

export interface CurrentUser {
  name: string;
  firstName: string;
  role: "Landlord" | "Tenant" | "Admin";
  initials: string;
}

export const formatRs = (amount: number) =>
  `Rs. ${amount.toLocaleString("en-IN")}`;

export const currentLandlord: CurrentUser = {
  name: "Aarav Shrestha",
  firstName: "Aarav",
  role: "Landlord",
  initials: "AS",
};

export const dashboardStats: DashboardStat[] = [
  {
    id: "active-leases",
    label: "Active leases",
    value: "3",
    hint: "2 move-outs this month",
    icon: "leases",
    tone: "primary",
  },
  {
    id: "held-in-escrow",
    label: "Held in escrow",
    value: formatRs(210000),
    hint: "Across 4 deposits",
    icon: "escrow",
    tone: "primary",
  },
  {
    id: "awaiting-payment",
    label: "Awaiting payment",
    value: "1",
    hint: `${formatRs(50000)} pending`,
    icon: "payment",
    tone: "primary",
  },
  {
    id: "awaiting-agreement",
    label: "Awaiting my agreement",
    value: "1",
    hint: "Review settlement",
    icon: "agreement",
    tone: "amber",
  },
];

export const pendingActions: PendingAction[] = [
  {
    id: "pa-1",
    kind: "payment",
    title: "Tenant payment is pending",
    property: "Lazimpat Garden Residence",
    ctaLabel: "View lease",
    href: "/landlord/leases/lazimpat-garden-residence",
  },
  {
    id: "pa-2",
    kind: "settlement",
    title: "Review settlement proposal",
    property: "Bhaisepati Green Homes",
    ctaLabel: "Review now",
    href: "/landlord/leases/bhaisepati-green-homes",
  },
  {
    id: "pa-3",
    kind: "photos",
    title: "Move-out photos requested",
    property: "Baluwatar Heights",
    ctaLabel: "Upload photos",
    href: "/landlord/leases/baluwatar-heights",
  },
];

export const recentActivity: ActivityEntry[] = [
  {
    id: "ra-1",
    title: "Settlement agreed",
    description: "Both parties agreed on Bhaisepati Green Homes",
    time: "2 hours ago",
  },
  {
    id: "ra-2",
    title: "Photos uploaded",
    description: "Maya added 8 move-out photos",
    time: "Yesterday",
  },
  {
    id: "ra-3",
    title: "Deposit received",
    description: `${formatRs(65000)} is now held in escrow`,
    time: "3 days ago",
  },
];

export const landlordNav = [
  { label: "Dashboard", href: "/landlord", icon: "dashboard" },
  { label: "Leases", href: "/landlord/leases", icon: "leases" },
  { label: "Profile", href: "/landlord/profile", icon: "profile" },
] as const;
